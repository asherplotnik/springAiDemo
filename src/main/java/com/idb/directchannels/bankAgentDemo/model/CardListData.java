package com.idb.directchannels.bankAgentDemo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record CardListData(
        Double nextDebitTotalNIS,
        Double nextDebitTotalUSD,
        Double nextDebitTotalEuro,
        Integer maxNumOfCardsForDisplay,
        String nextDebitDateForDisplay,
        List<CardItem> cardList) {
}
