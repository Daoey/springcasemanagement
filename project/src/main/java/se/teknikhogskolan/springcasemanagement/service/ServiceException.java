package se.teknikhogskolan.springcasemanagement.service;

public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 4662570353744704876L;

    protected ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ServiceException(String message) {
        super(message);
    }
}