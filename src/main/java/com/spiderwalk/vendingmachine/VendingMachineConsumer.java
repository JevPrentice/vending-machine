package com.spiderwalk.vendingmachine;

import com.spiderwalk.vendingmachine.domain.Coin;

import java.util.List;

/**
 * An interface containing methods to interact with a vending machine from the point of view of a consumer.
 * Has methods to fetch product price and to purchase a product.
 *
 * @author Jev Prentice
 * @since 08 December 2021
 */
public interface VendingMachineConsumer {

    /**
     * Retrieves the price of a stored product.
     *
     * @param index The product slot index.
     * @return The price of the product, defaults to 0.
     * @throws IllegalStateException If the product cannot be found for the index.
     */
    int getProductPrice(int index);

    /**
     * Reduces the internal state of the products 'quantity' for the product at the provided index by one.
     * Returns a list of coins that represents the change from the vending machines.
     * When this function is unable to process the request an exception will be thrown, in the case of an exception
     * assume that the coins provided are returned to the user and no internal state change occours.
     *
     * @param index The product slot index.
     * @param coins A list of coins being used to pay for the product.
     * @return A list of coins being returned by the vending machine to the consumer, the items in the list returns
     * would have existed inside the vending machines state prior to this call and are removed from the internal
     * state before being returned here.
     * @throws IllegalStateException When the product is not avail for purchase or when there are not enough coins provided.
     */
    List<Coin> purchaseProduct(int index, List<Coin> coins);
}
