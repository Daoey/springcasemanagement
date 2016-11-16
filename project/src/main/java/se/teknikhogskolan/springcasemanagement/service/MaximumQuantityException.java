package se.teknikhogskolan.springcasemanagement.service;

/**
 * Created by Martin on 2016-11-16.
 */
public final class MaximumQuantityException extends ServiceException {

    private static final long serialVersionUID = -2156498542156325894L;

    public MaximumQuantityException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaximumQuantityException(String message) {
        super(message);
    }

    public MaximumQuantityException() {
    }
}
