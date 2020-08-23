package com.coolioasjulio.topiccloud;

import com.google.gson.Gson;
import twitter4j.TwitterException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TwitterInfoServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("application/json".equalsIgnoreCase(req.getContentType())) {
            Gson gson = new Gson();
            Request request = gson.fromJson(req.getReader(), Request.class);

            if (request != null && request.screenName != null) {
                try {
                    Response<String> response = new Response<>(TwitterAPI.getInstance().getProfilePicURL(request.screenName));
                    String json = gson.toJson(response);
                    resp.setStatus(200);
                    resp.setContentType("application/json");
                    resp.getWriter().print(json);
                    resp.getWriter().flush();
                } catch (TwitterException e) {
                    e.printStackTrace();
                    resp.sendError(500, e.getErrorMessage());
                }
            } else {
                resp.sendError(400, "Request must define a screen name");
            }
        }
    }

    private static class Request {
        public final String screenName;

        private Request(String screenName) {
            this.screenName = screenName;
        }
    }

    private static class Response<T> {
        public final T content;

        private Response(T content) {
            this.content = content;
        }
    }
}
