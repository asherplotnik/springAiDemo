package com.idb.directchannels.bankAgentDemo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record BffTermDepositTotals(
        Double termDepositsBalanceILS,
        Double termDepositsForeignCurrencyBalance,
        String currencyCode,
        List<TermDepositsTotalsByCurrencyItem> termDepositsTotalsByCurrency,
        List<TermDepositItem> termDeposits) {
}
