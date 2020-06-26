package com.example.receiptAnalyzer.controller;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
public class ImagesController {

    private static final String START_PRODUCT_LIST = "PARAGON FISKALNY";
    private static final String FLOAT_PRICE_PATTERN = "\\d+(?:[\\.,]\\d+)?";
    ResourceLoader resourceLoader;
    private CloudVisionTemplate cloudVisionTemplate;

    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    @PostMapping("/get-text-detection")
    public TextDetectionResponse getTextDetection(@RequestBody TextDetectionRequest request) {
        byte[] decodeImage = Base64.getDecoder().decode(request.getEncodedImage());
        ByteArrayResource imageResource = new ByteArrayResource(decodeImage);
//        Resource imageResource = this.resourceLoader.getResource("file:src/main/resources/6535.jpg");

        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.DOCUMENT_TEXT_DETECTION);
        List<String> textAnnotationsList = Arrays.stream(response.getFullTextAnnotation().getText().replaceAll(",", ".").split("\n")).collect(Collectors.toList());
        int productStartIndex = textAnnotationsList.indexOf(getIndexOfStartProductList(textAnnotationsList));
        List<Product> products = textAnnotationsList.subList(productStartIndex, textAnnotationsList.size())
                .stream()
                .map(this::creteProduct)
                .filter(Objects::nonNull)
                .filter(item -> checkPriceEquality(item.getQuantity(), item.getPrice(), item.getTotalPrice()))
                .collect(Collectors.toList());

        return TextDetectionResponse.builder()
                .products(products)
                .totalPrice(0)
                .build();

    }

    private boolean checkPriceEquality(float quantity, float price, float totalPrice) {
        return round(quantity * price) == totalPrice;
    }

    private Product creteProduct(String line) {
        List<String> prices = getPricesList(line);
        if (prices.size() > 2) {
            return Product.builder()
                    .name(line.substring(0, line.indexOf(prices.get(prices.size() - 3))))
                    .price(convertToFloat(prices.get(prices.size() - 2)))
                    .totalPrice(convertToFloat(prices.get(prices.size() - 1)))
                    .quantity(convertToFloat(prices.get(prices.size() - 3)))
                    .build();
        }
        return null;
    }

    private float convertToFloat(String number) {
        try {
            return Float.parseFloat(number);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getIndexOfStartProductList(List<String> textAnnotationsList) {
        return textAnnotationsList.stream()
                .filter(item -> item.toUpperCase().contains(START_PRODUCT_LIST))
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private List<String> getPricesList(String line) {
        List<String> prices = new ArrayList<>();
        Matcher m = Pattern.compile(FLOAT_PRICE_PATTERN).matcher(line);
        while (m.find()) {
            prices.add(m.group());
        }
        return prices;
    }
}
