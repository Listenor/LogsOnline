package com.xxg.websocket;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class RequestListener implements ServletRequestListener {
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {

    }

    public RequestListener() {

    }

    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        ((HttpServletRequest) servletRequestEvent.getServletRequest()).getSession();
    }
}
