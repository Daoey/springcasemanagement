package se.teknikhogskolan.springcasemanagement.service;

public class ServiceException extends RuntimeException { // TODO make abstract(?) when services and tests updated so they uses DatabaseException instead

    private static final long serialVersionUID = 6529570775397439370L;

    protected ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ServiceException(String message) {
        super(message);
    }

    protected ServiceException() {
        super();
    }
}