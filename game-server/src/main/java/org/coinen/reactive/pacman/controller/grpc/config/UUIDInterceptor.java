package org.coinen.reactive.pacman.controller.grpc.config;

import java.util.UUID;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class UUIDInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> META_UUID_KEY = Metadata.Key.of("uuid", ASCII_STRING_MARSHALLER);

    public static final Context.Key<UUID> CONTEXT_UUID_KEY = Context.key("uuid");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    ) {
        String uuid = headers.get(META_UUID_KEY);
        Context context = Context.current();

        if (uuid != null) {
            context = context.withValue(CONTEXT_UUID_KEY, UUID.fromString(uuid));
        }

        return Contexts.interceptCall(context, call,headers, next);
    }
}
