package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;
import twitter4j.TwitterException;

import javax.servlet.ServletException;
import java.util.List;

public class TwitterNameSuggestionServlet extends JSONTwitterServlet<TwitterNameSuggestionServlet.Request, TwitterNameSuggestionServlet.Response> {

    public TwitterNameSuggestionServlet() {
        super(Request.class);
    }

    @Override
    protected boolean validRequest(Request request) {
        return request != null && request.screenName != null && request.numSuggestions != null && request.numSuggestions > 0;
    }

    @Override
    protected Response handleRequest(TwitterAPI client, Request request) throws ServletException {
        // TODO: This currently fails. We'll need to authenticate on a per-user basis (that'll help with rate limits too)
        try {
            return new Response(client.getNameSuggestions(request.screenName, request.numSuggestions));
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
    }

    public static class Request {
        public final String screenName;
        public final Integer numSuggestions;

        private Request(String screenName, Integer numSuggestions) {
            this.screenName = screenName;
            this.numSuggestions = numSuggestions;
        }
    }

    public static class Response {
        public final List<String> suggestions;

        private Response(List<String> suggestions) {
            this.suggestions = suggestions;
        }
    }
}
