package se.teknikhogskolan.springcasemanagement.service.exception;

public final class NotAllowedException extends ServiceException {
    
    private static final long serialVersionUID = -1117900468912370L;

    public NotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAllowedException(String message) {
        super(message);
    }

    public NotAllowedException() {
        super();
    }
}
