package com.coolioasjulio.topiccloud.servlets;

import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class JSONServlet<Q,A> extends HttpServlet {
    private final Class<Q> reqClass;

    public JSONServlet(Class<Q> reqClass) {
        this.reqClass = reqClass;
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("application/json".equalsIgnoreCase(req.getContentType())) {
            Gson gson = new Gson();
            Q request = gson.fromJson(req.getReader(), reqClass);

            if (request != null && validRequest(request)) {
                try {
                    A response = handleRequest(request);
                    String json = gson.toJson(response);
                    resp.setStatus(200);
                    resp.setContentType("application/json");
                    resp.getWriter().print(json);
                    resp.getWriter().flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(500, e.getMessage());
                }
            } else {
                resp.sendError(400, "Illegally formatted request!");
            }
        }
    }

    protected abstract boolean validRequest(Q request);

    protected abstract A handleRequest(Q request) throws Exception;
}
