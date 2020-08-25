package com.coolioasjulio.topiccloud;

import com.google.gson.Gson;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class TwitterAPI {
    public static void main(String[] args) {
        try {
            List<Status> list = TwitterAPI.getSingleton().getRecentTweets("@GovInslee", 10);
            for (Status s : list) {
                System.out.printf("%s - %s\n\n", s.getCreatedAt(), s.getText().replaceAll("\\s+", " "));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private static final Object instanceLock = new Object();
    private static volatile TwitterAPI instance;

    public static TwitterAPI getSingleton() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = createNew(true);
                    try {
                        instance.client.getOAuth2Token();
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return instance;
    }

    public static TwitterAPI createNew() {
        return createNew(false);
    }

    public static TwitterAPI createNew(boolean appOnlyAuth) {
        Gson gson = new Gson();
        try (Reader reader = new InputStreamReader(TwitterAPI.class.getResourceAsStream("/twitter.json"))) {
            TwitterConf conf = gson.fromJson(reader, TwitterConf.class);
            Configuration c = new ConfigurationBuilder()
                    .setApplicationOnlyAuthEnabled(appOnlyAuth)
                    .setOAuthConsumerKey(conf.consumerKey)
                    .setOAuthConsumerSecret(conf.consumerSecret)
                    .build();
            Twitter client = new TwitterFactory(c).getInstance();
            return new TwitterAPI(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sanitizedContent(Status status) {
        String text = status.getText();
        return text
                .replaceAll("\\s+", " ")
                .replaceAll("https?://\\S+", "")
                .replaceAll("RT @\\S+", "")
                .trim();
    }

    public final Twitter client;

    public TwitterAPI(Twitter client) {
        this.client = client;
    }

    public List<String> getNameSuggestions(String name, int numSuggestions) throws TwitterException {
        ResponseList<User> responseList = client.searchUsers(name, 1);
        int len = Math.min(responseList.size(), numSuggestions);
        return responseList.stream().map(User::getScreenName).limit(len).collect(Collectors.toList());
    }

    public String getProfilePicURL(String name) throws TwitterException {
        return client.users().showUser(name).get400x400ProfileImageURLHttps();
    }

    public List<Status> getRecentTweets(String name, int numTweets) throws TwitterException {
        Paging p = new Paging(1, numTweets);
        return client.getUserTimeline(name, p);
    }

    public List<String> getRecentTweetsSanitized(String name, int numTweets) throws TwitterException {
        List<Status> statuses = getRecentTweets(name, numTweets);
        return statuses.stream().map(TwitterAPI::sanitizedContent).collect(Collectors.toList());
    }

    private static class TwitterConf {
        public String consumerKey;
        public String consumerSecret;
    }
}
