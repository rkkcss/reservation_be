package hu.daniinc.reservation.web.rest.errors;

import org.springframework.http.HttpStatus;

public class GeneralException extends RuntimeException {

    private final String errorKey;
    private final HttpStatus status;

    public GeneralException(String message, String errorKey, HttpStatus status) {
        super(message);
        this.errorKey = errorKey;
        this.status = status;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
