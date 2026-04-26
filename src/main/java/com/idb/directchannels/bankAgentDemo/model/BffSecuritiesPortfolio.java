package com.idb.directchannels.bankAgentDemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@Generated
public record BffSecuritiesPortfolio(
        String securitiesDepositID,
        Double securitiesPortfolioValue,
        Integer securitiesQuantity,
        Double israeliSecuritiesValue,
        Double foreignSecuritiesValue,
        Boolean yieldCalculationExists,
        Double yieldPortfolioValue,
        Double portfolioProfitLossAmount,
        Double portfolioProfitLossPercent,
        Boolean receiptsExist,
        Double receiptsValue,
        Integer openOrdersQuantity,
        Boolean isPortfolioProfitLossCalculated) {
}
