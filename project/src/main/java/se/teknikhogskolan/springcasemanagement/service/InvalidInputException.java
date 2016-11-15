package se.teknikhogskolan.springcasemanagement.service;

public class InvalidInputException extends ServiceException {

    private static final long serialVersionUID = -255178842463683079L;

    protected InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    protected InvalidInputException(String message) {
        super(message);
    }

    protected InvalidInputException() {
        super();
    }
}
