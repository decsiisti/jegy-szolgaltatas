package api.exception;

import model.ErrorResponse;

public class ErrorResponseException extends RuntimeException {
    private ErrorResponse error;

    public ErrorResponseException(ErrorResponse error) {
        super(error.getErrorCode().toString());
        this.error = error;
    }

    public ErrorResponse getError() {
        return error;
    }
}
