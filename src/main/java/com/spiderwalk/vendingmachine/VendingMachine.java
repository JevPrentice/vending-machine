package com.spiderwalk.vendingmachine;

import com.spiderwalk.vendingmachine.domain.Coin;
import com.spiderwalk.vendingmachine.domain.Product;
import lombok.ToString;

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
final public class VendingMachine implements VendingMachineInventory, VendingMachineConsumer {

    private final int productSlots; // fixed number of product slots
    private final List<Product> products; // fixed number of editable products
    private final Map<Coin, Integer> coins; // fixed number of coin types, with quantity available

    private final Object MONITOR = new Object();

    public VendingMachine(final int productSlots, final List<Double> supportedCoinValues) {
        // Validate input
        if (productSlots <= 0) throw new IllegalArgumentException();
        if (supportedCoinValues == null || supportedCoinValues.isEmpty()) throw new IllegalArgumentException();

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

    private Product getProductOrThrow(final int index) {
        if (index - 1 > productSlots)
            throw new IllegalStateException();
        final Product current = this.products.get(index);
        if (current == null) throw new IllegalStateException();
        return current;
    }

    @Override
    public void setProductQuantity(final int index, final int newQuantity) {
        synchronized (MONITOR) {
            if (newQuantity < 0) throw new IllegalArgumentException();
            final Product current = getProductOrThrow(index);
            current.setQuantity(newQuantity);
        }
    }

    @Override
    public int getProductQuantity(final int index) {
        if (index > productSlots) throw new IndexOutOfBoundsException();
        return getProductOrThrow(index).getQuantity();
    }

    @Override
    public void setProductPrice(final int index, final int newPrice) {
        synchronized (MONITOR) {
            if (newPrice <= 0) throw new IllegalArgumentException();
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

            if (product.getQuantity() <= 0 || product.getPrice() <= 0)
                throw new IllegalStateException();

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

        if (priceMinusIncomingCoins == 0)
            return Collections.emptyList(); // There is no change to return.

        if (priceMinusIncomingCoins > 0)
            throw new IllegalArgumentException(
                    "Not enough money, you need to pay %s pence for this product but have input %s. Cancelling transaction."
                            .formatted(priceMinusIncomingCoins, incomingCoinsSum));

        int totalChangeToReturn = priceMinusIncomingCoins * -1;

        final Map<Coin, Integer> coinsToReturnAsChange = new HashMap<>(); // Map containing how many of each coin should be returned to the user as change.

        // Sort available petty cash (the coins already inside the vending machine) desc according to their denomination, filter out missing coins.
        final List<Coin> availableCoins = this.coins.entrySet()
                .stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparingInt(Coin::getValueInPence).reversed())
                .collect(Collectors.toList());

        // Iterate the (sorted) available coins to collect the ones to be returned as change.
        for (final Coin availableCoin : availableCoins) {

            if (totalChangeToReturn == 0) // Stop looping when total change to return reaches 0.
                break;

            final int availableValueInPence = availableCoin.getValueInPence();
            if (availableValueInPence == 0) throw new IllegalStateException();

            // The number of this specific (available) coin to return to the customer.
            final int nrOfCoinToReturn = totalChangeToReturn / availableValueInPence;

            final Integer quantityOfAvailableCoin = this.coins.get(availableCoin);
            int remainder = totalChangeToReturn % availableValueInPence;

            if (quantityOfAvailableCoin >= nrOfCoinToReturn) {
                coinsToReturnAsChange.put(availableCoin, nrOfCoinToReturn); // All coins are avail, remember the nr of coins to return.
            } else {
                coinsToReturnAsChange.put(availableCoin, quantityOfAvailableCoin); // Not enough coins of this denomination are avail, remember the max quantity of available coin to return.
                remainder += (nrOfCoinToReturn - quantityOfAvailableCoin) * availableValueInPence; // Remember the remaining values
            }
            totalChangeToReturn = remainder;
        }

        if (totalChangeToReturn != 0)
            throw new IllegalStateException("The vending machine does not have the coin denominations in stock to return the correct change.");

        // Decrease the quantity of available coins by the "remembered values".
        coinsToReturnAsChange.forEach((k, v) -> this.coins.put(k, this.coins.get(k) - v));

        return coinsToReturnAsChange.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
