package com.idb.directchannels.bankAgentDemo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record BffLoansTotals(
        Double nextPaymentAmount,
        Double currentMonthPaymentAmount,
        String currentHebrewMonth,
        String currencyCode,
        Double originalCurrencyBalance,
        Double loansBalanceNIS,
        Double eligibilityLoanAmount,
        Integer loansQuantity,
        Integer arrearsLoansQuantity,
        List<LoanItem> loansList) {
}
