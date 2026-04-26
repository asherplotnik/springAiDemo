package com.idb.directchannels.bankAgentDemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContext;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContextHolder;
import com.idb.directchannels.bankAgentDemo.model.CurrentAccountSummaryAndTransactions;
import com.idb.directchannels.bankAgentDemo.model.CurrentAccountSummaryData;
import com.idb.directchannels.bankAgentDemo.model.CurrentAccountSummaryAndTransactionsResponse;
import com.idb.directchannels.bankAgentDemo.util.JwtUtils;

@Service
public class AccountSummaryAndTransactionsToolService {

    private final RestClient restClient;
    private final String currentAccountBaseUrl;
    private final BankAgentRequestContextHolder requestContextHolder;

    public AccountSummaryAndTransactionsToolService(
            RestClient.Builder restClientBuilder,
            @Value("${banking.tools.current-account.base-url:http://localhost:3000}") String currentAccountBaseUrl,
            BankAgentRequestContextHolder requestContextHolder) {
        this.restClient = restClientBuilder.build();
        this.currentAccountBaseUrl = currentAccountBaseUrl.replaceAll("/+$", "");
        this.requestContextHolder = requestContextHolder;
    }

    @Tool(name = "get-account-summary-and-transactions", description = "Get current account summary details and recent transactions from the BFF current account summary API.")
    public CurrentAccountSummaryAndTransactionsResponse getAccountSummaryAndTransactions() {
        BankAgentRequestContext requestContext = requestContextHolder.getOrThrow();
        String endpoint = currentAccountBaseUrl + "/api/v1/currentAccount/BFFcurrentAccSummaryTrans";
        String branchNumber = JwtUtils.getBranchNumber(requestContext.authorization());
        String accountNumber = JwtUtils.getAccountNumber(requestContext.authorization());

        try {
            CurrentAccountSummaryAndTransactionsResponse response = restClient.get()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, requestContext.authorization())
                    .header("X-Global-Transaction-ID", requestContext.globalTransactionId())
                    .header("Accept-Language", requestContext.acceptLanguage())
                    .header("clientOS", requestContext.clientOS())
                    .header("clientVersion", requestContext.clientVersion())
                    .retrieve()
                    .body(CurrentAccountSummaryAndTransactionsResponse.class);
            return enrichWithDerivedAccountData(response, branchNumber, accountNumber);
        } catch (RestClientResponseException ex) {
            throw new RuntimeException(
                    "Current account summary API failed (" + ex.getStatusCode().value() + " " + ex.getStatusText() + "): " + ex.getResponseBodyAsString(),
                    ex);
        }
    }

    private static CurrentAccountSummaryAndTransactionsResponse enrichWithDerivedAccountData(
            CurrentAccountSummaryAndTransactionsResponse response,
            String branchNumber,
            String accountNumber) {
        if (response == null || response.bffCurrentAccSummaryTrans() == null || response.bffCurrentAccSummaryTrans().data() == null) {
            return response;
        }

        CurrentAccountSummaryAndTransactions summary = response.bffCurrentAccSummaryTrans();
        CurrentAccountSummaryData data = summary.data();
        CurrentAccountSummaryData enrichedData = new CurrentAccountSummaryData(
                branchNumber,
                accountNumber,
                data.balance(),
                data.availableBalance(),
                data.currencyCode(),
                data.currencyDescription(),
                data.creditLineFramework(),
                data.secureFutureTransactionsExists(),
                data.loanExists(),
                data.termDepositExists(),
                data.savingPlansExists(),
                data.loanTermDepositExists(),
                data.securityExists(),
                data.mortgageExists(),
                data.parameterMinTransactionsForDisplay(),
                data.isInLegalTreatment(),
                data.transactionsList());

        return new CurrentAccountSummaryAndTransactionsResponse(
                new CurrentAccountSummaryAndTransactions(enrichedData, summary.metaData()));
    }
}
