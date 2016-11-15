package se.teknikhogskolan.springcasemanagement.service;

public class ForbiddenOperationException extends ServiceException {
    private static final long serialVersionUID = -8087900468912370L;

    protected ForbiddenOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ForbiddenOperationException(String message) {
        super(message);
    }

    protected ForbiddenOperationException() {
        super();
    }
}
