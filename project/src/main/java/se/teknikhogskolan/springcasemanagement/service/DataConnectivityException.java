package se.teknikhogskolan.springcasemanagement.service;

public class DataConnectivityException extends ServiceException {

    private static final long serialVersionUID = 1086295941726559958L;

    protected DataConnectivityException(String message, Throwable cause) {
        super(message, cause);
    }

    protected DataConnectivityException(String message) {
        super(message);
    }

    protected DataConnectivityException() {
        super();
    }

}
