package com.coolioasjulio.topiccloud;

import com.google.gson.Gson;
import org.apache.commons.lang3.time.DateUtils;
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
            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(2019, Calendar.DECEMBER, 25);
            Date date = cal.getTime();
            List<Status> list = TwitterAPI.getSingleton().getRecentTweets("@GovInslee", 500);
//            List<Status> list = TwitterAPI.getSingleton().getTweetsSince("@GovInslee", date);
            for (Status s : list) {
                System.out.printf("%s - %s\n\n", s.getCreatedAt(), s.getText().replaceAll("\\s+", " "));
            }
            System.out.println(list.size());
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private static final Object instanceLock = new Object();
    private static volatile TwitterAPI instance;
    private static final int MAX_PAGE_SIZE = 200;

    public static TwitterAPI getSingleton() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = createNew(true);
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
            if (appOnlyAuth) {
                client.getOAuth2Token();
            }
            return new TwitterAPI(client);
        } catch (IOException | TwitterException e) {
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

    public List<Status> getTweetsSince(String name, Date date) throws TwitterException {
        List<Status> tweets = new ArrayList<>();
        Long lowestSeenID = null;
        Date lowestSeenDate = new Date();
        int pageNum = 1;
        while (lowestSeenDate.after(date)) {
            Paging p = new Paging(pageNum++, MAX_PAGE_SIZE);
            if (lowestSeenID != null) {
                p.setMaxId(lowestSeenID - 1);
            }
            List<Status> page = client.getUserTimeline(name, p);
            int last = page.size() - 1;
            lowestSeenID = page.get(last).getId();
            lowestSeenDate = page.get(last).getCreatedAt();
            tweets.addAll(page);
        }

        return tweets;

//        int low = 0;
//        int high = tweets.size();
//        int mid;
//        do {
//            mid = (low + high) / 2;
//            int compare = DateUtils.truncatedCompareTo(date, tweets.get(mid).getCreatedAt(), Calendar.DATE);
//            if (compare == 0) {
//                break;
//            } else if (compare < 0) {
//                low = mid;
//            } else {
//                high = mid;
//            }
//        } while (low != mid);
//
//        while (mid < tweets.size() && DateUtils.truncatedCompareTo(date, tweets.get(mid).getCreatedAt(), Calendar.DATE) <= 0) mid++;
//
//        return tweets.subList(0, mid);
    }

    public List<Status> getRecentTweets(String name, int numTweets) throws TwitterException {
        List<Status> tweets = new ArrayList<>();
        Long lowestSeenID = null;
        for (int i = 0; i < numTweets; i += MAX_PAGE_SIZE) {
            int pageNum = 1 + i / MAX_PAGE_SIZE;
            int count = Math.min(numTweets - i, MAX_PAGE_SIZE);
            Paging p = new Paging(pageNum, count);
            if (lowestSeenID != null) {
                p.setMaxId(lowestSeenID - 1);
            }
            List<Status> page = client.getUserTimeline(name, p);
            lowestSeenID = page.get(page.size() - 1).getId();
            tweets.addAll(page);
        }

        return tweets;
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
