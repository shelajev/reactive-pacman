package org.coinen.reactive.pacman.agent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.coinen.pacman.*;
import org.coinen.pacman.learning.KnowledgeService;
import org.coinen.pacman.learning.KnowledgeServiceClient;
import org.coinen.reactive.pacman.agent.core.G;
import org.coinen.reactive.pacman.agent.core._G_;
import org.coinen.reactive.pacman.agent.model.GameState;
import org.coinen.reactive.pacman.agent.repository.TemporaryHistoryRepository;
import org.coinen.reactive.pacman.agent.repository.impl.InMemoryKnowledgeRepository;
import org.coinen.reactive.pacman.agent.repository.KnowledgeRepository;
import org.coinen.reactive.pacman.agent.repository.impl.InMemoryTemporaryHistoryRepositoryImpl;
import org.coinen.reactive.pacman.agent.repository.impl.RemoteKnowledgeRepository;
import org.coinen.reactive.pacman.agent.service.DecisionService;
import org.coinen.reactive.pacman.agent.service.impl.DefaultGameEngineService;
import org.coinen.reactive.pacman.agent.service.GameEngineService;
import org.coinen.reactive.pacman.agent.service.LearningService;
import org.coinen.reactive.pacman.agent.service.impl.QLearningDecisionService;
import org.coinen.reactive.pacman.agent.service.impl.QLearningLearningService;
import qlearn.Q_learn;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * One simulator can run one instance of PacMan-vs-Ghosts game.
 * <p>
 * Can be used for both head/less games.
 *
 * @author Jimmy
 */
public class PacManSimulator {

    static final ByteBuf PACMAN_PLAYER;
    static {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeCharSequence(Player.Type.PACMAN.name(), Charset.defaultCharset());
        PACMAN_PLAYER = buffer.retain();
    }

    public static class GameConfig {

        public int seed = -1;

        /**
         * Whether POWER PILLS should be present within the environment.
         */
        public boolean powerPillsEnabled = true;

        /**
         * Total percentage of PILLS present within the level. If < 1, some (random) pills will be taken away.
         */
        public double totalPills = 1;

        /**
         * How many levels Ms PacMan may play (-1 => unbound).
         */
        public int levelsToPlay = -1;


        public GameConfig clone() {
            GameConfig result = new GameConfig();

            result.seed = seed;
            result.powerPillsEnabled = powerPillsEnabled;
            result.totalPills = totalPills;
            result.levelsToPlay = levelsToPlay;

            return result;
        }

        public String asString() {
            return "" + seed + ";" + powerPillsEnabled + ";" + totalPills + ";" + levelsToPlay;
        }

        public void fromString(String line) {
            String[] all = line.split(";");
            seed = Integer.parseInt(all[0]);
            powerPillsEnabled = Boolean.parseBoolean(all[1]);
            totalPills = Double.parseDouble(all[2]);
            levelsToPlay = Integer.parseInt(all[3]);
        }

        public String getCSVHeader() {
            return "seed;powerPillsEnabled;totalPills;levelsToPlay";
        }

        public String getCSV() {
            return "" + seed + ";" + powerPillsEnabled + ";" + totalPills + ";" + levelsToPlay;
        }

    }

    private SimulatorConfig config;

    private _G_ game;

    //private static Q_learn Q_learn;


    /*-----------------------------------------------------------------------------------*/
    /*
     * FUNCTIONS FOR STORING DATA IN TEXT FILES
     * */

