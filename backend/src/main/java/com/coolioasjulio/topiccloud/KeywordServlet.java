package com.coolioasjulio.topiccloud;

import com.google.gson.Gson;
import twitter4j.Status;
import twitter4j.TwitterException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class KeywordServlet extends HttpServlet {

    private static final double SALIENCE_THRESHOLD = 0.08;
    private static final int NUM_TWEETS = 100;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("application/json".equalsIgnoreCase(req.getContentType())) {
            Gson gson = new Gson();
            Request request = gson.fromJson(req.getReader(), Request.class);

            if (request != null && request.screenName != null && request.numKeywords != null) {
                System.out.printf("Request: %s, num: %d\n", request.screenName, request.numKeywords);
                Response<?> response = processRequest(request);
                if (response != null) {
                    String json = gson.toJson(response);
                    System.out.println(json);
                    resp.setStatus(200);
                    resp.setContentType("application/json");
                    resp.getWriter().print(json);
                    resp.getWriter().flush();
                } else {
                    resp.sendError(500, "An unexpected error occurred!");
                }
            } else {
                resp.sendError(400, "Illegally formatted request!");
            }
        }
    }

    private Response<List<Topic>> processRequest(Request request) {
        try {
            List<Status> tweets = TwitterAPI.getInstance().getRecentTweets(request.screenName, NUM_TWEETS);
            List<Topic> topics = LanguageAPI.getInstance().getTopics(tweets, SALIENCE_THRESHOLD);
            int sublistLen = Math.min(topics.size(), request.numKeywords);

            topics.sort(((Comparator<Topic>) Topic::compareTo).reversed());
            topics = topics.subList(0, sublistLen);

            return new Response<>(System.currentTimeMillis(), topics);
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class Request {
        public final String screenName;
        public final Integer numKeywords;

        private Request(String screenName, Integer numKeywords) {
            this.screenName = screenName;
            this.numKeywords = numKeywords;
        }
    }

    private static class Result {
        public final Topic topic;
        public final List<Long> ids;

        private Result(Topic topic, List<Long> ids) {
            this.topic = topic;
            this.ids = ids;
        }
    }

    private static class Response<T> {
        public final long timestamp;
        public final T content;

        private Response(long timestamp, T content) {
            this.timestamp = timestamp;
            this.content = content;
        }
    }
}
