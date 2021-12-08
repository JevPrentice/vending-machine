package com.spiderwalk.vendingmachine;

import com.spiderwalk.vendingmachine.domain.Coin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jev Prentice
 * @since 08 December 2021
 */
public class VendingMachineTest {

    private static void addNOfEachCoin(final VendingMachineInventory vendingMachine, final int n) {
        Stream.of(Coin.values()).forEach(c -> vendingMachine.setCoinQuantity(c, n));
    }

    @Test
    @DisplayName("Test vending machine init")
    void testVendingMachineInit() {
        final VendingMachineInventory vendingMachine
                = new VendingMachine(10, List.of(0.01, 0.05, 0.10, 0.20, 0.50, 1.0));

        vendingMachine.setProductPrice(0, 100);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 10);

        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FivePound));

        Assertions.assertEquals(5, vendingMachine.getProductQuantity(0));
        Assertions.assertEquals(100, vendingMachine.getProductPrice(0));
    }

    @Test
    @DisplayName("Test invalid vending machine init")
    void testInvalidVendingMachineInit() {
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> new VendingMachine(-1, List.of(0.10, 0.20, 0.50, 1.0)));
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> new VendingMachine(10, Collections.emptyList()));
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> new VendingMachine(10, List.of(0.15)));
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> new VendingMachine(1, List.of(42d)));
        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> new VendingMachine(-1, List.of(1d)));
    }

    @Test
    @DisplayName("Test get quantity on missing product")
    void testGetQuantityOnMissingProduct() {
        final VendingMachineInventory vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        Assertions.assertEquals(0, vendingMachine.getProductQuantity(0));
        Assertions.assertEquals(0, vendingMachine.getProductQuantity(5));
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> vendingMachine.getProductQuantity(11));
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> vendingMachine.getProductQuantity(-1));
    }

    @Test
    @DisplayName("Test get price on missing product")
    void testGetPriceOnMissingProduct() {
        final VendingMachineInventory vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        Assertions.assertEquals(0, vendingMachine.getProductPrice(0));
        Assertions.assertEquals(0, vendingMachine.getProductPrice(5));
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> vendingMachine.getProductPrice(11));
        Assertions.assertThrowsExactly(IndexOutOfBoundsException.class, () -> vendingMachine.getProductPrice(-1));
    }

    @Test
    @DisplayName("Test happy path with the exact amount")
    void testHappyPathWithTheExactAmount() {
        final VendingMachine vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        vendingMachine.setProductPrice(0, 180);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 10);

        final List<Coin> coinsToPayWith = Stream.of(0.10, 0.20, 0.50, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> coins = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(0, coins.size());
        Assertions.assertEquals(Collections.emptyList(), coins); // Confirm no change returned.

        // Confirm no products updated.
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FivePound));

        Assertions.assertEquals(4, vendingMachine.getProductQuantity(0));
        Assertions.assertEquals(180, vendingMachine.getProductPrice(0));
    }

    @Test
    @DisplayName("Test happy path for scenario that returns change")
    void testHappyPathForScenarioThatReturnsChange() {
        final VendingMachine vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        vendingMachine.setProductPrice(0, 100);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 10);

        final List<Coin> coinsToPayWith = Stream.of(0.10, 0.20, 0.50, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> expected = Stream.of(0.10, 0.20, 0.50)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> result = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(result.containsAll(expected));

        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(9, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(9, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(9, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FivePound));

        Assertions.assertEquals(4, vendingMachine.getProductQuantity(0));
        Assertions.assertEquals(100, vendingMachine.getProductPrice(0));
    }

    @Test
    @DisplayName("Test another happy path for scenario that returns change")
    void testAnotherHappyPathForScenarioThatReturnsChange() {
        final VendingMachine vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        vendingMachine.setProductPrice(0, 130);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 10);

        final List<Coin> coinsToPayWith = Stream.of(1.0, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> expected = Stream.of(0.20, 0.50)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> coins = vendingMachine.purchaseProduct(0, coinsToPayWith);

        Assertions.assertEquals(2, coins.size());
        Assertions.assertTrue(expected.containsAll(coins));
    }

    @Test
    @DisplayName("Test not enough money")
    void testNotEnoughMoney() {

        final VendingMachine vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        vendingMachine.setProductPrice(0, 200);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 10);

        final List<Coin> coinsToPayWith = Stream.of(0.10, 0.20, 0.50, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        Assertions.assertThrowsExactly(IllegalArgumentException.class,
                () -> vendingMachine.purchaseProduct(0, coinsToPayWith));
    }


    @Test
    @DisplayName("Test enough funds but not enough coins in vending machine")
    void testEnoughFundsButNotEnoughCoinsInVendingMachine() {
        final VendingMachine vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        vendingMachine.setProductPrice(0, 130);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 1);

        final List<Coin> coinsToPayWith = Stream.of(5.0, 5.0, 5.0, 5.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
                vendingMachine.purchaseProduct(0, coinsToPayWith));

    }

    @Test
    @DisplayName("Test buying items until none left")
    void testBuyingItemsUntilNoneLeft() {

        final VendingMachine vendingMachine
                = new VendingMachine(10, List.of(0.01, 0.05, 0.10, 0.20, 0.50, 1.0));
        vendingMachine.setProductPrice(0, 300);
        vendingMachine.setProductQuantity(0, 3);

        addNOfEachCoin(vendingMachine, 10);

        final List<Coin> coinsToPayWith = Stream.of(2.0, 2.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> expected = Stream.of(1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> result = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(expected.containsAll(result));

        final List<Coin> result2 = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(1, result2.size());
        Assertions.assertTrue(expected.containsAll(result2));

        final List<Coin> result3 = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(1, result3.size());
        Assertions.assertTrue(expected.containsAll(result3));

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
                vendingMachine.purchaseProduct(0, coinsToPayWith));
    }
}