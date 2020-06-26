package com.example.receiptAnalyzer.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class TextDetectionResponse {
    private List<Product> products;
    private List<String> shopDetails;
    private float totalPrice;
    private String errorMessage;
    private String date;
}
