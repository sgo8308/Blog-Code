package com.db.concurrencyproblem;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Product {

    int id;
    int stock;
    String name;
    int version;

    @Builder
    public Product(int id, int stock, String name, int version) {
        this.id = id;
        this.stock = stock;
        this.name = name;
        this.version = version;
    }

    public void subtractStock(int quantity) {
        this.stock -= quantity;
    }
}
