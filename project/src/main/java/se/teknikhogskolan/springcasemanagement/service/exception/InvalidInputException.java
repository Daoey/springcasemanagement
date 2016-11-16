package se.teknikhogskolan.springcasemanagement.service.exception;

public final class InvalidInputException extends ServiceException {

    private static final long serialVersionUID = -155178842463683079L;

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException() {
        super();
    }
}
