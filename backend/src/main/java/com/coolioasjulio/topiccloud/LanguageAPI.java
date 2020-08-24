package com.coolioasjulio.topiccloud;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.*;
import com.google.common.collect.Lists;
import twitter4j.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<Topic> getTopics(List<Status> tweets, double wordThreshold) {
        Map<ApiFuture<AnalyzeEntitySentimentResponse>, Long> futures = new HashMap<>();
        for (Status tweet : tweets) {
            String content = TwitterAPI.sanitizedContent(tweet);
            Document doc = Document.newBuilder().setContent(content).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeEntitySentimentRequest request = AnalyzeEntitySentimentRequest.newBuilder().setDocument(doc).build();
            ApiFuture<AnalyzeEntitySentimentResponse> future = client.analyzeEntitySentimentCallable().futureCall(request);
            futures.put(future, tweet.getId());
        }

        Map<String, List<Topic>> topicMap = new HashMap<>();
        Map<String, List<String>> topicTweetMap = new HashMap<>();
        try {
            for (ApiFuture<AnalyzeEntitySentimentResponse> future : futures.keySet()) {
                try {
                    AnalyzeEntitySentimentResponse response = future.get();
                    String id = Long.toUnsignedString(futures.get(future));
                    response.getEntitiesList().stream()
                            .filter(e -> e.getSalience() >= wordThreshold)
                            .forEach(e -> {
                                String word = e.getName();
                                topicMap.putIfAbsent(word, new ArrayList<>());
                                topicMap.get(word).add(new Topic(word, e.getSalience(), e.getSentiment().getScore()));

                                topicTweetMap.putIfAbsent(word, new ArrayList<>());
                                topicTweetMap.get(word).add(id);
                            });
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return topicMap.values()
                .stream()
                .map(words -> {
                    Topic topic = words.stream()
                            .reduce((cum, w) -> {
                                cum.value += w.value;
                                cum.score += w.score;
                                return cum;
                            })
                            .orElseThrow(IllegalStateException::new);
                    topic.score /= words.size();
                    topic.tweetIds = topicTweetMap.get(topic.text);
                    return topic;
                })
                .collect(Collectors.toList());
    }

    public List<Topic> getEntities(String text, double threshold) {
        Document doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
        AnalyzeEntitySentimentResponse response = client.analyzeEntitySentiment(doc);
        return response.getEntitiesList()
                .stream()
                .filter(e -> e.getSalience() > threshold)
                .map(e -> new Topic(e.getName(), e.getSalience(), e.getSentiment().getScore()))
                .collect(Collectors.toList());
    }
}
