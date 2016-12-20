package se.teknikhogskolan.springcasemanagement.service.exception;

import se.teknikhogskolan.springcasemanagement.model.AbstractEntity;

public final class NotFoundException extends ServiceException {

    private static final long serialVersionUID = 2L;

    private Class<? extends AbstractEntity> missingEntity;

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException() {
        super();
    }

    public NotFoundException(String message) {
        super(message);
    }

    public Class<? extends AbstractEntity> getMissingEntity() {
        return missingEntity;
    }

    public NotFoundException setMissingEntity(Class<? extends AbstractEntity> missingEntity) {
        this.missingEntity = missingEntity;
        return this;
    }
}