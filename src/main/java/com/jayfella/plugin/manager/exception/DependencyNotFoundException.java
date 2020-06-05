package com.jayfella.plugin.manager.exception;

public class DependencyNotFoundException extends Exception {

    private static final long serialVersionUID = 314168362230971177L;

    public DependencyNotFoundException() {
    }

    public DependencyNotFoundException(String message) {
        super(message);
    }

    public DependencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DependencyNotFoundException(Throwable cause) {
        super(cause);
    }

    public DependencyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
