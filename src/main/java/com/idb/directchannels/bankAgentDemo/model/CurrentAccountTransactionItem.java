package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record CurrentAccountTransactionItem(
        Integer transactionNumber,
        String transactionCode,
        String transactionDescription,
        String transactionFullDescription,
        String transactionDate,
        String transactionBusinessDate,
        Double transactionAmount) {
}
