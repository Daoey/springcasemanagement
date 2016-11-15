package se.teknikhogskolan.springcasemanagement.service;

public final class DatabaseException extends ServiceException {

    private static final long serialVersionUID = -2172912350506311252L;

    DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    DatabaseException(String message) {
        super(message);
    }

    DatabaseException() {
        super();
    }
}