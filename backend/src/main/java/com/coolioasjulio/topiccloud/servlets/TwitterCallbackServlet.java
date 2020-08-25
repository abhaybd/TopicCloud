package com.coolioasjulio.topiccloud.servlets;

import com.coolioasjulio.topiccloud.TwitterAPI;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TwitterCallbackServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TwitterAPI client = (TwitterAPI) request.getSession().getAttribute("twitterClient");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        String verifier = request.getParameter("oauth_verifier");
        try {
            client.client.getOAuthAccessToken(requestToken, verifier);
            request.getSession().removeAttribute("requestToken");
        } catch (TwitterException e) {
            throw new ServletException(e);
        }
        response.sendRedirect("http://localhost:3000/");
    }
}
