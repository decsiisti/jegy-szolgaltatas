package core.exception;

import core.model.CoreError;

public class CoreErrorException extends RuntimeException {
    private CoreError error;

    public CoreErrorException(CoreError error) {
        super("Error " + error.getErrorCode() + ": " + error.getErrorMessage());
        this.error = error;
    }

    public CoreError getError() {
        return error;
    }
}
