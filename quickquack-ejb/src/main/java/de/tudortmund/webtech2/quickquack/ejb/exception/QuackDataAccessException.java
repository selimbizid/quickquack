package de.tudortmund.webtech2.quickquack.ejb.exception;

public class QuackDataAccessException extends Exception {
    private final Class<?> entityClass;
    private boolean withMessage;
    
    public QuackDataAccessException(Class<?> entityClass, Throwable t, String message) {
        super(message, t);
        this.entityClass = entityClass;
        this.withMessage = message != null;
    }
    
    public QuackDataAccessException(Class<?> entityClass, Throwable t) {
        this(entityClass, t, null);
    }

    public boolean hasCustomMessage() {
        return withMessage;
    }
    
    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    @Override
    public String toString() {
        return "Exception in " + entityClass.getName() + " data access object: " + this.getMessage();
    }
}
