package com.coolioasjulio.topiccloud;

import twitter4j.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}
