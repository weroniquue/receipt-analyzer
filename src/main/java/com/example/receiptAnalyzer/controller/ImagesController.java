package com.example.receiptAnalyzer.controller;

import com.example.receiptAnalyzer.ReceiptItem;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Vertex;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class ImagesController {

    ResourceLoader resourceLoader;
    private CloudVisionTemplate cloudVisionTemplate;
    private Gson gson = new GsonBuilder().create();

    @RequestMapping("/getTextDetection")
    public List<List<String>> getTextDetection() {
        Resource imageResource = this.resourceLoader.getResource("file:src/main/resources/lidl.png");
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                imageResource, Feature.Type.DOCUMENT_TEXT_DETECTION);

        List<ReceiptItem> list = response.getTextAnnotationsList().stream()
                .map(this::mapItem).collect(Collectors.toList());

//        List<ReceiptItem> list = gson.fromJson("[{\"text\":\"Lidl sp. z 0.0. sp. k.\\nMoniuszki 2\\n59-220 Legnica\\nNIP 781-18-97-358\\n2018-09-03\\nnr wydr. 1062127\\nPARAGON FISKALNY\\nBUŁKA GRYCZANA ŚW. I 2 * 0,89 1,78 B\\nPAPRYKARZ SZCZECIN. F 2 * 1,99 3,98 C\\nBATON ZBOŻ.LION4X25 F 1 * 3,99 3,99 C\\nKIEŁKI SORT,\\nF 1 * 3,99 3,99 C\\nSprzed. opod. PTU B\\n1,78\\nKwota B 08,00%\\n0,13\\nSprzed, opod. PTU C\\n11,96\\nKwota C 05,00%\\n0,57\\nPodatek PTU\\n0,70\\nSUMA PLN\\n13,74\\n000088 #1 19 1029 nr:637033 08:08\\n3B8U-RFPV0-G480W-CF68H-DDFOE\\n2 BGI 14143317\\nPłatność\\nKarta płatnicza 13\\nRAZEM PLN\\n13\\nDziękujemy za zakupy w LID\\nlanlik\\nho\\n11\\n\",\"meanX\":413.0,\"meanY\":588.0},{\"text\":\"Lidl\",\"meanX\":278.0,\"meanY\":21.0},{\"text\":\"sp\",\"meanX\":347.0,\"meanY\":21.0},{\"text\":\".\",\"meanX\":377.0,\"meanY\":20.0},{\"text\":\"z\",\"meanX\":413.0,\"meanY\":20.0},{\"text\":\"0.0\",\"meanX\":465.0,\"meanY\":19.0},{\"text\":\".\",\"meanX\":503.0,\"meanY\":19.0},{\"text\":\"sp\",\"meanX\":549.0,\"meanY\":19.0},{\"text\":\".\",\"meanX\":577.0,\"meanY\":18.0},{\"text\":\"k\",\"meanX\":611.0,\"meanY\":18.0},{\"text\":\".\",\"meanX\":637.0,\"meanY\":17.0},{\"text\":\"Moniuszki\",\"meanX\":414.0,\"meanY\":66.0},{\"text\":\"2\",\"meanX\":525.0,\"meanY\":64.0},{\"text\":\"59-220\",\"meanX\":365.0,\"meanY\":115.0},{\"text\":\"Legnica\",\"meanX\":502.0,\"meanY\":114.0},{\"text\":\"NIP\",\"meanX\":283.0,\"meanY\":204.0},{\"text\":\"781-18-97-358\",\"meanX\":446.0,\"meanY\":204.0},{\"text\":\"2018-09-03\",\"meanX\":133.0,\"meanY\":252.0},{\"text\":\"nr\",\"meanX\":546.0,\"meanY\":251.0},{\"text\":\"wydr\",\"meanX\":619.0,\"meanY\":248.0},{\"text\":\".\",\"meanX\":666.0,\"meanY\":246.0},{\"text\":\"1062127\",\"meanX\":741.0,\"meanY\":243.0},{\"text\":\"PARAGON\",\"meanX\":335.0,\"meanY\":295.0},{\"text\":\"FISKALNY\",\"meanX\":490.0,\"meanY\":294.0},{\"text\":\"BUŁKA\",\"meanX\":83.0,\"meanY\":340.0},{\"text\":\"GRYCZANA\",\"meanX\":218.0,\"meanY\":339.0},{\"text\":\"ŚW\",\"meanX\":326.0,\"meanY\":339.0},{\"text\":\".\",\"meanX\":358.0,\"meanY\":339.0},{\"text\":\"I\",\"meanX\":468.0,\"meanY\":338.0},{\"text\":\"2\",\"meanX\":535.0,\"meanY\":338.0},{\"text\":\"*\",\"meanX\":569.0,\"meanY\":338.0},{\"text\":\"0,89\",\"meanX\":636.0,\"meanY\":338.0},{\"text\":\"1,78\",\"meanX\":730.0,\"meanY\":337.0},{\"text\":\"B\",\"meanX\":794.0,\"meanY\":337.0},{\"text\":\"PAPRYKARZ\",\"meanX\":116.0,\"meanY\":384.0},{\"text\":\"SZCZECIN\",\"meanX\":291.0,\"meanY\":384.0},{\"text\":\".\",\"meanX\":376.0,\"meanY\":384.0},{\"text\":\"F\",\"meanX\":464.0,\"meanY\":384.0},{\"text\":\"2\",\"meanX\":533.0,\"meanY\":384.0},{\"text\":\"*\",\"meanX\":567.0,\"meanY\":384.0},{\"text\":\"1,99\",\"meanX\":636.0,\"meanY\":384.0},{\"text\":\"3,98\",\"meanX\":727.0,\"meanY\":384.0},{\"text\":\"C\",\"meanX\":787.0,\"meanY\":384.0},{\"text\":\"BATON\",\"meanX\":78.0,\"meanY\":430.0},{\"text\":\"ZBOŻ.LION4X25\",\"meanX\":261.0,\"meanY\":430.0},{\"text\":\"F\",\"meanX\":459.0,\"meanY\":430.0},{\"text\":\"1\",\"meanX\":535.0,\"meanY\":430.0},{\"text\":\"*\",\"meanX\":566.0,\"meanY\":430.0},{\"text\":\"3,99\",\"meanX\":634.0,\"meanY\":430.0},{\"text\":\"3,99\",\"meanX\":726.0,\"meanY\":430.0},{\"text\":\"C\",\"meanX\":785.0,\"meanY\":430.0},{\"text\":\"KIEŁKI\",\"meanX\":85.0,\"meanY\":471.0},{\"text\":\"SORT\",\"meanX\":198.0,\"meanY\":472.0},{\"text\":\",\",\"meanX\":241.0,\"meanY\":472.0},{\"text\":\"F\",\"meanX\":456.0,\"meanY\":475.0},{\"text\":\"1\",\"meanX\":533.0,\"meanY\":475.0},{\"text\":\"*\",\"meanX\":567.0,\"meanY\":475.0},{\"text\":\"3,99\",\"meanX\":631.0,\"meanY\":475.0},{\"text\":\"3,99\",\"meanX\":724.0,\"meanY\":475.0},{\"text\":\"C\",\"meanX\":783.0,\"meanY\":475.0},{\"text\":\"Sprzed\",\"meanX\":83.0,\"meanY\":561.0},{\"text\":\".\",\"meanX\":147.0,\"meanY\":561.0},{\"text\":\"opod\",\"meanX\":211.0,\"meanY\":561.0},{\"text\":\".\",\"meanX\":257.0,\"meanY\":561.0},{\"text\":\"PTU\",\"meanX\":309.0,\"meanY\":561.0},{\"text\":\"B\",\"meanX\":363.0,\"meanY\":561.0},{\"text\":\"1,78\",\"meanX\":716.0,\"meanY\":563.0},{\"text\":\"Kwota\",\"meanX\":70.0,\"meanY\":611.0},{\"text\":\"B\",\"meanX\":147.0,\"meanY\":610.0},{\"text\":\"08,00\",\"meanX\":217.0,\"meanY\":610.0},{\"text\":\"%\",\"meanX\":272.0,\"meanY\":610.0},{\"text\":\"0,13\",\"meanX\":717.0,\"meanY\":606.0},{\"text\":\"Sprzed\",\"meanX\":81.0,\"meanY\":655.0},{\"text\":\",\",\"meanX\":143.0,\"meanY\":655.0},{\"text\":\"opod\",\"meanX\":204.0,\"meanY\":654.0},{\"text\":\".\",\"meanX\":253.0,\"meanY\":654.0},{\"text\":\"PTU\",\"meanX\":300.0,\"meanY\":653.0},{\"text\":\"C\",\"meanX\":357.0,\"meanY\":653.0},{\"text\":\"11,96\",\"meanX\":705.0,\"meanY\":648.0},{\"text\":\"Kwota\",\"meanX\":71.0,\"meanY\":697.0},{\"text\":\"C\",\"meanX\":143.0,\"meanY\":697.0},{\"text\":\"05,00\",\"meanX\":211.0,\"meanY\":697.0},{\"text\":\"%\",\"meanX\":267.0,\"meanY\":697.0},{\"text\":\"0,57\",\"meanX\":711.0,\"meanY\":693.0},{\"text\":\"Podatek\",\"meanX\":86.0,\"meanY\":743.0},{\"text\":\"PTU\",\"meanX\":193.0,\"meanY\":743.0},{\"text\":\"0,70\",\"meanX\":712.0,\"meanY\":738.0},{\"text\":\"SUMA\",\"meanX\":99.0,\"meanY\":803.0},{\"text\":\"PLN\",\"meanX\":254.0,\"meanY\":804.0},{\"text\":\"13,74\",\"meanX\":702.0,\"meanY\":802.0},{\"text\":\"000088\",\"meanX\":80.0,\"meanY\":860.0},{\"text\":\"#\",\"meanX\":158.0,\"meanY\":860.0},{\"text\":\"1\",\"meanX\":177.0,\"meanY\":860.0},{\"text\":\"19\",\"meanX\":278.0,\"meanY\":860.0},{\"text\":\"1029\",\"meanX\":353.0,\"meanY\":860.0},{\"text\":\"nr\",\"meanX\":477.0,\"meanY\":860.0},{\"text\":\":\",\"meanX\":506.0,\"meanY\":860.0},{\"text\":\"637033\",\"meanX\":567.0,\"meanY\":860.0},{\"text\":\"08:08\",\"meanX\":741.0,\"meanY\":860.0},{\"text\":\"3B8U\",\"meanX\":167.0,\"meanY\":904.0},{\"text\":\"-\",\"meanX\":230.0,\"meanY\":904.0},{\"text\":\"RFPV0\",\"meanX\":280.0,\"meanY\":904.0},{\"text\":\"-\",\"meanX\":335.0,\"meanY\":904.0},{\"text\":\"G480W\",\"meanX\":388.0,\"meanY\":904.0},{\"text\":\"-\",\"meanX\":449.0,\"meanY\":904.0},{\"text\":\"CF68H\",\"meanX\":500.0,\"meanY\":904.0},{\"text\":\"-\",\"meanX\":557.0,\"meanY\":904.0},{\"text\":\"DDFOE\",\"meanX\":614.0,\"meanY\":904.0},{\"text\":\"2\",\"meanX\":285.0,\"meanY\":950.0},{\"text\":\"BGI\",\"meanX\":362.0,\"meanY\":950.0},{\"text\":\"14143317\",\"meanX\":482.0,\"meanY\":950.0},{\"text\":\"Płatność\",\"meanX\":100.0,\"meanY\":988.0},{\"text\":\"Karta\",\"meanX\":445.0,\"meanY\":995.0},{\"text\":\"płatnicza\",\"meanX\":593.0,\"meanY\":995.0},{\"text\":\"13\",\"meanX\":714.0,\"meanY\":994.0},{\"text\":\"RAZEM\",\"meanX\":108.0,\"meanY\":1041.0},{\"text\":\"PLN\",\"meanX\":290.0,\"meanY\":1044.0},{\"text\":\"13\",\"meanX\":637.0,\"meanY\":1036.0},{\"text\":\"Dziękujemy\",\"meanX\":238.0,\"meanY\":1126.0},{\"text\":\"za\",\"meanX\":366.0,\"meanY\":1125.0},{\"text\":\"zakupy\",\"meanX\":454.0,\"meanY\":1125.0},{\"text\":\"w\",\"meanX\":532.0,\"meanY\":1124.0},{\"text\":\"LID\",\"meanX\":589.0,\"meanY\":1124.0},{\"text\":\"lanlik\",\"meanX\":548.0,\"meanY\":1161.0},{\"text\":\"ho\",\"meanX\":192.0,\"meanY\":1162.0},{\"text\":\"11\",\"meanX\":401.0,\"meanY\":1161.0}]",
//                new TypeToken<ArrayList<ReceiptItem>>() {
//                }.getType());

        ArrayList<ReceiptItem> copyList = new ArrayList<>(list);
        List<List<String>> newList = new ArrayList<>();

        for (ReceiptItem item : list) {
            List<ReceiptItem> itemsToRemove = groupTextByLine(item, copyList);
            if (!itemsToRemove.isEmpty()) {
                newList.add(itemsToRemove.stream().map(ReceiptItem::getText).collect(Collectors.toList()));
            }
            copyList.removeAll(itemsToRemove);
        }
        return newList;
    }

    private List<ReceiptItem> groupTextByLine(ReceiptItem receiptItem, List<ReceiptItem> list) {
        return list.stream().filter(item -> Math.abs(item.getMeanY() - receiptItem.getMeanY()) < 15)
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
