package se.teknikhogskolan.springcasemanagement.service.exception;

public final class DuplicateValueException extends ServiceException {

    private static final long serialVersionUID = 1829172638473629182L;

    public DuplicateValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateValueException(String message) {
        super(message);
    }

    public DuplicateValueException() {
    }
}
