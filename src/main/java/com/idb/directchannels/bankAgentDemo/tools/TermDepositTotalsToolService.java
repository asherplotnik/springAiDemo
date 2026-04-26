package com.idb.directchannels.bankAgentDemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContext;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContextHolder;
import com.idb.directchannels.bankAgentDemo.model.TermDepositTotalsResponse;

@Service
public class TermDepositTotalsToolService {

    private final RestClient restClient;
    private final String depositsBaseUrl;
    private final BankAgentRequestContextHolder requestContextHolder;

    public TermDepositTotalsToolService(
            RestClient.Builder restClientBuilder,
            @Value("${banking.tools.deposits.base-url:http://localhost:3000}") String depositsBaseUrl,
            BankAgentRequestContextHolder requestContextHolder) {
        this.restClient = restClientBuilder.build();
        this.depositsBaseUrl = depositsBaseUrl.replaceAll("/+$", "");
        this.requestContextHolder = requestContextHolder;
    }

    @Tool(name = "get-term-deposit-totals", description = "Get term-deposit totals and deposit list from the BFF term deposit totals API.")
    public TermDepositTotalsResponse getTermDepositTotals() {
        BankAgentRequestContext requestContext = requestContextHolder.getOrThrow();
        String endpoint = depositsBaseUrl + "/api/v1/deposits/BFFtermDepositTotals";

        try {
            return restClient.get()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, requestContext.authorization())
                    .header("X-Global-Transaction-ID", requestContext.globalTransactionId())
                    .header("Accept-Language", requestContext.acceptLanguage())
                    .header("clientOS", requestContext.clientOS())
                    .header("clientVersion", requestContext.clientVersion())
                    .retrieve()
                    .body(TermDepositTotalsResponse.class);
        } catch (RestClientResponseException ex) {
            throw new RuntimeException(
                    "Term deposit totals API failed (" + ex.getStatusCode().value() + " " + ex.getStatusText() + "): " + ex.getResponseBodyAsString(),
                    ex);
        }
    }
}
