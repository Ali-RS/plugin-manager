package com.jayfella.plugin.manager.exception;

public class InvalidPluginDescriptionException extends Exception {

    private static final long serialVersionUID = -3791024724953336272L;

    public InvalidPluginDescriptionException() {
    }

    public InvalidPluginDescriptionException(String message) {
        super(message);
    }

    public InvalidPluginDescriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPluginDescriptionException(Throwable cause) {
        super(cause);
    }

    public InvalidPluginDescriptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
