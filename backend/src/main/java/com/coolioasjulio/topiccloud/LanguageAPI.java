package com.coolioasjulio.topiccloud;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.*;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LanguageAPI {
    public static void main(String[] args) {
        // The text to analyze
        String text = "Kay Sakai Nakao was a bright light in the Bainbridge Island community, and an essential voice on the gross mistreatment of Japanese Americans during WWII. " +
                "Trudi and my thoughts are with her friends, family and all those she touched in her 100 years.";

        System.out.println(LanguageAPI.getInstance().getEntities(text, 0.08));
    }

    private static final Object instanceLock = new Object();
    private static LanguageAPI instance;

    public static LanguageAPI getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    try {
                        instance = new LanguageAPI();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return instance;
    }

    private final LanguageServiceClient client;

    private LanguageAPI() throws IOException {
        GoogleCredentials creds = GoogleCredentials.fromStream(LanguageAPI.class.getResourceAsStream("/auth.json"))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        CredentialsProvider provider = () -> creds;

        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().setCredentialsProvider(provider).build();
        client = LanguageServiceClient.create(settings);
    }

    public List<Word> getEntities(List<String> texts, double wordThreshold) {
        List<ApiFuture<AnalyzeEntitiesResponse>> futures = texts.stream()
                .map(s -> Document.newBuilder().setContent(s).setType(Document.Type.PLAIN_TEXT).build())
                .map(doc -> AnalyzeEntitiesRequest.newBuilder().setDocument(doc).build())
                .map(client.analyzeEntitiesCallable()::futureCall)
                .collect(Collectors.toList());

        HashMap<String, Word> map = new HashMap<>();
        try {
            for (ApiFuture<AnalyzeEntitiesResponse> future : futures) {
                try {
                    AnalyzeEntitiesResponse response = future.get();
                    response.getEntitiesList().stream()
                            .filter(e -> e.getSalience() >= wordThreshold)
                            .forEach(e -> {
                                if (map.containsKey(e.getName())) {
                                    map.put(e.getName(), new Word(e.getName(), map.get(e.getName()).value + e.getSalience()));
                                } else {
                                    map.put(e.getName(), new Word(e.getName(), e.getSalience()));
                                }
                            });
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>(map.values());
    }

    public List<Word> getEntities(String text, double threshold) {
        Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
        AnalyzeEntitiesResponse response = client.analyzeEntities(doc);
        return response.getEntitiesList()
                .stream()
                .filter(e -> e.getSalience() > threshold)
                .map(e -> new Word(e.getName(), e.getSalience()))
                .collect(Collectors.toList());
    }
}
