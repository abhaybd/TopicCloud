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
        List<ApiFuture<AnalyzeEntitySentimentResponse>> futures = texts.stream()
                .map(s -> Document.newBuilder().setContent(s).setType(Document.Type.PLAIN_TEXT).build())
                .map(doc -> AnalyzeEntitySentimentRequest.newBuilder().setDocument(doc).build())
                .map(client.analyzeEntitySentimentCallable()::futureCall)
                .collect(Collectors.toList());

        HashMap<String, List<Word>> map = new HashMap<>();
        try {
            for (ApiFuture<AnalyzeEntitySentimentResponse> future : futures) {
                try {
                    AnalyzeEntitySentimentResponse response = future.get();
                    response.getEntitiesList().stream()
                            .filter(e -> e.getSalience() >= wordThreshold)
                            .forEach(e -> {
                                map.putIfAbsent(e.getName(), new ArrayList<>());
                                map.get(e.getName()).add(new Word(e.getName(), e.getSalience(), e.getSentiment().getScore()));
                            });
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return map.values()
                .stream()
                .map(words -> {
                    Word word = words.stream()
                            .reduce((cum, w) -> {
                                cum.value += w.value;
                                cum.score += w.score;
                                return cum;
                            })
                            .orElseThrow(IllegalStateException::new);
                    return new Word(word.text, word.value, word.score / words.size());
                }).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Word> getEntities(String text, double threshold) {
        Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
        AnalyzeEntitySentimentResponse response = client.analyzeEntitySentiment(doc);
        return response.getEntitiesList()
                .stream()
                .filter(e -> e.getSalience() > threshold)
                .map(e -> new Word(e.getName(), e.getSalience(), e.getSentiment().getScore()))
                .collect(Collectors.toList());
    }
}
