package com.epharmacy.exception;

/**
 * Exception levée lors d'une violation de règle métier.
 *
 * @author E-PHARMACIE
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
