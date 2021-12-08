package com.spiderwalk.vendingmachine.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO class to hold values for a vending machine product. Product attributes are mutable.
 *
 * @author Jev Prentice
 * @since 08 December 2021
 */
@AllArgsConstructor
@Data
public class Product {
    private int quantity;
    private int price;
}
