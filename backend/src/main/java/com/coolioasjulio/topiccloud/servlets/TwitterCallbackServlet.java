package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TwitterCallbackServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        TwitterAPI client = (TwitterAPI) request.getSession().getAttribute("twitterClient");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");

        request.getSession().removeAttribute("requestToken");

        boolean success = true;
        if (verifier != null) {
            try {
                client.client.getOAuthAccessToken(requestToken, verifier);
            } catch (TwitterException e) {
                success = false;
            }
        } else {
            success = false;
        }

        if (!success) {
            request.getSession().removeAttribute("twitterClient");
        }

        String redirect = request.getContextPath() + "/";
        if ("false".equals(System.getProperty("prod"))) {
            redirect = "http://localhost:3000/";
        }

        response.sendRedirect(redirect);
    }
}
