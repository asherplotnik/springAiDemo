package com.idb.directchannels.bankAgentDemo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Generated;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated
public record BankAgentExecuteResponse(
        String role,
        String content,
        @JsonProperty("tool_calls") List<Object> toolCalls,
        @JsonProperty("session_id") String sessionId) {
}
