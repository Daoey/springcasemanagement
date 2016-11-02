package se.teknikhogskolan.springcasemanagement.service;

public class ServiceException extends RuntimeException {

    protected ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ServiceException(String message) {
        super(message);
    }
}