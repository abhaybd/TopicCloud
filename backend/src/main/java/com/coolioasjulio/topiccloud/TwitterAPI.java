package com.coolioasjulio.topiccloud;

import twitter4j.*;

import java.util.*;
import java.util.stream.Collectors;

public class TwitterAPI {
    public static void main(String[] args) {

        try {
            List<Status> list = TwitterAPI.getInstance().getRecentTweets("@GovInslee", 10);
            for (Status s : list) {
                System.out.printf("%s - %s\n\n", s.getCreatedAt(), s.getText().replaceAll("\\s+", " "));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private static final Object instanceLock = new Object();
    private static TwitterAPI instance;

    public static TwitterAPI getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new TwitterAPI();
                }
            }
        }

        return instance;
    }

    private final Twitter client;

    public TwitterAPI() {
        client = TwitterFactory.getSingleton();
        try {
            client.getOAuth2Token();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public List<Status> getRecentTweets(String name, int numTweets) throws TwitterException {
        Paging p = new Paging(1, numTweets);
        return client.getUserTimeline(name, p);
    }

    public List<String> getRecentTweetsSanitized(String name, int numTweets) throws TwitterException {
        List<Status> statuses = getRecentTweets(name, numTweets);
        return statuses.stream().map(this::sanitize).collect(Collectors.toList());
    }

    private String sanitize(Status status) {
        String text = status.getText();
        return text
                .replaceAll("\\s+", " ")
                .replaceAll("https?://\\S+", "")
                .trim();
    }
}
