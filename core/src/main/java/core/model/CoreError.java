package core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoreError {
    @JsonProperty
    private Long errorCode;

    @JsonProperty("message")
    private String errorMessage;

    public CoreError(Long errorCode, String message) {
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
