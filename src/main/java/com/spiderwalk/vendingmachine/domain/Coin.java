package com.spiderwalk.vendingmachine.domain;

/**
 * Enum to represent the coin denominations from the UK royal mint. Coins can be used to buy products stored in a
 * vending machine and are produced as change after a successful transaction.
 *
 * @author Jev Prentice
 * @since 08 December 2021
 */
public enum Coin {

    OneP(1),
    TwoP(2),
    FiveP(5),
    TenP(10),
    TwentyP(20),
    FiftyP(50),
    OnePound(100),
    TwoPound(200),
    FivePound(500);

    private final int valueInPence;

    /**
     * @param valueInPence See {@link #getValueInPence()}
     */
    Coin(final int valueInPence) {
        this.valueInPence = valueInPence;
    }

    /**
     * @return The value of the coin in pence.
     */
    public int getValueInPence() {
        if (valueInPence == 0) throw new IllegalStateException("A coin cannot have 0 value.");
        return valueInPence;
    }

    /**
     * Utility method to convert from a double to a coin.
     *
     * @param d The double to convert.
     * @return The converted coin.
     */
    public static Coin doubleToCoin(final double d) {
        if (Double.compare(d, 0.01) == 0) {
            return OneP;
        } else if (Double.compare(d, 0.02) == 0) {
            return TwoP;
        } else if (Double.compare(d, 0.05) == 0) {
            return FiveP;
        } else if (Double.compare(d, 0.1) == 0) {
            return TenP;
        } else if (Double.compare(d, 0.2) == 0) {
            return TwentyP;
        } else if (Double.compare(d, 0.5) == 0) {
            return FiftyP;
        } else if (Double.compare(d, 1.0) == 0) {
            return OnePound;
        } else if (Double.compare(d, 2.0) == 0) {
            return TwoPound;
        } else if (Double.compare(d, 5.0) == 0) {
            return FivePound;
        }
        throw new IllegalArgumentException();
    }
}
