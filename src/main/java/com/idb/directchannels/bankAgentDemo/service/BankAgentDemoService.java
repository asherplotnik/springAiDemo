package com.idb.directchannels.bankAgentDemo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContext;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContextHolder;
import com.idb.directchannels.bankAgentDemo.model.BankAgentExecuteRequest;
import com.idb.directchannels.bankAgentDemo.model.BankAgentExecuteResponse;
import com.idb.directchannels.bankAgentDemo.prompts.BankAgentDemoPrompts;
import com.idb.directchannels.bankAgentDemo.tools.CreditCardsToolService;
import com.idb.directchannels.bankAgentDemo.tools.LoansTotalsToolService;
import com.idb.directchannels.bankAgentDemo.tools.SecuritiesSummaryToolService;
import com.idb.directchannels.bankAgentDemo.tools.TermDepositTotalsToolService;

import lombok.extern.slf4j.Slf4j;

/**
 * Banking root agent via Spring AI {@link ChatClient}. Optional A2A delegation to
 * {@code accountPlatformSpecialist} matches {@code AdkDemoAgentService}.
 */
@Slf4j
@Service
public class BankAgentDemoService {

    private static final int MAX_DELEGATION_HOPS = 3;

    private final ChatClient bankAgentChatClient;
    private final ConversationMemoryService conversationMemoryService;
    private final ConversationPromptBuilder conversationPromptBuilder;
    private final CreditCardsToolService creditCardsToolService;
    private final LoansTotalsToolService loansTotalsToolService;
    private final SecuritiesSummaryToolService securitiesSummaryToolService;
    private final TermDepositTotalsToolService termDepositTotalsToolService;
    private final BankAgentRequestContextHolder requestContextHolder;
    private final RestClient restClient;
    private final JsonMapper jsonMapper;
    private final String accountAgentBaseUrl;

    public BankAgentDemoService(
            ChatClient bankAgentChatClient,
            ConversationMemoryService conversationMemoryService,
            ConversationPromptBuilder conversationPromptBuilder,
            CreditCardsToolService creditCardsToolService,
            LoansTotalsToolService loansTotalsToolService,
            SecuritiesSummaryToolService securitiesSummaryToolService,
            TermDepositTotalsToolService termDepositTotalsToolService,
            BankAgentRequestContextHolder requestContextHolder,
            RestClient.Builder restClientBuilder,
            JsonMapper jsonMapper,
            @Value("${banking.agents.account-agent.base-url:http://localhost:8081}") String accountAgentBaseUrl) {
        this.bankAgentChatClient = bankAgentChatClient;
        this.conversationMemoryService = conversationMemoryService;
        this.conversationPromptBuilder = conversationPromptBuilder;
        this.creditCardsToolService = creditCardsToolService;
        this.loansTotalsToolService = loansTotalsToolService;
        this.securitiesSummaryToolService = securitiesSummaryToolService;
        this.termDepositTotalsToolService = termDepositTotalsToolService;
        this.requestContextHolder = requestContextHolder;
        this.restClient = restClientBuilder.build();
        this.jsonMapper = jsonMapper;
        this.accountAgentBaseUrl = accountAgentBaseUrl.replaceAll("/+$", "");
    }

    /**
     * Runs the banking agent with the same delegation loop as {@code AdkDemoAgentService}.
     */
    public AgentExecutionResult execute(String sessionId, String taskInput) {
        List<Object> aggregatedToolCalls = new ArrayList<>();
        String userTurn = taskInput;
        String originalTaskInput = taskInput;
        String currentTaskInput = taskInput;

        for (int hop = 0; hop < MAX_DELEGATION_HOPS; hop++) {
            int hopNumber = hop + 1;
            logHopStart(hopNumber, sessionId, currentTaskInput);

            AgentExecutionResult mainResult = runMainAgentOnce(sessionId, currentTaskInput);
            aggregatedToolCalls.addAll(mainResult.toolCalls());

            DelegationRequest delegation = parseDelegationRequest(mainResult.content());
            if (delegation == null) {
                return finalizeWithMainResponse(sessionId, userTurn, hopNumber, mainResult, aggregatedToolCalls);
            }
            logDelegationDetected(hopNumber, sessionId, delegation);
            if (!"accountPlatformSpecialist".equals(delegation.targetAgent())) {
                return rejectUnsupportedDelegation(delegation, aggregatedToolCalls);
            }

            currentTaskInput = invokeDelegateAndBuildNextPrompt(
                    sessionId, hopNumber, delegation, originalTaskInput, aggregatedToolCalls);
        }

        return handleDelegationLoopLimitReached(sessionId, userTurn, aggregatedToolCalls);
    }

