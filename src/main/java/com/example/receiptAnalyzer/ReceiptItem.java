package com.example.receiptAnalyzer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class ReceiptItem {

    private String text;
    private float meanX;
    private float meanY;
}
