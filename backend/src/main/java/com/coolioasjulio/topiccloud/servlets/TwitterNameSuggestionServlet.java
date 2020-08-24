package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;

import java.util.List;

public class TwitterNameSuggestionServlet extends JSONServlet<TwitterNameSuggestionServlet.Request, TwitterNameSuggestionServlet.Response> {

    public TwitterNameSuggestionServlet() {
        super(Request.class);
    }

    @Override
    protected boolean validRequest(Request request) {
        return request != null && request.screenName != null && request.numSuggestions != null && request.numSuggestions > 0;
    }

    @Override
    protected Response handleRequest(Request request) throws Exception {
        // TODO: This currently fails. We'll need to authenticate on a per-user basis (that'll help with rate limits too)
        return new Response(TwitterAPI.getInstance().getNameSuggestions(request.screenName, request.numSuggestions));
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
        public final List<String> content;

        private Response(List<String> content) {
            this.content = content;
        }
    }
}
