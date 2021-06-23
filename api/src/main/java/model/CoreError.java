package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoreError {
    @JsonProperty
    private Long errorCode;

    @JsonProperty("message")
    private String errorMessage;

    public Long getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

