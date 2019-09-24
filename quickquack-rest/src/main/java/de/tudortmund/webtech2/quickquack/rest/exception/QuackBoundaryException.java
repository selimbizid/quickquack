package de.tudortmund.webtech2.quickquack.rest.exception;

import de.tudortmund.webtech2.quickquack.rest.config.PredefinedHttpCodes;

public class QuackBoundaryException extends Exception {
    private int httpErrorCode;
    
    public QuackBoundaryException(String message, int code) {
        super(message);
        this.httpErrorCode = code;
    }
    
    public QuackBoundaryException(String message, PredefinedHttpCodes code) {
        this(message, code.getCode());
    }
    
    public QuackBoundaryException(String message) {
        this(message, PredefinedHttpCodes.INVALID_REQUEST);
    }
    
    public int getHttpErrorCode() {
        return this.httpErrorCode;
    }
}
