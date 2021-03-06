package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.LanguageAPI;
import com.coolioasjulio.topiccloud.Topic;
import com.coolioasjulio.topiccloud.TwitterAPI;
import twitter4j.Status;
import twitter4j.TwitterException;

import javax.servlet.ServletException;
import java.util.*;

public class KeywordServlet extends JSONTwitterServlet<KeywordServlet.Request, KeywordServlet.Response> {

    private static final double SALIENCE_THRESHOLD = 0.08;
    private static final int NUM_TWEETS = 500;

    public KeywordServlet() {
        super(Request.class);
    }

    @Override
    protected boolean validRequest(Request request) {
        return request != null && request.numKeywords != null && request.numKeywords > 0 && request.screenName != null;
    }

    @Override
    protected Response handleRequest(TwitterAPI client, Request request) throws ServletException {
        try {
            try {
                String name = client.client.getScreenName();
                System.out.printf("Signed in to %s\n", name);
            } catch (Exception e) {
                System.out.println("Signed in with application credentials!");
            }
            List<Status> tweets = client.getRecentTweets(request.screenName, NUM_TWEETS);
            List<Topic> topics = LanguageAPI.getInstance().getTopics(tweets, SALIENCE_THRESHOLD);
            int sublistLen = Math.min(topics.size(), request.numKeywords);

            topics.sort(((Comparator<Topic>) Topic::compareTo).reversed());
            topics = topics.subList(0, sublistLen);

            return new Response(System.currentTimeMillis(), topics);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    public static class Request {
        public final String screenName;
        public final Integer numKeywords;

        private Request(String screenName, Integer numKeywords) {
            this.screenName = screenName;
            this.numKeywords = numKeywords;
        }
    }

    public static class Response {
        public final long timestamp;
        public final List<Topic> topics;

        private Response(long timestamp, List<Topic> topics) {
            this.timestamp = timestamp;
            this.topics = topics;
        }
    }
}
