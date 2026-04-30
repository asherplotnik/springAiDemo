package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record CurrentAccountSummaryAndTransactionsResponse(
        @JsonProperty("currentAccountSummary") CurrentAccountSummary currentAccountSummary) {
}
