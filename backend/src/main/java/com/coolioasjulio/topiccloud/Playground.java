package com.coolioasjulio.topiccloud;

import com.coolioasjulio.topiccloud.servlets.GetLoggedInUserServlet;
import com.google.gson.Gson;
import twitter4j.TwitterException;

public class Playground {
    public static void main(String[] args) throws TwitterException {
        Gson gson = new Gson();
        System.out.println(gson.toJson(new Foo(), Foo.class));
    }

    static class Foo {
        String bar = "a";
    }
}
