package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record BffCardList(CardListData data, MetaData metaData) {
}
