package se.teknikhogskolan.springcasemanagement.service.exception;

public final class NoSearchResultException extends ServiceException {

    private static final long serialVersionUID = 1111170353744704871L;

    public NoSearchResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSearchResultException(String message) {
        super(message);
    }

    public NoSearchResultException() {
        super();
    }
}