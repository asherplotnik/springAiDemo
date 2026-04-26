package com.idb.directchannels.bankAgentDemo.context;

import org.springframework.stereotype.Component;

@Component
public class BankAgentRequestContextHolder {

    private final ThreadLocal<BankAgentRequestContext> requestContextThreadLocal = new ThreadLocal<>();

    public void set(BankAgentRequestContext requestContext) {
        requestContextThreadLocal.set(requestContext);
    }

    public BankAgentRequestContext getOrThrow() {
        BankAgentRequestContext requestContext = requestContextThreadLocal.get();
        if (requestContext == null) {
            throw new IllegalStateException("Missing bank agent request context");
        }
        return requestContext;
    }

    public void clear() {
        requestContextThreadLocal.remove();
    }
}
