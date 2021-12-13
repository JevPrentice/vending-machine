package com.spiderwalk.vendingmachine;

import com.spiderwalk.vendingmachine.domain.Coin;
import com.spiderwalk.vendingmachine.domain.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Jev Prentice
 * @since 08 December 2021
 */
public class VendingMachineTest {

    private static void addNOfEachCoin(final VendingMachineInventory vendingMachine, final int n) {
        Stream.of(Coin.values()).forEach(c -> vendingMachine.setCoinQuantity(c, n));
    }

    private VendingMachine createNormalTestVendingMachine() {
        final VendingMachine vendingMachine = new VendingMachine(10,
                List.of(0.01d, 0.02d, 0.05d, 0.10d, 0.20d, 0.50, 1.0, 2.0, 5.0));

        // default products for test cases £1 each, 10 avail to use for change.
        IntStream.range(0, vendingMachine.getProducts().size()).forEach(i -> {
            vendingMachine.setProductPrice(i, 100);
            vendingMachine.setProductQuantity(i, 10);
        });

        addNOfEachCoin(vendingMachine, 10);

        return vendingMachine;
    }

    @Test
    @DisplayName("Test vending machine init")
    void testVendingMachineInit() {

        final VendingMachine vendingMachine = createNormalTestVendingMachine();

        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FivePound));

        Assertions.assertEquals(10, vendingMachine.getProductQuantity(0));
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
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> vendingMachine.getProductQuantity(11));
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> vendingMachine.getProductQuantity(-1));
    }

    @Test
    @DisplayName("Test get price on missing product")
    void testGetPriceOnMissingProduct() {
        final VendingMachineInventory vendingMachine
                = new VendingMachine(10, List.of(0.10, 0.20, 0.50, 1.0));
        Assertions.assertEquals(0, vendingMachine.getProductPrice(0));
        Assertions.assertEquals(0, vendingMachine.getProductPrice(5));
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> vendingMachine.getProductPrice(11));
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> vendingMachine.getProductPrice(-1));
    }

    @Test
    @DisplayName("Test happy path with the exact amount")
    void testHappyPathWithTheExactAmount() {
        final VendingMachine vendingMachine = createNormalTestVendingMachine();

        vendingMachine.setProductPrice(0, 180);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 10);

        final List<Coin> coinsToPayWith = Stream.of(0.10, 0.20, 0.50, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> coins = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(0, coins.size());
        Assertions.assertEquals(Collections.emptyList(), coins); // Confirm no change returned.

        // Confirm product 0 quantity is reduced
        Assertions.assertEquals(4, vendingMachine.getProductQuantity(0));
        Assertions.assertEquals(180, vendingMachine.getProductPrice(0));

        // Confirm product 1 quantity is unchanged
        Assertions.assertEquals(10, vendingMachine.getProductQuantity(1));
        Assertions.assertEquals(100, vendingMachine.getProductPrice(1));

        // Confirm coins are absorbed into the vending machine
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FivePound));
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
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.OnePound));
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
        Assertions.assertEquals(expected.size(), coins.size());
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
        final VendingMachine vendingMachine = new VendingMachine(10,
                List.of(0.01d, 0.02d, 0.05d, 0.10d, 0.20d, 0.50, 1.0, 2.0, 5.0));
        vendingMachine.setProductPrice(0, 130);
        vendingMachine.setProductQuantity(0, 5);

        addNOfEachCoin(vendingMachine, 1);

        final List<Coin> coinsToPayWith = Stream.of(5.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> actualResult = vendingMachine.purchaseProduct(0, coinsToPayWith);

        final List<Coin> expected = Stream.of(1.0, 2.0, 0.5, 0.2)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        Assertions.assertEquals(expected.size(), actualResult.size());
        Assertions.assertTrue(expected.containsAll(actualResult));

        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(2, vendingMachine.getCoinQuantity(Coin.FivePound));

        Assertions.assertThrowsExactly(IllegalStateException.class, () -> {
            final List<Coin> coinsToPayWith2 = Stream.of(5.0)
                    .map(Coin::doubleToCoin)
                    .collect(Collectors.toList());

            vendingMachine.purchaseProduct(0, coinsToPayWith2);
        });

        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(2, vendingMachine.getCoinQuantity(Coin.FivePound));
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

    @Test
    @DisplayName("Empty vending machine product costs £1 customer pays with 2x £1 coins returns £1")
    void emptyVendingMachineProductCosts1CustomerPaysWith2X1CoinsReturns1() {
        final VendingMachine vendingMachine
                = new VendingMachine(10, List.of(0.01, 0.02, 0.05, 0.10, 0.20, 0.50, 1.0, 2.0, 5.0));
        vendingMachine.setProductPrice(0, 100);
        vendingMachine.setProductQuantity(0, 3);

        final List<Coin> coinsToPayWith = Stream.of(1.0, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> expected = Stream.of(1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> result = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(expected.containsAll(result));

        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FivePound));
    }

    @Test
    @DisplayName("Vending machine tries next biggest denomination 3x20p example")
    void vendingMachineTriesNextBiggestDenomination3X20PExample() {
        final VendingMachine vendingMachine = new VendingMachine(10,
                List.of(0.01d, 0.02d, 0.05d, 0.10d, 0.20d, 0.50, 1.0, 2.0, 5.0));

        // default products for test cases £1 each, 10 avail to use for change.
        IntStream.range(0, vendingMachine.getProducts().size()).forEach(i -> {
            vendingMachine.setProductQuantity(i, 10);
            vendingMachine.setProductPrice(i, 140);
        });

        vendingMachine.setCoinQuantity(Coin.FiftyP, 1); // HAS ONE 50p, but 0 50p
        vendingMachine.setCoinQuantity(Coin.TenP, 0);
        vendingMachine.setCoinQuantity(Coin.TwentyP, 3); // The actual change that should be used.

        Stream.of(Coin.values()).forEach(c -> {
            final int actualQuantity = vendingMachine.getCoinQuantity(c);
            final int expectedQuantity = switch (c) {
                case TwentyP -> 3;
                case FiftyP -> 1;
                default -> 0;
            };
            Assertions.assertEquals(expectedQuantity, actualQuantity);
        });

        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(3, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FivePound));

        final List<Coin> coinsToPayWith = Stream.of(1.0, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> expected = Stream.of(0.2, 0.2, 0.2)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> result = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertTrue(expected.containsAll(result));
        Assertions.assertEquals(expected.size(), result.size());

        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(1, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(2, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(0, vendingMachine.getCoinQuantity(Coin.FivePound));
    }

    @Test
    @DisplayName("Vending machine should accumulate coins as customers use the Machine")
    void vendingMachineShouldAccumulateCoinsAsCustomersUseTheMachine() {
        testHappyPathWithTheExactAmount();

        final VendingMachine vendingMachine = createNormalTestVendingMachine();

        vendingMachine.setProductPrice(0, 180);
        vendingMachine.setProductQuantity(0, 5);

        final List<Coin> coinsToPayWith = Stream.of(0.10, 0.20, 0.50, 1.0)
                .map(Coin::doubleToCoin)
                .collect(Collectors.toList());

        final List<Coin> coins = vendingMachine.purchaseProduct(0, coinsToPayWith);
        Assertions.assertEquals(0, coins.size());
        Assertions.assertEquals(Collections.emptyList(), coins); // Confirm no change returned.

        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.OneP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoP));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FiveP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.TenP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.TwentyP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.FiftyP));
        Assertions.assertEquals(11, vendingMachine.getCoinQuantity(Coin.OnePound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.TwoPound));
        Assertions.assertEquals(10, vendingMachine.getCoinQuantity(Coin.FivePound));
    }

    @Test
    @DisplayName("Test encapsulation")
    void testEncapsulation() {
        final VendingMachine vendingMachine = createNormalTestVendingMachine();

        final List<Product> products = vendingMachine.getProducts();
        final Product product = products.get(0);
        product.setQuantity(999);
        Assertions.assertEquals(999, product.getQuantity());

        Assertions.assertEquals(10,
                vendingMachine.getProducts().get(0).getQuantity());

        Assertions.assertThrowsExactly(UnsupportedOperationException.class, () ->
                vendingMachine.getCoins().put(Coin.TenP, 5));
    }
}