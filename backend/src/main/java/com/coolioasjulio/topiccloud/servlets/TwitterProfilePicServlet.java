package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;

public class TwitterProfilePicServlet extends JSONServlet<TwitterProfilePicServlet.Request, TwitterProfilePicServlet.Response> {

    public TwitterProfilePicServlet() {
        super(Request.class);
    }

    @Override
    protected boolean validRequest(Request request) {
        return request != null && request.screenName != null;
    }

    @Override
    protected Response handleRequest(Request request) throws Exception {
        return new Response(TwitterAPI.getInstance().getProfilePicURL(request.screenName));
    }

    public static class Request {
        public final String screenName;

        private Request(String screenName) {
            this.screenName = screenName;
        }
    }

    public static class Response {
        public final String content;

        private Response(String content) {
            this.content = content;
        }
    }
}
