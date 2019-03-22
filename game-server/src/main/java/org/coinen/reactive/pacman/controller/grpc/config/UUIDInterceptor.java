package org.coinen.reactive.pacman.controller.grpc.config;

import java.util.UUID;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

@GRpcGlobalInterceptor
class UUIDInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next) {
        String uuid = headers.get(Metadata.Key.of("uuid", ASCII_STRING_MARSHALLER));
        UUIDHolder.set(UUID.fromString(uuid));
        return next.startCall(call, headers);
    }
}
