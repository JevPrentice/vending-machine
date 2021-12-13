package com.spiderwalk.vendingmachine;

import com.spiderwalk.vendingmachine.domain.Coin;
import com.spiderwalk.vendingmachine.domain.Product;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of a simple vending machine.
 * <p>
 * A vending machine is initiated with a fixed number of product slots and a number of supported coins represented
 * as by their decimal value.
 * </p>
 * <p>
 * A vending machine has methods to get/set the price or quantity for a specific product slot.
 * </p>
 * <p>
 * A vending machine has methods to get/set the quantity of each type of coin.
 * </p>
 * <p>
 * A vending machine has a method to purchase a specific "in stock" product using a list of input coins,
 * if the product exists in stock
 * AND the input coins are enough funds
 * AND there is sufficient coins for change inside the vending,
 * The result will be a List of the coins the user should get as change (Result will be an empty list if there is no change)
 * An IllegalStateException is returned when the vending machine cannot support the operation or IllegalArgumentException for invalid arguments.
 * </p>
 *
 * @author Jev Prentice
 * @since 08 December 2021
 */
@ToString
@Slf4j
final public class VendingMachine implements VendingMachineInventory, VendingMachineConsumer {

    private final int productSlots; // fixed number of product slots
    private final List<Product> products; // fixed number of editable products
    private final Map<Coin, Integer> coins; // fixed number of coin types, with quantity available

    private final Object MONITOR = new Object();

    public VendingMachine(final int productSlots, final List<Double> supportedCoinValues) {
        // Validate input
        if (productSlots <= 0) throw new IllegalArgumentException("Product slots must be a positive integer");
        if (supportedCoinValues == null || supportedCoinValues.isEmpty())
            throw new IllegalArgumentException("A vending machine must support at least one type of coin");

        // Prepare products
        this.productSlots = productSlots;
        this.products = IntStream.range(0, productSlots)
                .mapToObj(i -> new Product(0, 0))
                .collect(Collectors.toList());

        // Prepare coins
        this.coins = supportedCoinValues.stream()
                .map(Coin::doubleToCoin)
                .collect(Collectors.toMap(c -> c, c -> 0));
    }

    private void validateProductIndex(final int index) {
        if (index < 0)
            throw new IllegalStateException("The index must be a positive integer or 0 but %s was provided"
                    .formatted(index));
        if (index > productSlots)
            throw new IllegalStateException("The index %s is larger than the max product slots of %s"
                    .formatted(index, products));
    }

    private Product getProductOrThrow(final int index) {
        validateProductIndex(index);
        final Product current = this.products.get(index);
        if (current == null) throw new IllegalStateException("There is no product in slot %s".formatted(index));
        return current;
    }

    @Override
    public void setProductQuantity(final int index, final int newQuantity) {
        synchronized (MONITOR) {
            if (newQuantity < 0)
                throw new IllegalArgumentException("A product quantity must be a positive integer or 0");
            final Product current = getProductOrThrow(index);
            current.setQuantity(newQuantity);
        }
    }

    @Override
    public int getProductQuantity(final int index) {
        return getProductOrThrow(index).getQuantity();
    }

    @Override
    public void setProductPrice(final int index, final int newPrice) {
        synchronized (MONITOR) {
            if (newPrice <= 0) throw new IllegalArgumentException("A product price must br a positive integer");
            final Product current = getProductOrThrow(index);
            current.setPrice(newPrice);
        }
    }

    @Override
    public int getProductPrice(final int index) {
        return getProductOrThrow(index).getPrice();
    }

    @Override
    public void setCoinQuantity(final Coin coin, final int newQuantity) {
        synchronized (MONITOR) {
            coins.put(coin, newQuantity);
        }
    }

    @Override
    public int getCoinQuantity(final Coin coin) {
        return coins.get(coin);
    }

    @Override
    public List<Coin> purchaseProduct(final int index, final List<Coin> coins) {
        synchronized (MONITOR) {
            final Product product = getProductOrThrow(index);

            log.info("Purchase {} @ index {} using {} coins. Current coins: {}",
                    product, index, coins, this.coins);

            if (product.getQuantity() <= 0)
                throw new IllegalStateException("Product quantity is less than or equal 0");

            if (product.getPrice() <= 0)
                throw new IllegalStateException("Product price is less than or equal 0");

            final List<Coin> change = determineChange(product.getPrice(), coins);

            setProductQuantity(index, product.getQuantity() - 1);

            return change;
        }
    }

