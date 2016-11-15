package se.teknikhogskolan.springcasemanagement.service;

public final class NoSearchResultException extends ServiceException {

    private static final long serialVersionUID = 1111170353744704876L;

    NoSearchResultException(String message, Throwable cause) {
        super(message, cause);
    }

    NoSearchResultException(String message) {
        super(message);
    }

    NoSearchResultException() {
        super();
    }
}