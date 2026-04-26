package com.idb.directchannels.bankAgentDemo.context;

public record BankAgentRequestContext(
        String authorization,
        String sessionId,
        String globalTransactionId,
        String acceptLanguage,
        String clientOS,
        String clientVersion,
        String xForwardedFor,
        String accountV) {
}
