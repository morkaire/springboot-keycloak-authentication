package com.kairemor.test.exception;

public class KeycloakVerificationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public KeycloakVerificationException(String message){
        super("An error occurs during the verification of the token provider: "+ message);
    }

}
