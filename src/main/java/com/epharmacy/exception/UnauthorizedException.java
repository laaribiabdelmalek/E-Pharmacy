package com.epharmacy.exception;

/**
 * Exception levée lorsqu'un utilisateur non autorisé tente une action restreinte.
 *
 * @author E-PHARMACIE
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
