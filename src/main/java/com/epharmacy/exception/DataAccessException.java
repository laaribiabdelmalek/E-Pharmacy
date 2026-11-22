package com.epharmacy.exception;

/**
 * Exception levée lors d'une erreur d'accès aux données (couche DAO).
 *
 * @author E-PHARMACIE
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
