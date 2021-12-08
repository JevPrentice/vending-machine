package com.spiderwalk.vendingmachine;

import com.spiderwalk.vendingmachine.domain.Coin;

/**
 * An interface containing methods to maintain the internal state of a vending machines inventory.
 *
 * @author Jev Prentice
 * @since 08 December 2021
 */
public interface VendingMachineInventory {

    /**
     * Sets the quantity for an existing product.
     *
     * @param index       The product item index.
     * @param newQuantity The new quantity.
     * @throws IllegalArgumentException If the new quantity is invalid.
     * @throws IllegalStateException    If the product does not exist or no product exists for the provided index.
     */
    void setProductQuantity(int index, int newQuantity);

    /**
     * Gets a product quantity by index.
     *
     * @param index The product item index.
     * @return The quantity of the product, defaults to 0.
     * @throws IndexOutOfBoundsException If the internal product list is less than the provided index.
     * @throws IllegalStateException     If the product does not exist.
     */
    int getProductQuantity(int index);

    /**
     * Sets the price for a product.
     *
     * @param index    The product item index.
     * @param newValue The new value for the product price.
     * @throws IllegalArgumentException  If the new value is invalid.
     * @throws IndexOutOfBoundsException If no element exists at the index
     */
    void setProductPrice(int index, int newValue);

    /**
     * Get the price for a product.
     *
     * @param index The product item index.
     * @return The price of the product.
     */
    int getProductPrice(int index);

    /**
     * Set the value of the quantity of a particular coin
     *
     * @param coin        The coin
     * @param newQuantity The new quantity for the coin.
     */
    void setCoinQuantity(Coin coin, int newQuantity);

    /**
     * Get the quantity of a particular coin
     *
     * @param coin The coin.
     * @return The quantity of the coin.
     */
    int getCoinQuantity(Coin coin);
}
