package se.teknikhogskolan.springcasemanagement.service.exception;

public final class DatabaseException extends ServiceException {

    private static final long serialVersionUID = -1172912350506311252L;

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException() {
        super();
    }
}