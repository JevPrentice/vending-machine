package com.spiderwalk.vendingmachine.api;

import com.spiderwalk.vendingmachine.VendingMachine;
import com.spiderwalk.vendingmachine.domain.Coin;
import com.spiderwalk.vendingmachine.domain.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jev Prentice
 * @since 13 December 2021
 */
@RestController
public class VendingMachineController {

    private final static VendingMachine VENDING_MACHINE = new VendingMachine(10,
            List.of(0.01d, 0.02d, 0.05d, 0.10d, 0.20d, 0.50, 1.0, 2.0, 5.0));

    @GetMapping("/")
    public VendingMachine getVendingMachine() {
        return VENDING_MACHINE;
    }

    @GetMapping("/product")
    public List<Product> getAllProducts() {
        return VENDING_MACHINE.getProducts();
    }

    @GetMapping("/product/{index}")
    public Product getProductByProductSlot(@PathVariable final Integer index) {
        return VENDING_MACHINE.getProductCopy(index);
    }

    @PostMapping("/product/{index}/quantity/{quantity}")
    public void setProductQuantity(@PathVariable final Integer index,
                                   @PathVariable final Integer quantity) {
        VENDING_MACHINE.setProductQuantity(index, quantity);
    }

    @PostMapping("/product/{index}/price/{price}")
    public void setProductPrice(@PathVariable final Integer index,
                                @PathVariable final Integer price) {
        VENDING_MACHINE.setProductPrice(index, price);
    }

    @GetMapping("/coin")
    public Map<Coin, Integer> getCoins() {
        return VENDING_MACHINE.getCoins();
    }

    @PostMapping("/coin/{coin}/quantity/{quantity}")
    public void setCoinQuantity(@PathVariable final Double coin,
                                @PathVariable final Integer quantity) {
        VENDING_MACHINE.setCoinQuantity(Coin.doubleToCoin(coin), quantity);
    }

    @PostMapping("/coin/all/quantity/{quantity}")
    public void initDefaultCoins(@PathVariable final Integer quantity) {
        Stream.of(Coin.values()).forEach(c -> VENDING_MACHINE.setCoinQuantity(c, quantity));
    }

    @PostMapping("/product/{index}/purchase")
    public List<Coin> purchaseProduct(@PathVariable final int index,
                                      @RequestBody final List<Double> coins) {
        return VENDING_MACHINE.purchaseProduct(index,
                coins.stream().map(Coin::doubleToCoin).collect(Collectors.toList()));
    }
}
