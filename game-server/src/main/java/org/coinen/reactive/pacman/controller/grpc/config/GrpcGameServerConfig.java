package org.coinen.reactive.pacman.controller.grpc.config;

import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcGameServerConfig {

    @Bean
    @GRpcGlobalInterceptor
    public UUIDInterceptor uuidInterceptor() {
        return new UUIDInterceptor();
    }
}
