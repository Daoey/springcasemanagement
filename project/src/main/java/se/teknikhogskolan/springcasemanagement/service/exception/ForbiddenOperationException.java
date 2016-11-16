package se.teknikhogskolan.springcasemanagement.service.exception;

public final class ForbiddenOperationException extends ServiceException {
    
    private static final long serialVersionUID = -1117900468912370L;

    public ForbiddenOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenOperationException(String message) {
        super(message);
    }

    public ForbiddenOperationException() {
        super();
    }
}
