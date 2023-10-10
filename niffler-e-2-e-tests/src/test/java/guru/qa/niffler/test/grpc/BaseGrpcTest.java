package guru.qa.niffler.test.grpc;

import com.google.protobuf.Empty;
import guru.qa.grpc.niffler.grpc.NifflerCurrencyServiceGrpc;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.GrpcTest;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.qameta.allure.grpc.AllureGrpc;

@GrpcTest
public class BaseGrpcTest {

    protected static final Config CFG = Config.getInstance();
    protected static final Empty EMPTY = Empty.getDefaultInstance();
    private static final Channel channel;

    static {
        channel = ManagedChannelBuilder
                .forAddress(CFG.getCurrencyGrpcUrl(), CFG.getCurrencyGrpcPort())
                .usePlaintext()
                .intercept(new AllureGrpc())
                .build();
    }

    protected final NifflerCurrencyServiceGrpc.NifflerCurrencyServiceBlockingStub nifflerCurrencyStub = NifflerCurrencyServiceGrpc.newBlockingStub(channel);
}
