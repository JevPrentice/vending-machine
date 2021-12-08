package com.spiderwalk.vendingmachine.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Jev Prentice
 * @since 08 December 2021
 */
public class CoinTest {

    @Test
    @DisplayName("Test parse coin from double")
    void testParseCoinFromDouble() {
        Assertions.assertEquals(Coin.OneP, Coin.doubleToCoin(0.01d));
        Assertions.assertEquals(Coin.TwoP, Coin.doubleToCoin(0.02d));
        Assertions.assertEquals(Coin.FiveP, Coin.doubleToCoin(0.05d));
        Assertions.assertEquals(Coin.TenP, Coin.doubleToCoin(0.1d));
        Assertions.assertEquals(Coin.TwentyP, Coin.doubleToCoin(0.2d));
        Assertions.assertEquals(Coin.FiftyP, Coin.doubleToCoin(0.5d));
        Assertions.assertEquals(Coin.OnePound, Coin.doubleToCoin(1d));
        Assertions.assertEquals(Coin.TwoPound, Coin.doubleToCoin(2d));
        Assertions.assertEquals(Coin.FivePound, Coin.doubleToCoin(5d));
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> Coin.doubleToCoin(42));
    }
}