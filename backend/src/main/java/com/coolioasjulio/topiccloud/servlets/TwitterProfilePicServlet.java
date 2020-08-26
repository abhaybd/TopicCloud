package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;
import twitter4j.TwitterException;

import javax.servlet.ServletException;

public class TwitterProfilePicServlet extends JSONTwitterServlet<TwitterProfilePicServlet.Request, TwitterProfilePicServlet.Response> {

    public TwitterProfilePicServlet() {
        super(Request.class);
    }

    @Override
    protected boolean validRequest(Request request) {
        return request != null && request.screenName != null;
    }

    @Override
    protected Response handleRequest(TwitterAPI client, Request request) throws ServletException {
        try {
            return new Response(client.getProfilePicURL(request.screenName));
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
    }

    public static class Request {
        public final String screenName;

        private Request(String screenName) {
            this.screenName = screenName;
        }
    }

    public static class Response {
        public final String imageUrl;

        private Response(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
