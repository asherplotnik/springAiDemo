package com.idb.directchannels.bankAgentDemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContext;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContextHolder;
import com.idb.directchannels.bankAgentDemo.model.LoansTotalsResponse;

@Service
public class LoansTotalsToolService {

    private final RestClient restClient;
    private final String loansBaseUrl;
    private final BankAgentRequestContextHolder requestContextHolder;

    public LoansTotalsToolService(
            RestClient.Builder restClientBuilder,
            @Value("${banking.tools.loans.base-url:http://localhost:3000}") String loansBaseUrl,
            BankAgentRequestContextHolder requestContextHolder) {
        this.restClient = restClientBuilder.build();
        this.loansBaseUrl = loansBaseUrl.replaceAll("/+$", "");
        this.requestContextHolder = requestContextHolder;
    }

    @Tool(name = "get-loans-totals", description = "Get loan totals and loan list from the BFF loans totals API.")
    public LoansTotalsResponse getLoansTotals() {
        BankAgentRequestContext requestContext = requestContextHolder.getOrThrow();
        String endpoint = loansBaseUrl + "/api/v1/loans/BFFloansTotals";

        try {
            return restClient.get()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, requestContext.authorization())
                    .header("X-Global-Transaction-ID", requestContext.globalTransactionId())
                    .header("Accept-Language", requestContext.acceptLanguage())
                    .header("clientOS", requestContext.clientOS())
                    .header("clientVersion", requestContext.clientVersion())
                    .retrieve()
                    .body(LoansTotalsResponse.class);
        } catch (RestClientResponseException ex) {
            throw new RuntimeException(
                    "Loans totals API failed (" + ex.getStatusCode().value() + " " + ex.getStatusText() + "): " + ex.getResponseBodyAsString(),
                    ex);
        }
    }
}
