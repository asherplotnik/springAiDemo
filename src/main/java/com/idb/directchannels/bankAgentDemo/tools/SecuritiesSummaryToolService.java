package com.idb.directchannels.bankAgentDemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContext;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContextHolder;
import com.idb.directchannels.bankAgentDemo.model.SecuritiesSummaryResponse;

@Service
public class SecuritiesSummaryToolService {

    private final RestClient restClient;
    private final String securitiesBaseUrl;
    private final BankAgentRequestContextHolder requestContextHolder;

    public SecuritiesSummaryToolService(
            RestClient.Builder restClientBuilder,
            @Value("${banking.tools.securities.base-url:http://localhost:3000}") String securitiesBaseUrl,
            BankAgentRequestContextHolder requestContextHolder) {
        this.restClient = restClientBuilder.build();
        this.securitiesBaseUrl = securitiesBaseUrl.replaceAll("/+$", "");
        this.requestContextHolder = requestContextHolder;
    }

    @Tool(name = "get-securities-summary", description = "Get securities portfolio summary from the BFF securities portfolio API.")
    public SecuritiesSummaryResponse getSecuritiesSummary() {
        BankAgentRequestContext requestContext = requestContextHolder.getOrThrow();
        String endpoint = securitiesBaseUrl + "/api/v1/securities/BFFsecuritiesPortfolio";

        try {
            return restClient.get()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, requestContext.authorization())
                    .header("X-Global-Transaction-ID", requestContext.globalTransactionId())
                    .header("Accept-Language", requestContext.acceptLanguage())
                    .header("clientOS", requestContext.clientOS())
                    .header("clientVersion", requestContext.clientVersion())
                    .retrieve()
                    .body(SecuritiesSummaryResponse.class);
        } catch (RestClientResponseException ex) {
            throw new RuntimeException(
                    "Securities portfolio API failed (" + ex.getStatusCode().value() + " " + ex.getStatusText() + "): " + ex.getResponseBodyAsString(),
                    ex);
        }
    }
}
