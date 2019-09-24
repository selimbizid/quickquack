package de.tudortmund.webtech2.quickquack.ejb.exception;

public class QuackServiceException extends Exception {
    private boolean ignorable;
    
    public QuackServiceException(Throwable t, String message, boolean isIgnorable) {
        super(message, t);
        this.ignorable = isIgnorable;
    }
    
    public QuackServiceException(Throwable t, String message) {
        this(t, message, false);
    }
    
    public QuackServiceException(Throwable t) {
        this(t, null, false);
    }
    
    public QuackServiceException(String message) {
        this(null, message, false);
    }

    public boolean isIgnorable() {
        return ignorable;
    }
}
