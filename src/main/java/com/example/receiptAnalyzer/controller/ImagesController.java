package com.example.receiptAnalyzer.controller;

import com.example.receiptAnalyzer.ReceiptItem;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Vertex;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
public class ImagesController {

    private static final String START_PRODUCT_LIST = "PARAGON FISKALNY";
    private static final String END_PRODUCT_LIST = "Sprzed. opod";
    private static final String END_PRODUCT_LIST_2 = "Sprzedaż opodatk";
    private static final String TOTAL = "Suma";
    private static final String FLOAT_PRICE_PATTERN = "\\d+\\.\\d+";
    private static final String TEXT_PATTERN = "[AaĄąBbCcĆćDdEeĘęFfGgHhIiJjKkLlŁłMmNnŃńOoÓóPpRrSsŚśTtUuWwYyZzŹźŻż ./]*";
    private static final String QUANTITY_PATTERN = " \\d+ ";
    ResourceLoader resourceLoader;
    private CloudVisionTemplate cloudVisionTemplate;

    public static double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @PostMapping("/getTextDetection")
    public TextDetectionResponse getTextDetection(@Valid @RequestBody TextDetectionRequest request) {
        byte[] decodeImage = Base64.getDecoder().decode(request.getEncodedImage());
        ByteArrayResource imageResource = new ByteArrayResource(decodeImage);
//        Resource imageResource = this.resourceLoader.getResource("file:src/main/resources/rossm.jpg");

        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(imageResource, Feature.Type.TEXT_DETECTION);

        List<ReceiptItem> textAnnotationsList = response.getTextAnnotationsList().stream()
                .map(this::mapItem).collect(Collectors.toList());

        textAnnotationsList.remove(0);

        ArrayList<ReceiptItem> copyTextAnnotationsList = new ArrayList<>(textAnnotationsList);
        List<String> textReadLineByLine = new ArrayList<>();

        for (ReceiptItem item : textAnnotationsList) {
            List<ReceiptItem> itemsToRemove = groupTextByLine(item, copyTextAnnotationsList);
            if (!itemsToRemove.isEmpty()) {
                String joined = StringUtils.join(itemsToRemove.stream().map(ReceiptItem::getText).collect(Collectors.toList()), " ");
                String replacedComma = joined.replaceAll(",", ".").replaceAll(" . ", ".");
                textReadLineByLine.add(replacedComma);
            }
            copyTextAnnotationsList.removeAll(itemsToRemove);
        }
        List<Product> products = getProducts(textReadLineByLine.subList(1, textReadLineByLine.size()));
        double totalPriceFromList = round(products.stream().map(Product::getTotalPrice).mapToDouble(d -> d).sum());

        String totalPriceString = textReadLineByLine.stream()
                .filter(item -> StringUtils.containsIgnoreCase(item, TOTAL))
                .findFirst()
                .orElse(StringUtils.EMPTY);
        float totalPrice = convertToFloat(totalPriceString);

        TextDetectionResponse textDetectionResponse = TextDetectionResponse.builder()
                .products(products)
                .totalPriceFromList(totalPriceFromList)
                .readTotalPrice(totalPrice)
                .shopDetails(textReadLineByLine.subList(0, 6))
                .build();
        if (totalPriceFromList != totalPrice) {
            textDetectionResponse.setErrorMessage("Something went wrong during analyze. Check details on the next page");
        }
        return textDetectionResponse;
    }

    private List<String> findStringByPattern(String pattern, String string) {
        Pattern p = Pattern.compile(pattern);
        return p.matcher(string)
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
    }

    private List<Product> getProducts(List<String> textReadLineByLine) {
        String firstProduct = findTextInLine(textReadLineByLine, START_PRODUCT_LIST);
        String lastProduct = findTextInLine(textReadLineByLine, END_PRODUCT_LIST);
        if (lastProduct.isEmpty()) {
            lastProduct = findTextInLine(textReadLineByLine, END_PRODUCT_LIST_2);
        }
        int startPosition = textReadLineByLine.indexOf(firstProduct);
        int endPosition = textReadLineByLine.indexOf(lastProduct);
        if (startPosition == -1 || endPosition == -1) {
            return Collections.emptyList();
        }

        List<String> listWithoutEmptyLines = concatIfEmptyLines(textReadLineByLine.subList(startPosition + 1, endPosition));

        List<Product> listWithProducts = listWithoutEmptyLines.stream().map(item -> {
            List<String> foundPrices = findStringByPattern(FLOAT_PRICE_PATTERN, item);
            if (foundPrices.size() >= 2) {
                float totalPrice = convertToFloat(foundPrices.get(foundPrices.size() - 1));
                float price = convertToFloat(foundPrices.get(foundPrices.size() - 2));
                String text = findStringByPattern(TEXT_PATTERN, item)
                        .stream()
                        .findFirst()
                        .orElse(StringUtils.EMPTY);
                return Product.builder()
                        .totalPrice(totalPrice)
                        .price(price)
                        .name(text)
                        .quantity(totalPrice / price)
                        .build();
            }
            return Product.builder().build();
        }).collect(Collectors.toList());

        return listWithProducts.stream()
                .filter(item -> StringUtils.isNoneBlank(item.getName()))
                .filter(item -> StringUtils.containsIgnoreCase(item.getName(), "PTU"))
                .collect(Collectors.toList());
    }

    private List<String> concatIfEmptyLines(List<String> subList) {
        List<String> filteredList = subList.stream()
                .filter(item -> !StringUtils.containsIgnoreCase(item, "rabat"))
                .filter(item -> item.length() > 1)
                .collect(Collectors.toList());
        for (int i = 0; i < filteredList.size(); i++) {
            String text = findStringByPattern(TEXT_PATTERN, filteredList.get(i))
                    .stream()
                    .findFirst()
                    .orElse(StringUtils.EMPTY);
            if (StringUtils.isEmpty(text) && i > 0) {
                filteredList.set(i - 1, filteredList.get(i - 1) + filteredList.get(i));
                filteredList.remove(i);
            }
        }
        return filteredList;
    }

    private float convertToFloat(String number) {
        try {
            return Float.parseFloat(number);
        } catch (Exception e) {
            return 0;
        }
    }

    private String findTextInLine(List<String> textReadLineByLine, String searchString) {
        return textReadLineByLine.stream()
                .filter(line -> StringUtils.containsIgnoreCase(line, searchString))
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private List<ReceiptItem> groupTextByLine(ReceiptItem receiptItem, List<ReceiptItem> list) {
        return list.stream().filter(item -> Math.abs(item.getMeanY() - receiptItem.getMeanY()) < 50)
                .collect(Collectors.toList());
    }

    private ReceiptItem mapItem(EntityAnnotation item) {
        return ReceiptItem.builder()
                .text(new String(item.getDescriptionBytes().toByteArray(), StandardCharsets.UTF_8))
                .meanX(calculateMeanX(item.getBoundingPoly().getVerticesList()))
                .meanY(calculateMeanY(item.getBoundingPoly().getVerticesList()))
                .build();
    }

    private float calculateMeanY(List<Vertex> verticesList) {
        return verticesList.stream()
                .map(Vertex::getY)
                .mapToInt(Integer::intValue)
                .sum() / verticesList.size();
    }

    private float calculateMeanX(List<Vertex> verticesList) {
        return verticesList.stream()
                .map(Vertex::getX)
                .mapToInt(Integer::intValue)
                .sum() / verticesList.size();
    }
}
