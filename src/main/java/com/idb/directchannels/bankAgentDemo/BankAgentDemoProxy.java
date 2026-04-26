package com.idb.directchannels.bankAgentDemo;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContext;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContextHolder;
import com.idb.directchannels.bankAgentDemo.model.BankAgentExecuteRequest;
import com.idb.directchannels.bankAgentDemo.model.BankAgentExecuteResponse;
import com.idb.directchannels.bankAgentDemo.service.BankAgentDemoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BankAgentDemoProxy {
    private final BankAgentDemoService bankAgentDemoService;
    private final BankAgentRequestContextHolder requestContextHolder;

    @PostMapping("/api/banking-agent-demo/a2a/execute")
        public BankAgentExecuteResponse getBankAgentDemo(
                @RequestHeader("Authorization") String authorization,
                @RequestHeader(value = "sessionId", defaultValue = "SessionID") String sessionId,
                @RequestHeader(value = "X-Global-Transaction-ID", defaultValue = "netanel1122334455ss") String globalTransactionId,
                @RequestHeader(value = "Accept-Language", defaultValue = "he-IL") String acceptLanguage,
                @RequestHeader(value = "clientOS", defaultValue = "1") String clientOS,
                @RequestHeader(value = "clientVersion", defaultValue = "2") String clientVersion,
                @RequestHeader(value = "X-Forwarded-For", defaultValue = "123456") String xForwardedFor,
                @RequestHeader(value = "accountV", defaultValue = "blabla") String accountV,
                @RequestBody BankAgentExecuteRequest requestBody) {
            String resolvedSessionId = requestBody.sessionId() == null || requestBody.sessionId().isBlank()
                    ? sessionId
                    : requestBody.sessionId();
            requestContextHolder.set(new BankAgentRequestContext(
                    authorization,
                    resolvedSessionId,
                    globalTransactionId,
                    acceptLanguage,
                    clientOS,
                    clientVersion,
                    xForwardedFor,
                    accountV));
            try {
                String content = bankAgentDemoService.getBankAgentDemo(resolvedSessionId, requestBody.taskInput());
                return new BankAgentExecuteResponse("agent", content, List.of(), resolvedSessionId);
            } finally {
                requestContextHolder.clear();
            }
        }
}