    /**
     * To determine the correct amount of change for the user first the total incoming coins are compared with the
     * price of the product.
     * If there is "no change" to return an empty list is returned,
     * If there is "not enough" money provided IllegalArgumentException is thrown,
     * If "too much" money is provided the vending machine provides change to the customer by returning a list of
     * coins and when this happens the internal count of available coins is modified.
     * <p>
     * A note on returning coins as change:
     * A vending machine will try to return change starting with the largest coin denominations first collecting
     * as many as possible of each of the coin denomination in a map called coinsToReturnAsChange which is tallied
     * after iterating through the available coins.
     * </p>
     *
     * @param price         The price of the product
     * @param incomingCoins The incoming coins to pay for the product.
     * @return A list containing the coins to be returned as change if the transaction was successful.
     */
    private List<Coin> determineChange(int price, final List<Coin> incomingCoins) {

        final Integer incomingCoinsSum = incomingCoins.stream()
                .map(Coin::getValueInPence)
                .reduce(0, Integer::sum);
        int priceMinusIncomingCoins = price - incomingCoinsSum;

        if (priceMinusIncomingCoins == 0) {
            incomingCoins.forEach(ic -> this.coins.put(ic, this.coins.get(ic) + 1));
            return Collections.emptyList(); // There is no change to return.
        }

        if (priceMinusIncomingCoins > 0)
            throw new IllegalArgumentException(
                    "Not enough money, you need to pay %s pence for this product but have input %s. Cancelling transaction."
                            .formatted(priceMinusIncomingCoins, incomingCoinsSum));

        int totalChangeToReturn = priceMinusIncomingCoins * -1;

        final Map<Coin, Integer> availableCoinQuantityMap = new HashMap<>(this.coins);
        incomingCoins.forEach(ic -> availableCoinQuantityMap.put(ic, availableCoinQuantityMap.get(ic) + 1));

        final Map<Coin, Integer> coinsToReturnAsChange = new HashMap<>(); // Map containing how many of each coin should be returned to the user as change.

        boolean isFinishedLooping = false;

        // Sort available petty cash (the coins already inside the vending machine) desc according to their denomination, filter out missing coins.
        // Iterate the (sorted) available coins to collect the ones to be returned as change.
        final List<Coin> availableCoins = availableCoinQuantityMap.entrySet()
                .stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparingInt(Coin::getValueInPence).reversed())
                .collect(Collectors.toList());

        for (final Coin largestConsideredCoin : availableCoins) {
            if (isFinishedLooping) break;

            for (final Coin availableCoin : availableCoins.stream()
                    .filter(c -> c.getValueInPence() <= largestConsideredCoin.getValueInPence())
                    .collect(Collectors.toList())) {

                if (totalChangeToReturn == 0) // Stop looping when total change to return reaches 0.
                    break;

                final int availableValueInPence = availableCoin.getValueInPence();

                // The number of this specific (available) coin to return to the customer.
                final int nrOfCoinToReturn = totalChangeToReturn / availableValueInPence;

                final Integer quantityOfAvailableCoin = availableCoinQuantityMap.get(availableCoin);

                int remainder = totalChangeToReturn % availableValueInPence;

                if (quantityOfAvailableCoin >= nrOfCoinToReturn) {
                    // All coins of this denomination are available, remember the nr of coins to return.
                    coinsToReturnAsChange.put(availableCoin, nrOfCoinToReturn);
                } else {
                    // Not enough coins of this denomination are available, remember the max quantity of available coin to return.
                    coinsToReturnAsChange.put(availableCoin, quantityOfAvailableCoin);
                    // Keep track of the total that has not been returned yet
                    remainder += (nrOfCoinToReturn - quantityOfAvailableCoin) * availableValueInPence;
                }

                totalChangeToReturn = remainder;
            }

            if (totalChangeToReturn == 0) {
                isFinishedLooping = true;
            } else {
                coinsToReturnAsChange.clear();
                totalChangeToReturn = priceMinusIncomingCoins * -1;
            }
        }

        if (totalChangeToReturn != 0)
            throw new IllegalStateException("The vending machine does not have the coin denominations in stock to return the correct change.");

        // Decrease the quantity of available coins by the "remembered values".
        coinsToReturnAsChange.forEach((k, v) -> this.coins.put(k, this.coins.get(k) - v));

        incomingCoins.forEach(ic -> this.coins.put(ic, this.coins.get(ic) + 1));

        return createChangeCoinList(coinsToReturnAsChange);
    }

    private List<Coin> createChangeCoinList(final Map<Coin, Integer> map) {
        final List<Coin> result = new ArrayList<>();
        map.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .forEach(e -> IntStream.range(0, e.getValue())
                        .mapToObj(i -> e.getKey())
                        .forEach(result::add));
        return result;
    }

    public List<Product> getProducts() {
        return products;
    }
}
