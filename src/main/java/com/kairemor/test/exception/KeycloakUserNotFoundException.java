package com.kairemor.test.exception;

public class KeycloakUserNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public KeycloakUserNotFoundException(String username){
        super("User with username "+username+", not found");
    }
}
