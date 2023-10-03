package guru.qa.niffler.test.grpc;

import guru.qa.grpc.niffler.grpc.*;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NifflerCurrencyGrpcTest extends BaseGrpcTest {

    @Test
    @AllureId("700")
    void getAllCurrenciesTest() {
        CurrencyResponse allCurrencies = nifflerCurrencyStub.getAllCurrencies(EMPTY);
        final List<Currency> currenciesList = allCurrencies.getAllCurrenciesList();
        assertEquals(4, allCurrencies.getAllCurrenciesList().size());
        assertEquals(CurrencyValues.RUB, currenciesList.get(0).getCurrency());
        assertEquals(0.015, currenciesList.get(0).getCurrencyRate());
        assertEquals(CurrencyValues.KZT, currenciesList.get(1).getCurrency());
        assertEquals(0.0021, currenciesList.get(1).getCurrencyRate());
        assertEquals(CurrencyValues.EUR, currenciesList.get(2).getCurrency());
        assertEquals(1.08, currenciesList.get(2).getCurrencyRate());
        assertEquals(CurrencyValues.USD, currenciesList.get(3).getCurrency());
        assertEquals(1.0, currenciesList.get(3).getCurrencyRate());
    }

    static Stream<Arguments> calculateCurrencyRateTest() {
        return Stream.of(
                Arguments.of(CurrencyValues.USD, CurrencyValues.RUB, 100.0, 6666.67),
                Arguments.of(CurrencyValues.RUB, CurrencyValues.USD, 100.0, 1.5),
                Arguments.of(CurrencyValues.USD, CurrencyValues.USD, 100.0, 100.0),
                Arguments.of(CurrencyValues.USD, CurrencyValues.KZT, 100.0, 47619.05),
                Arguments.of(CurrencyValues.KZT, CurrencyValues.EUR, 100.0, 0.19),
                Arguments.of(CurrencyValues.KZT, CurrencyValues.USD, -100.0, -0.21),
                Arguments.of(CurrencyValues.USD, CurrencyValues.KZT, -100.0, -47619.05)
        );
    }

    @ParameterizedTest()
    @MethodSource
    @AllureId("701")
    void calculateCurrencyRateTest(CurrencyValues spendCurrency,
                                   CurrencyValues desiredCurrency,
                                   double amount,
                                   double expected) {
        CalculateRequest request = CalculateRequest.newBuilder()
                .setAmount(amount)
                .setSpendCurrency(spendCurrency)
                .setDesiredCurrency(desiredCurrency)
                .build();

        final CalculateResponse calculateResponse = nifflerCurrencyStub.calculateRate(request);
        assertEquals(expected, calculateResponse.getCalculatedAmount());
    }

    @Test
    @AllureId("702")
    void calculateRateWithZeroTest() {
        CalculateRequest request = CalculateRequest.newBuilder()
                .setAmount(0)
                .setSpendCurrency(CurrencyValues.RUB)
                .setDesiredCurrency(CurrencyValues.EUR)
                .build();
        final CalculateResponse calculateResponse = nifflerCurrencyStub.calculateRate(request);
        assertEquals(0, calculateResponse.getCalculatedAmount());
    }
}
