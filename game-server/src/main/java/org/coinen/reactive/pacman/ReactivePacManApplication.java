package org.coinen.reactive.pacman;

import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import org.coinen.reactive.pacman.controller.rsocket.SetupController;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ReactivePacManApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext =
            SpringApplication.run(ReactivePacManApplication.class, args);

        SetupController setupController =
            applicationContext.getBean(SetupController.class);

        RSocketFactory.receive()
                      .acceptor(setupController)
                      .transport(WebsocketServerTransport.create(3000))
                      .start()
                      .block();
    }

}