    public static void storeCountData() {

        try {
            PrintWriter writer = new PrintWriter("Count.txt", "UTF-8");

            writer.println("States have been retrieved this amount of times:");

            for (int i = 0; i < Q_learn.stateCounter.size(); i++) {
                writer.println(i + " -> " + Q_learn.stateCounter.get(i));
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error printing count");
        }

    }

    public static void storeScores() {

        try {
            PrintWriter writer = new PrintWriter("Scores.txt", "UTF-8");

            for (int i = 0; i < Q_learn.scores.size(); i++) {
                //writer.println("Play no " + i + ":" + Q_learn.scores.get(i));
                writer.println(i + " " + Q_learn.scores.get(i));
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error printing scores.");
        }

        try {
            PrintWriter writer = new PrintWriter("drawableScores.txt", "UTF-8");

            for (int i = 0; i < Q_learn.drawableScores.size(); i++) {
                //writer.println("Play n� " + i + ":" + Q_learn.scores.get(i));
                writer.println(i + " " + Q_learn.drawableScores.get(i));
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error printing scores.");
        }


    }

    public static void storeExperiment() {

        try {
            PrintWriter writer = new PrintWriter("Experiment.txt", "UTF-8");


            writer.println("El experimento se ha completado con los siguientes par�metros:");
            writer.println("Pesos: (" + Q_learn.w1 + "," + Q_learn.w2 + "," + Q_learn.w3 + "," + Q_learn.w4 + "," + Q_learn.w5 + ")");
            writer.println("Episodios: " + Q_learn.n_ep + " - Alpha: " + Q_learn.alpha + " - Gamma: " + Q_learn.gamma + " - Case threshold: " + Q_learn.newCaseThreshold);
            writer.println("Media puntuaciones: " + Q_learn.avgScore + " - Media tiempos: " + Q_learn.avgTime + " - StdDev Score: " + Q_learn.stdScore + " - StdDev Time: " + Q_learn.stdTime +
                    "Max Score:" + Q_learn.maxScore + "Max Tiempo: " + Q_learn.maxTime + "Max. levels: " + Q_learn.maxLevel + "Avg levels: " + Q_learn.avgLevels);

            writer.close();
        } catch (IOException e) {
            System.out.println("Error storing experiment data");
        }

    }

    public static void storeTimes() {

        try {
            PrintWriter writer = new PrintWriter("Times.txt", "UTF-8");

            for (int i = 0; i < Q_learn.scores.size(); i++) {
                //writer.println("Play n� " + i + ":" + Q_learn.scores.get(i));
                writer.println(i + " " + Q_learn.times.get(i));
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error printing times.");
        }

        try {
            PrintWriter writer = new PrintWriter("DrwabaleTimes.txt", "UTF-8");

            for (int i = 0; i < Q_learn.drawableTimes.size(); i++) {
                //writer.println("Play n� " + i + ":" + Q_learn.scores.get(i));
                writer.println(i + " " + Q_learn.drawableTimes.get(i));
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error printing times.");
        }

    }

    public static void storeQ() {

        try {
            PrintWriter writer = new PrintWriter("Qdata.txt", "UTF-8");

            for (int i = 0; i < Q_learn.Q.size(); i++) {
                writer.println(parseQValue(i, 0) + " " + parseQValue(i, 1) + " " + parseQValue(i, 2) + " " + parseQValue(i, 3) + " " + Q_learn.stateCounter.get(i));
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error storing Q matrix");
        }


        try {
            PrintWriter writer = new PrintWriter("Q_Matrix.txt", "UTF-8");

            writer.println("Q matrix has stored the following info:");

            for (int i = 0; i < Q_learn.Q.size(); i++) {
                writer.println(i + " -> " + Q_learn.Q.get(i));
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error printing Q matrix");
        }

    }

    public static float parseQValue(int row, int col) {
        float result = -1;

        if (Q_learn.Q.get(row).get(col) < 0.001) result = 0;
        else result = Q_learn.Q.get(row).get(col);

        return result;
    }

    public static void storeS() {

        try {
            PrintWriter writer = new PrintWriter("Sdata.txt", "UTF-8");

            for (int i = 0; i < Q_learn.S.size(); i++) {
                writer.println(Q_learn.S.get(i).data());
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error storing S matrix");
        }

        try {
            PrintWriter writer = new PrintWriter("States.txt", "UTF-8");

            writer.println("Q matrix has stored the following info:");

            for (int i = 0; i < Q_learn.S.size(); i++) {
                writer.println(Q_learn.S.get(i).toString());
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error printing state list.");
        }

    }

    public static void storeData() {
        storeQ();

        storeCountData();
        storeS();

        storeExperiment();
        storeScores();
        storeTimes();

    }

    /*-----------------------------------------------------------------------------------*/
    /*
     * FUNCTIONS FOR LOADING DATA FROM TEXT FILES
     * */

    public static void loadQ() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("Qdata.txt"));
            float index = 0, q0, q1, q2, q3;
            int count;
            String line;
            String[] numbers = null;

            while ((line = reader.readLine()) != null) {
                numbers = line.split("\\d\\s+");

                q0 = Float.valueOf(numbers[0].trim());
                q1 = Float.valueOf(numbers[1].trim());
                q2 = Float.valueOf(numbers[2].trim());
                q3 = Float.valueOf(numbers[3].trim());
                count = Integer.parseInt(numbers[4].trim());

                Q_learn.Q.add(new ArrayList<>(Arrays.asList(q0, q1, q2, q3)));
                Q_learn.stateCounter.add(count);
            }

            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read Qdata.txt");
            e.printStackTrace();
        }

    }

    public static void loadScores() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("Scores.txt"));
            float index = 0;
            int count, score;
            String line, part;
            String[] s1 = null, s2 = null;

            while ((line = reader.readLine()) != null) {
                s1 = line.split(" ");

                score = Integer.valueOf(s1[1].trim());
                Q_learn.scores.add(score);
	           
		    	
		    	/*
		    	s1 = line.split(" ");
		    	
		    	score = Integer.valueOf(s1[1].trim());
		    	Q_learn.scores.add(score);*/
            }

            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read Scores.txt");
            e.printStackTrace();
        }


    }


    public static void loadOtherScores() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("RandomScores.txt"));
            float index = 0;
            int count, score;
            String line, part;
            String[] s1 = null, s2 = null;

            while ((line = reader.readLine()) != null) {
                s1 = line.split(" ");

                score = Integer.valueOf(s1[1].trim());
                Q_learn.randomScores.add(score);


            }

            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read Scores.txt");
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader("ClosestPillScores.txt"));
            float index = 0;
            int count, score;
            String line, part;
            String[] s1 = null, s2 = null;

            while ((line = reader.readLine()) != null) {
                s1 = line.split(" ");

                score = Integer.valueOf(s1[1].trim());
                Q_learn.closestPillScores.add(score);


            }

            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read Scores.txt");
            e.printStackTrace();
        }
    }

    public static void loadTimes() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("Times.txt"));
            float index = 0;
            int count, time;
            String line, part;
            String[] s1 = null, s2 = null;

            while ((line = reader.readLine()) != null) {
                s1 = line.split(" ");

                time = Integer.valueOf(s1[1].trim());
                Q_learn.times.add(time);
	           
		    	
		    	/*
		    	s1 = line.split(" ");
		    	
		    	score = Integer.valueOf(s1[1].trim());
		    	Q_learn.scores.add(score);*/
            }

            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read Times.txt");
            e.printStackTrace();
        }

    }


    public static void loadS() {
		  /*try
		  {
		    BufferedReader reader = new BufferedReader(new FileReader("Sdata.txt"));
		    int index = 0,  cp, cg, cpp, ceg;
		    boolean u, r, d, l;
		    String line;
		    String[] numbers = null;
		    
		    
		    while ((line = reader.readLine()) != null)
		    {
		    	
		    	numbers = line.split(" ");
		    	
		    	index = Integer.parseInt(numbers[0].trim());
	           	u = Boolean.parseBoolean(numbers[1].trim());
	           	r = Boolean.parseBoolean(numbers[2].trim());
	           	d = Boolean.parseBoolean(numbers[3].trim()); 	
	           	l = Boolean.parseBoolean(numbers[4].trim());
	           	cp = Integer.parseInt(numbers[5].trim());
	           	cg = Integer.parseInt(numbers[6].trim());
	           	cpp = Integer.parseInt(numbers[7].trim());
	           	ceg = Integer.parseInt(numbers[8].trim());
	           	
	           Q_learn.S.add(new GameState(index, u, r, d, l, cp, cg, cpp, ceg));
		    }
		    
		    reader.close();
		  }
		  catch (Exception e)
		  {
		    System.err.format("Exception occurred trying to read Sdata.txt");
		    e.printStackTrace();
		  }*/


        try {
            BufferedReader reader = new BufferedReader(new FileReader("Sdata.txt"));
            int index = 0, pu, pr, pd, pl, gu, gr, gd, gl, ppu, ppr, ppd, ppl, egu, egr, egd, egl, iu, ir, id, il;
            String line;
            String[] numbers = null;


            while ((line = reader.readLine()) != null) {

                numbers = line.split(" ");

                index = Integer.parseInt(numbers[0].trim());
                pu = Integer.parseInt(numbers[1].trim());
                pr = Integer.parseInt(numbers[2].trim());
                pd = Integer.parseInt(numbers[3].trim());
                pl = Integer.parseInt(numbers[4].trim());
                gu = Integer.parseInt(numbers[5].trim());
                gr = Integer.parseInt(numbers[6].trim());
                gd = Integer.parseInt(numbers[7].trim());
                gl = Integer.parseInt(numbers[8].trim());
                ppu = Integer.parseInt(numbers[9].trim());
                ppr = Integer.parseInt(numbers[10].trim());
                ppd = Integer.parseInt(numbers[11].trim());
                ppl = Integer.parseInt(numbers[12].trim());
                egu = Integer.parseInt(numbers[13].trim());
                egr = Integer.parseInt(numbers[14].trim());
                egd = Integer.parseInt(numbers[15].trim());
                egl = Integer.parseInt(numbers[16].trim());
                iu = Integer.parseInt(numbers[17].trim());
                ir = Integer.parseInt(numbers[18].trim());
                id = Integer.parseInt(numbers[19].trim());
                il = Integer.parseInt(numbers[20].trim());
                //ng = Integer.valueOf(numbers[3].trim());

                Q_learn.S.add(new GameState(pu, pr, pd, pl, gu, gr, gd, gl, ppu, ppr, ppd, ppl, egu, egr, egd, egl
                        , iu, ir, id, il));
            }

            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read Sdata.txt");
            e.printStackTrace();
        }

    }


    // Fill distances vectors
    public static void loadDistances() {
        //ArrayList<ArrayList<Integer>> distances = new ArrayList<ArrayList<Integer>>();
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("distances/distances.txt")));
//            String line;
//            String[] numbers = null;
//            int row = 0;
//
//            System.out.println("Loading distances...");
//
//            while ((line = reader.readLine()) != null) {
//                numbers = line.split(" ");
//
//                Q_learn.distances.add(new ArrayList<Integer>());
//                for (int i = 0; i < numbers.length - 1; i++) {
//                    Q_learn.distances.get(row).add(Integer.parseInt(numbers[i].trim()));
//
//                }
//                ;
//
//                if (row % 100 == 0) System.out.print("%");
//                row++;
//            }
//
//            System.out.println("%");
//            System.out.println("Load completed succesfully");
//
//            reader.close();
//
//
//        } catch (FileNotFoundException e) {
//            System.err.format("Exception occurred trying to read distances.txt");
//            e.printStackTrace();
//        } catch (IOException e) {
//            System.err.format("IO-Exception occurred trying to read distances.txt");
//            e.printStackTrace();
//        }


        //ArrayList<ArrayList<Integer>> distancesUp = new ArrayList<ArrayList<Integer>>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(("distances/distances0.txt"))));
            String line;
            String[] numbers = null;
            int row = 0;

            System.out.println("Loading distances in direction: UP");

            while ((line = reader.readLine()) != null) {
                numbers = line.split(" ");
                //int i = 0;

                Q_learn.distancesUp.add(new ArrayList<>());
                for (int i = 0; i < numbers.length - 1; i++) {
                    Q_learn.distancesUp.get(row).add(Integer.parseInt(numbers[i].trim()));
                }
		    	/*
		    	while(Integer.parseInt(numbers[i].trim()) != -1) {
		    		Q_learn.distancesUp.add(new ArrayList<Integer>());
		    		Q_learn.distancesUp.get(row).add(Integer.parseInt(numbers[i].trim()));
		    		i++;
		    	};*/

                if (row % 100 == 0) System.out.print("-");
                row++;
            }
            System.out.println("-");
            System.out.println("Load completed succesfully");

            reader.close();


        } catch (FileNotFoundException e) {
            System.err.format("Exception occurred trying to read distances.txt");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.format("IO-Exception occurred trying to read distances.txt");
            e.printStackTrace();
        }

        //ArrayList<ArrayList<Integer>> distances = new ArrayList<ArrayList<Integer>>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("distances/distances1.txt")));
            String line;
            String[] numbers = null;
            int row = 0;

            System.out.println("Loading distances in direction RIGHT");

            while ((line = reader.readLine()) != null) {
                numbers = line.split(" ");
                Q_learn.distancesRight.add(new ArrayList<>());
                for (int i = 0; i < numbers.length - 1; i++) {
                    Q_learn.distancesRight.get(row).add(Integer.parseInt(numbers[i].trim()));
                }

                if (row % 100 == 0) System.out.print("-");
                row++;
            }

            System.out.println("-");
            System.out.println("Load completed succesfully");

            reader.close();


        } catch (FileNotFoundException e) {
            System.err.format("Exception occurred trying to read distances.txt");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.format("IO-Exception occurred trying to read distances.txt");
            e.printStackTrace();
        }

        //ArrayList<ArrayList<Integer>> distances = new ArrayList<ArrayList<Integer>>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("distances/distances2.txt")));
            String line;
            String[] numbers = null;
            int row = 0;

            System.out.println("Loading distances in direction DOWN");

            while ((line = reader.readLine()) != null) {
                numbers = line.split(" ");
                Q_learn.distancesDown.add(new ArrayList<>());
                for (int i = 0; i < numbers.length - 1; i++) {
                    Q_learn.distancesDown.get(row).add(Integer.parseInt(numbers[i].trim()));
                }

                if (row % 100 == 0) System.out.print("-");
                row++;
            }

            System.out.println("-");
            System.out.println("Load completed succesfully");

            reader.close();


        } catch (FileNotFoundException e) {
            System.err.format("Exception occurred trying to read distances.txt");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.format("IO-Exception occurred trying to read distances.txt");
            e.printStackTrace();
        }

        //ArrayList<ArrayList<Integer>> distances = new ArrayList<ArrayList<Integer>>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("distances/distances3.txt")));
            String line;
            String[] numbers = null;
            int row = 0;

            System.out.println("Loading distances in direction LEFT");

            while ((line = reader.readLine()) != null) {
                numbers = line.split(" ");
                Q_learn.distancesLeft.add(new ArrayList<>());
                for (int i = 0; i < numbers.length - 1; i++) {
                    Q_learn.distancesLeft.get(row).add(Integer.parseInt(numbers[i].trim()));
                }

                if (row % 100 == 0) System.out.print("-");
                row++;
            }

            System.out.println("-");
            System.out.println("Load completed succesfully");

            reader.close();


        } catch (FileNotFoundException e) {
            System.err.format("Exception occurred trying to read distances.txt");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.format("IO-Exception occurred trying to read distances.txt");
            e.printStackTrace();
        }

        Q_learn.currDistancesUp = Q_learn.distancesUp;
        Q_learn.currDistancesRight = Q_learn.distancesRight;
        Q_learn.currDistancesDown = Q_learn.distancesDown;
        Q_learn.currDistancesLeft = Q_learn.distancesLeft;
    }


    public static void loadData() {

        loadQ();
        loadS();
        loadScores();
        loadTimes();
//		loadDistances();
    }


    public static void printEpisode(int i) {
        System.out.println("Episode " + (i + 1) + " finished \n");
        System.out.println("There are " + Q_learn.S.size() + " states \n");

    }

    public static void main(String[] args) throws InterruptedException {
        RSocket remoteServerKnowledgeBase = RSocketFactory.connect()
                .frameDecoder(PayloadDecoder.ZERO_COPY)
                .transport(TcpClientTransport.create(9099))
                .start()
                .block();
        TemporaryHistoryRepository temporaryHistoryRepository = new InMemoryTemporaryHistoryRepositoryImpl();
        KnowledgeRepository knowledgeRepository = new RemoteKnowledgeRepository(new KnowledgeServiceClient(remoteServerKnowledgeBase));
        RSocket rSocket = RSocketFactory.connect()
                .frameDecoder(PayloadDecoder.ZERO_COPY)
                .acceptor(new SocketAcceptor() {
                    @Override
                    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
                        return Mono.just(new MapServiceServer((map, metadata) -> {
                            loadDistances();
                            GameServiceClient gameServiceClient = new GameServiceClient(sendingSocket);
                            PlayerServiceClient locationServiceClient = new PlayerServiceClient(sendingSocket);
                            ExtrasServiceClient extrasService = new ExtrasServiceClient(sendingSocket);
                            LearningService learningService = new QLearningLearningService(knowledgeRepository, temporaryHistoryRepository);

                            var disposable = Mono.defer(() -> gameServiceClient
                                    .start(
                                        Nickname.newBuilder()
                                                .setValue("MsPacMan" + ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE))
                                                .build(),
                                        PACMAN_PLAYER.retainedSlice()
                                    ))
                                    .flatMapMany(config -> {
                                        var game = new _G_();
                                        game.newGame(map, config);
                                        DecisionService decisionService = new QLearningDecisionService(temporaryHistoryRepository, knowledgeRepository, game);
                                        GameEngineService gameEngineService = new DefaultGameEngineService(map, config, game, locationServiceClient, gameServiceClient, extrasService);

                                        // INIT RANDOMNESS
                                        G.rnd = new Random();

                                        return decisionService
                                                .decide()
                                                .transform(gameEngineService::run)
                                                .transform(learningService::learn)
                                                .as(knowledgeRepository::educate);

                                    })
                                    .doOnEach(System.out::println)
                                    .repeat()
                                    .subscribe(System.out::print, Throwable::printStackTrace, () -> System.out.println("done"));

                            sendingSocket.onClose()
                                    .doOnTerminate(disposable::dispose)
                                    .subscribe();

                            // INITIALIZE THE SIMULATION


                            return Mono.empty();
                        }, Optional.empty(), Optional.empty(), Optional.empty()));
                    }
                })
                .transport(WebsocketClientTransport.create(3000))
                .start()
                .block();

        rSocket.onClose().block();
    }
}
