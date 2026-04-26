package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record LoanItem(
        String loanAccountID,
        String loanAccountCurrencyCode,
        String loanRegistrationValueDate,
        String mainLoanModelDescription,
        String secondaryLoanModelDescription,
        Integer originPaymentsQuantity,
        Integer paidPaymentsCount,
        Double cashBalanceNIS,
        String nextPaymentDate,
        Boolean isLoanAccountArrears,
        Double originalLoanBalance) {
}
