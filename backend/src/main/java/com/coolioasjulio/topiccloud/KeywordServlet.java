package com.coolioasjulio.topiccloud;

import com.google.gson.Gson;
import twitter4j.TwitterException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class KeywordServlet extends HttpServlet {

    private static final double SALIENCE_THRESHOLD = 0.08;
    private static final int NUM_TWEETS = 100;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("application/json".equalsIgnoreCase(req.getContentType())) {
            Gson gson = new Gson();
            Request request = gson.fromJson(req.getReader(), Request.class);

            if (request != null && request.screenName != null) {
                Response response = processRequest(request);
                if (response != null) {
                    String json = gson.toJson(response);
                    resp.setStatus(200);
                    resp.setContentType("application/json");
                    resp.getWriter().print(json);
                    resp.getWriter().flush();
                } else {
                    resp.sendError(500, "An unexpected error occurred!");
                }
            } else {
                resp.sendError(400, "Request must define a screen name");
            }
        }
    }

    private Response processRequest(Request request) {
        try {
            List<String> tweets = TwitterAPI.getInstance().getRecentTweetsSanitized(request.screenName, NUM_TWEETS);
            List<Word> words = LanguageAPI.getInstance().getEntities(tweets, SALIENCE_THRESHOLD);

            return new Response(System.currentTimeMillis(), words.toArray(new Word[0]));
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class Request {
        public final String screenName;

        private Request(String screenName) {
            this.screenName = screenName;
        }
    }

    private static class Response {
        public final long timestamp;
        public final Word[] words;

        private Response(long timestamp, Word[] words) {
            this.timestamp = timestamp;
            this.words = words;
        }
    }
}
