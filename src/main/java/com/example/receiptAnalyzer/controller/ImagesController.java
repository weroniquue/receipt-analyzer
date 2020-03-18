package com.example.receiptAnalyzer.controller;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class ImagesController {

    private CloudVisionTemplate cloudVisionTemplate;
    ResourceLoader resourceLoader;
    private Gson gson = new GsonBuilder().create();

    @RequestMapping("/getTextDetection")
    public String getTextDetection() {
        Resource imageResource = this.resourceLoader.getResource("file:src/main/resources/IMG_6203.jpg");
        AnnotateImageResponse response = this.cloudVisionTemplate.analyzeImage(
                imageResource, Feature.Type.DOCUMENT_TEXT_DETECTION);
        return response.getTextAnnotationsList().get(0).toString();
    }
}
