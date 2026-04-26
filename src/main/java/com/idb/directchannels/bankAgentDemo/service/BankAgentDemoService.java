package com.idb.directchannels.bankAgentDemo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.idb.directchannels.bankAgentDemo.prompts.BankAgentDemoPrompts;
import com.idb.directchannels.bankAgentDemo.tools.AccountSummaryAndTransactionsToolService;
import com.idb.directchannels.bankAgentDemo.tools.CreditCardsToolService;
import com.idb.directchannels.bankAgentDemo.tools.LoansTotalsToolService;
import com.idb.directchannels.bankAgentDemo.tools.SecuritiesSummaryToolService;
import com.idb.directchannels.bankAgentDemo.tools.TermDepositTotalsToolService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAgentDemoService {
    private final ChatClient bankAgentChatClient;
    private final ConversationMemoryService conversationMemoryService;
    private final ConversationPromptBuilder conversationPromptBuilder;
    private final CreditCardsToolService creditCardsToolService;
    private final AccountSummaryAndTransactionsToolService accountSummaryAndTransactionsToolService;
    private final LoansTotalsToolService loansTotalsToolService;
    private final SecuritiesSummaryToolService securitiesSummaryToolService;
    private final TermDepositTotalsToolService termDepositTotalsToolService;

    public String getBankAgentDemo(String sessionId, String requestBody) {
        String userPrompt = conversationPromptBuilder.buildPromptWithHistory(sessionId, requestBody);

        String response = bankAgentChatClient.prompt()
                .system(BankAgentDemoPrompts.BANKING_AGENT_INSTRUCTIONS)
                .user(userPrompt)
                .tools(
                        creditCardsToolService,
                        accountSummaryAndTransactionsToolService,
                        loansTotalsToolService,
                        securitiesSummaryToolService,
                        termDepositTotalsToolService)
                .call()
                .content();

        conversationMemoryService.appendUserMessage(sessionId, requestBody);
        conversationMemoryService.appendAssistantMessage(sessionId, response);
        return response;
    }
}
