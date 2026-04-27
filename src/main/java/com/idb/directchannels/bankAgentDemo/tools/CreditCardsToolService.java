package com.idb.directchannels.bankAgentDemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContext;
import com.idb.directchannels.bankAgentDemo.context.BankAgentRequestContextHolder;
import com.idb.directchannels.bankAgentDemo.model.CustomerCreditCardsResponse;

@Service
public class CreditCardsToolService {

    private final RestClient restClient;
    private final String creditCardsBaseUrl;
    private final BankAgentRequestContextHolder requestContextHolder;

    public CreditCardsToolService(
            RestClient.Builder restClientBuilder,
            @Value("${banking.tools.credit-cards.base-url:http://localhost:3000}") String creditCardsBaseUrl,
            BankAgentRequestContextHolder requestContextHolder) {
        this.restClient = restClientBuilder.build();
        this.creditCardsBaseUrl = creditCardsBaseUrl.replaceAll("/+$", "");
        this.requestContextHolder = requestContextHolder;
    }

    @Tool(name = "get-customer-credit-cards", description = """
            Use this tool to retrieve the authenticated customer's credit cards and next debit totals.
            Uses request context headers (Authorization, SessionID, transaction/language/client headers).

            Output schema (key parts, no metadata):
            - CardListData:
              {
                nextDebitTotalNIS: number,
                nextDebitTotalUSD: number,
                nextDebitTotalEuro: number,
                maxNumOfCardsForDisplay: number,
                nextDebitDateForDisplay: string,
                cardList: CardItem[]
              }

            - CardItem:
              {
                cardFourLastDigits: string,
                nextDebitDate: string,
                isDigitalCard: boolean,
                cardStatus: string,
                cardStatusDescription: string,
                isAllowToUnfreeze: boolean,
                creditCardMonthlyUsageAmount: number,
                creditCardIssuerCodeDesc: string,
                dataCorrectnessDate: string,
                totalCardMonthUtilizeLimit: number,
                maximumRechargeAmount: number,
                nextDebitTotalNIS: number,
                nextDebitTotalUSD: number,
                nextDebitTotalEuro: number,
                prePaidCardBalance: number,
                leftToLoad: number,
                isDebitCard: boolean,
                isExternalCard: boolean,
                cardFamily: string,
                cardFamilyDescription: string,
                cardTypeDescription: string
              }
            """)
    public CustomerCreditCardsResponse getCustomerCreditCards() {
        BankAgentRequestContext requestContext = requestContextHolder.getOrThrow();
        String endpoint = creditCardsBaseUrl + "/api/v1/creditCard/BFFcardList";

        try {
            return restClient.get()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, requestContext.authorization())
                    .header("SessionID", requestContext.sessionId())
                    .header("X-Global-Transaction-ID", requestContext.globalTransactionId())
                    .header("Accept-Language", requestContext.acceptLanguage())
                    .header("clientOS", requestContext.clientOS())
                    .header("clientVersion", requestContext.clientVersion())
                    .header("X-Forwarded-For", requestContext.xForwardedFor())
                    .header("accountV", requestContext.accountV())
                    .retrieve()
                    .body(CustomerCreditCardsResponse.class);
        } catch (RestClientResponseException ex) {
            throw new RuntimeException(
                    "Credit cards API failed (" + ex.getStatusCode().value() + " " + ex.getStatusText() + "): " + ex.getResponseBodyAsString(),
                    ex);
        }
    }
}
