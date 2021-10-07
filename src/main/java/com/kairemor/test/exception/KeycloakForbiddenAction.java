package com.kairemor.test.exception;

public class KeycloakForbiddenAction extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public KeycloakForbiddenAction(String message){
        super(message);
    }
}
