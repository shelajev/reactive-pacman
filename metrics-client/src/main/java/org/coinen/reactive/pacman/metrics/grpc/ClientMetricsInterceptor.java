package org.coinen.reactive.pacman.metrics.grpc;

import java.time.Instant;

import com.google.protobuf.Message;
import com.google.protobuf.UnknownFieldSet;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class ClientMetricsInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions,
        Channel next
    ) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            @SuppressWarnings("unchecked")
            public void sendMessage(ReqT message) {
                if (message instanceof Message) {
                    Instant now = Instant.now();
                    message = (ReqT) ((Message) message)
                        .toBuilder()
                        .setUnknownFields(
                            UnknownFieldSet
                                .newBuilder()
                                .addField(
                                    9999,
                                    UnknownFieldSet.Field
                                        .newBuilder()
                                        .addFixed64(now.getEpochSecond())
                                        .addFixed32(now.getNano())
                                        .build()
                                )
                                .build()
                        )
                        .build();
                }
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(Constants.INSTANT_KEY, Instant.now());
                super.start(responseListener, headers);
            }
        };
    }
}
