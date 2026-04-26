package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record TermDepositItem(
        String currencyCode,
        String termDepositAccountID,
        String combinedTermDepositTypes,
        String subProductCode,
        String subProductLongDescription,
        String subProductShortDescription,
        Double termDepositBalance,
        String depositMaturityDate) {
}
