
package com.kairemor.test.exception;


import com.kairemor.test.service.dto.UserDTO;

import javax.ws.rs.core.Response.StatusType;

public class KeycloakCreationException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public KeycloakCreationException(UserDTO user, StatusType status){
        super("An error occurred while trying to create user "+user+", error : "+ status);
    }


}
