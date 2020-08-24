package com.coolioasjulio.topiccloud;

import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.*;

public class Playground {
    public static void main(String[] args) throws TwitterException {
        String user = "@GovInslee";
        List<Status> tweets = TwitterAPI.getInstance().getRecentTweets(user, 100);
        List<Topic> topics = LanguageAPI.getInstance().getTopics(tweets, 0.08);

        topics.sort(((Comparator<Topic>) Topic::compareTo).reversed());

        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            System.out.printf("%02d. %s - %s\n", i+1, topic.text, topic.value);
        }
    }
}
