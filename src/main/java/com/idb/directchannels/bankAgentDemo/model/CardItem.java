package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record CardItem(
        String cardFourLastDigits,
        String nextDebitDate,
        Boolean isDigitalCard,
        String cardStatus,
        String cardStatusDescription,
        Boolean isAllowToUnfreeze,
        Double creditCardMonthlyUsageAmount,
        String creditCardIssuerCodeDesc,
        String dataCorrectnessDate,
        Double totalCardMonthUtilizeLimit,
        Double maximumRechargeAmount,
        Double nextDebitTotalNIS,
        Double nextDebitTotalUSD,
        Double nextDebitTotalEuro,
        Double prePaidCardBalance,
        Double leftToLoad,
        Boolean isDebitCard,
        Boolean isExternalCard,
        String cardFamily,
        String cardFamilyDescription,
        String cardTypeDescription) {
}
