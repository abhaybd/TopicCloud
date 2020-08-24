package com.coolioasjulio.topiccloud;

import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.*;

public class Playground {
    public static void main(String[] args) throws TwitterException {
        String user = "@GovInslee";
        List<Status> tweets = TwitterAPI.getInstance().getRecentTweets(user, 100);
        List<Word> words = new ArrayList<>(LanguageAPI.getInstance().getEntitiesAndIds(tweets, 0.08).keySet());

        words.sort(((Comparator<Word>) Word::compareTo).reversed());

        for (int i = 0; i < words.size(); i++) {
            Word word = words.get(i);
            System.out.printf("%02d. %s - %s\n", i+1, word.text, word.value);
        }
    }
}
