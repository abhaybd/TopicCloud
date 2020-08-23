package com.coolioasjulio.topiccloud;

import twitter4j.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TwitterAPI {
    public static void main(String[] args) {
        try {
            List<Status> list = getRecentTweets("@GovInslee", 10);
            for (Status s : list) {
                System.out.printf("%s - %s\n\n", s.getCreatedAt(), s.getText().replaceAll("\\s+", " "));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public static List<Status> getRecentTweets(String name, int numTweets) throws TwitterException {
        Twitter twitter = TwitterFactory.getSingleton();
        Paging p = new Paging(1, numTweets);
        return twitter.getUserTimeline(name, p);
    }

    public static List<String> getRecentTweetsSanitized(String name, int numTweets) throws TwitterException {
        List<Status> statuses = getRecentTweets(name, numTweets);
        return statuses.stream().map(TwitterAPI::sanitize).collect(Collectors.toList());
    }

    private static String sanitize(Status status) {
        String text = status.getText();
        return text
                .replaceAll("\\s+", " ")
                .replaceAll("https?://\\S+", "")
                .trim();
    }
}
