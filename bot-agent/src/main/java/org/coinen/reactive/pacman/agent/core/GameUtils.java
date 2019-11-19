package org.coinen.reactive.pacman.agent.core;

import org.coinen.reactive.pacman.agent.controllers.Direction;
import qlearn.Q_learn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static qlearn.Q_learn.calculateDistancesInDirection;

public final class GameUtils {

    private GameUtils() {}
    // Fill distances vectors

    public static void rebuildDistances(_G_ game) {
        CountDownLatch latch = new CountDownLatch(4);
        System.out.println("Started calculation");
        new Thread(() -> {
            try {

                calculateDistancesInDirection(game, Direction.UP.index);
            } catch (Throwable t) {
                System.out.println("aaaaa it died " + t);
            }
            latch.countDown();
        }).start();
        new Thread(() -> {
            try {
                calculateDistancesInDirection(game, Direction.DOWN.index);
            } catch (Throwable t) {
                System.out.println("aaaaa it died " + t);
            }
            latch.countDown();
        }).start();
        new Thread(() -> {
            try {
                calculateDistancesInDirection(game, Direction.RIGHT.index);
            } catch (Throwable t) {
                System.out.println("aaaaa it died " + t);
            }
            latch.countDown();
        }).start();
        new Thread(() -> {
            try {
                calculateDistancesInDirection(game, Direction.LEFT.index);
            } catch (Throwable t) {
                System.out.println("aaaaa it died " + t);
            }
            latch.countDown();
        }).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Finished calculation");
    }

    public static void loadDistances() {
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
}
