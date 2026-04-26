package com.idb.directchannels.bankAgentDemo.service;

import java.util.List;

import com.idb.directchannels.bankAgentDemo.model.SessionMessage;

public interface ConversationMemoryService {
    List<SessionMessage> getHistory(String sessionId);

    void appendUserMessage(String sessionId, String content);

    void appendAssistantMessage(String sessionId, String content);
}
