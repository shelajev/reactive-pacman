package org.coinen.reactive.pacman.controller.grpc;

import java.util.UUID;

import com.google.protobuf.Empty;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.coinen.pacman.Map;
import org.coinen.pacman.ReactorSetupServiceGrpc;
import org.coinen.reactive.pacman.service.MapService;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

@GRpcService
public class GrpcSetupController extends ReactorSetupServiceGrpc.SetupServiceImplBase {

    final MapService mapService;

    public GrpcSetupController(MapService mapService) {
        this.mapService = mapService;
    }

    @Override
    public Mono<Map> get(Mono<Empty> request) {
        return Mono.just(mapService.getMap());
    }

    @GRpcGlobalInterceptor
    static class TestInterceptor implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
            String uuid = headers.get(Metadata.Key.of("uuid", ASCII_STRING_MARSHALLER));
            UUIDHolder.set(UUID.fromString(uuid));
            return next.startCall(call, headers);
        }
    }
}