    private void logHopStart(int hopNumber, String sessionId, String currentTaskInput) {
        log.info("[A2A][LOOP] hop={}/{} sessionId={} phase=main-agent-run", hopNumber, MAX_DELEGATION_HOPS, sessionId);
        log.debug(
                "[DEBUG][SESSION_DUMP][MAIN][INPUT] hop={}/{} sessionId={} payload={}",
                hopNumber,
                MAX_DELEGATION_HOPS,
                sessionId,
                currentTaskInput == null ? "null" : currentTaskInput);
    }

    private AgentExecutionResult runMainAgentOnce(String sessionId, String taskInput) {
        String userPrompt = conversationPromptBuilder.buildPromptWithHistory(sessionId, taskInput);
        var responseSpec = bankAgentChatClient.prompt()
                .system(BankAgentDemoPrompts.BANKING_AGENT_INSTRUCTIONS)
                .user(userPrompt)
                .tools(
                        creditCardsToolService,
                        loansTotalsToolService,
                        securitiesSummaryToolService,
                        termDepositTotalsToolService)
                .call();

        ChatResponse chatResponse = responseSpec.chatResponse();
        String content = responseSpec.content();

        List<Object> roundToolCalls = new ArrayList<>();
        collectSpringAiToolCalls(chatResponse, roundToolCalls);
        return new AgentExecutionResult(content, roundToolCalls);
    }

    private static void collectSpringAiToolCalls(ChatResponse chatResponse, List<Object> toolCalls) {
        if (chatResponse == null || chatResponse.getResults() == null) {
            return;
        }
        for (Generation gen : chatResponse.getResults()) {
            AssistantMessage msg = gen.getOutput();
            if (msg == null || !msg.hasToolCalls()) {
                continue;
            }
            for (AssistantMessage.ToolCall tc : msg.getToolCalls()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", "tool_call");
                item.put("toolName", tc.name());
                item.put("callId", tc.id());
                item.put("arguments", tc.arguments());
                toolCalls.add(item);
            }
        }
    }

    private AgentExecutionResult finalizeWithMainResponse(
            String sessionId,
            String userTurn,
            int hopNumber,
            AgentExecutionResult mainResult,
            List<Object> aggregatedToolCalls) {
        log.info(
                "[A2A][LOOP][COMPLETE] hop={}/{} sessionId={} result=final-response preview=\"{}\"",
                hopNumber,
                MAX_DELEGATION_HOPS,
                sessionId,
                preview(mainResult.content()));
        if (userTurn != null && !userTurn.isBlank()) {
            conversationMemoryService.appendUserMessage(sessionId, userTurn);
        }
        if (mainResult.content() != null && !mainResult.content().isBlank()) {
            conversationMemoryService.appendAssistantMessage(sessionId, mainResult.content());
        }
        return new AgentExecutionResult(mainResult.content(), aggregatedToolCalls);
    }

    private void logDelegationDetected(int hopNumber, String sessionId, DelegationRequest delegation) {
        log.info(
                "[A2A][DELEGATION][DETECTED] hop={}/{} sessionId={} targetAgent={} taskPreview=\"{}\"",
                hopNumber,
                MAX_DELEGATION_HOPS,
                sessionId,
                delegation.targetAgent(),
                preview(delegation.taskInput()));
    }

    private AgentExecutionResult rejectUnsupportedDelegation(DelegationRequest delegation, List<Object> aggregatedToolCalls) {
        log.warn("[A2A][DELEGATION][REJECTED] unsupportedTarget={}", delegation.targetAgent());
        return new AgentExecutionResult(
                "Unsupported delegation target: " + delegation.targetAgent(),
                aggregatedToolCalls);
    }

    private String invokeDelegateAndBuildNextPrompt(
            String sessionId,
            int hopNumber,
            DelegationRequest delegation,
            String originalTaskInput,
            List<Object> aggregatedToolCalls) {
        AccountAgentResult accountAgentResult = invokeAccountAgent(sessionId, delegation.taskInput(), hopNumber);
        aggregatedToolCalls.add(accountAgentResult.trace());
        return buildDelegationFollowupPrompt(originalTaskInput, accountAgentResult.content());
    }

    private static String buildDelegationFollowupPrompt(String originalTaskInput, String accountAgentResponse) {
        return """
                The original user request was:
                %s

                Peer agent response from accountPlatformSpecialist:
                %s

                Produce a concise user-facing answer in the user's language.
                If the peer response is OUT_OF_SCOPE, offer a concrete alternative:
                show recent transactions or transactions in a chosen date range.
                Do not reset the conversation.
                """.formatted(originalTaskInput, accountAgentResponse);
    }

