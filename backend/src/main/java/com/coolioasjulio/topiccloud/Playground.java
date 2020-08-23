package com.coolioasjulio.topiccloud;

import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.*;

public class Playground {
    public static void main(String[] args) throws TwitterException {
        String user = "@GovInslee";
        List<String> tweets = TwitterAPI.getRecentTweetsSanitized(user, 100);

        Map<String, Double> scoreMap = new HashMap<>();
        for (String text : tweets) {
            List<Word> words = LanguageAPI.getInstance().getEntities(text, 0.04);
            for (Word w : words) {
                scoreMap.putIfAbsent(w.text, 0.0);
                scoreMap.put(w.text, scoreMap.get(w.text) + w.value);
            }
        }

        List<String> words = new ArrayList<>(scoreMap.keySet());

        words.sort(Comparator.comparing(scoreMap::get).reversed());

        for (int i = 0; i < words.size(); i++) {
            System.out.printf("%02d. %s - %s\n", i, words.get(i), scoreMap.get(words.get(i)));
        }
    }
}
