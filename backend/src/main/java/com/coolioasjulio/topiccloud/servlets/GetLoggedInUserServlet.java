package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;
import twitter4j.TwitterException;

public class GetLoggedInUserServlet extends JSONTwitterServlet<Void, GetLoggedInUserServlet.Response> {

    public GetLoggedInUserServlet() {
        super(Void.class);
    }

    @Override
    protected boolean validRequest(Void request) {
        return true;
    }

    @Override
    protected Response handleRequest(TwitterAPI client, Void request) {
        try {
            return new Response(client.client.getScreenName());
        } catch (IllegalStateException | TwitterException e) {
            return new Response(null);
        }
    }

    public static class Response {
        public boolean loggedIn;
        public String screenName;

        public Response(String screenName) {
            this.loggedIn = screenName != null;
            this.screenName = screenName;
        }
    }
}
