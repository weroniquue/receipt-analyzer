package com.example.receiptAnalyzer.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class Product {
    private String name;
    private float quantity;
    private float price;
    private float totalPrice;
}
