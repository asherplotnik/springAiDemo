package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record CurrentAccountSummary(
        CurrentAccountSummaryData data,
        MetaData metaData) {
}
