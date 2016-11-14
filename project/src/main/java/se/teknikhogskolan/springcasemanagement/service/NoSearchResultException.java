package se.teknikhogskolan.springcasemanagement.service;

public final class NoSearchResultException extends ServiceException {

    private static final long serialVersionUID = 1111170353744704876L;

    protected NoSearchResultException(String message, Throwable cause) {
        super(message, cause);
    }

    protected NoSearchResultException(String message) {
        super(message);
    }

    protected NoSearchResultException() {
        super();
    }
}