    private AgentExecutionResult handleDelegationLoopLimitReached(
            String sessionId, String userTurn, List<Object> aggregatedToolCalls) {
        String message = "Delegation loop limit reached before final response.";
        if (userTurn != null && !userTurn.isBlank()) {
            conversationMemoryService.appendUserMessage(sessionId, userTurn);
        }
        conversationMemoryService.appendAssistantMessage(sessionId, message);
        return new AgentExecutionResult(message, aggregatedToolCalls);
    }

    /**
     * Parses delegation JSON from the assistant message. Supports POC "mixed intent" replies where
     * prose or tool summaries appear before a trailing {@code {"action":"delegate",...}} block.
     */
    private DelegationRequest parseDelegationRequest(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String trimmed = content.trim();
        DelegationRequest whole = tryParseDelegationObject(trimmed);
        if (whole != null) {
            return whole;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (trimmed.charAt(i) != '{') {
                continue;
            }
            int end = findMatchingObjectEnd(trimmed, i);
            if (end < 0) {
                continue;
            }
            String candidate = trimmed.substring(i, end + 1);
            DelegationRequest parsed = tryParseDelegationObject(candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private DelegationRequest tryParseDelegationObject(String json) {
        try {
            JsonNode node = jsonMapper.readTree(json);
            if (!node.isObject()) {
                return null;
            }
            String action = node.path("action").asText("");
            if (!"delegate".equals(action)) {
                return null;
            }
            String targetAgent = node.path("target_agent").asText("");
            String taskInput = node.path("task_input").asText("");
            if (targetAgent.isBlank() || taskInput.isBlank()) {
                return null;
            }
            return new DelegationRequest(targetAgent, taskInput);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Index of closing {@code '}'} for JSON object starting at {@code start}, or -1. Respects strings and escapes. */
    private static int findMatchingObjectEnd(String s, int start) {
        if (start >= s.length() || s.charAt(start) != '{') {
            return -1;
        }
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private AccountAgentResult invokeAccountAgent(String sessionId, String taskInput, int hopNumber) {
        BankAgentRequestContext requestContext = requestContextHolder.getOrThrow();
        String endpoint = accountAgentBaseUrl + "/api/account-agent-demo/a2a/execute";

        try {
            log.info(
                    "[A2A][INVOKE][START] hop={} sessionId={} targetAgent=accountPlatformSpecialist endpoint={} taskPreview=\"{}\"",
                    hopNumber,
                    sessionId,
                    endpoint,
                    preview(taskInput));
            BankAgentExecuteResponse response = restClient.post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, requestContext.authorization())
                    .header("sessionId", sessionId)
                    .header("X-Global-Transaction-ID", requestContext.globalTransactionId())
                    .header("Accept-Language", requestContext.acceptLanguage())
                    .header("clientOS", requestContext.clientOS())
                    .header("clientVersion", requestContext.clientVersion())
                    .header("X-Forwarded-For", requestContext.xForwardedFor())
                    .header("accountV", requestContext.accountV())
                    .body(new BankAgentExecuteRequest(taskInput, sessionId))
                    .retrieve()
                    .body(BankAgentExecuteResponse.class);

            String responseContent = response == null ? null : response.content();
            log.info(
                    "[A2A][INVOKE][SUCCESS] hop={} sessionId={} targetAgent=accountPlatformSpecialist responsePreview=\"{}\"",
                    hopNumber,
                    sessionId,
                    preview(responseContent));
            Map<String, Object> trace = new LinkedHashMap<>();
            trace.put("type", "agent_call");
            trace.put("targetAgent", "accountPlatformSpecialist");
            trace.put("endpoint", endpoint);
            trace.put("taskInput", taskInput);
            trace.put("result", responseContent);
            if (response != null && response.toolCalls() != null) {
                trace.put("toolCalls", response.toolCalls());
            }
            return new AccountAgentResult(responseContent, trace);
        } catch (RestClientResponseException ex) {
            log.error(
                    "[A2A][INVOKE][FAILED] hop={} sessionId={} targetAgent=accountPlatformSpecialist status={} {} body=\"{}\"",
                    hopNumber,
                    sessionId,
                    ex.getStatusCode().value(),
                    ex.getStatusText(),
                    preview(ex.getResponseBodyAsString()));
            throw new RuntimeException(
                    "Account agent invoke failed (" + ex.getStatusCode().value() + " " + ex.getStatusText()
                            + "): " + ex.getResponseBodyAsString(),
                    ex);
        }
    }

    private static String preview(String text) {
        if (text == null) {
            return "null";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        int max = 180;
        return normalized.length() <= max ? normalized : normalized.substring(0, max) + "...";
    }

    public record AgentExecutionResult(String content, List<Object> toolCalls) {}

    private record DelegationRequest(String targetAgent, String taskInput) {}

    private record AccountAgentResult(String content, Map<String, Object> trace) {}
}
