package com.kairemor.test.service.dto;

import java.util.List;

public class UserWithGroupDTO {
    private String userReference;

    private List<GroupDTO> oldRoles;

    private List<GroupDTO> newRoles;

    public UserWithGroupDTO(String userReference, List<GroupDTO> oldRoles, List<GroupDTO> newRoles) {
        this.userReference = userReference;
        this.oldRoles = oldRoles;
        this.newRoles = newRoles;
    }

    public String getUserReference() {
        return userReference;
    }

    public void setUserReference(String userReference) {
        this.userReference = userReference;
    }

    public List<GroupDTO> getOldRoles() {
        return oldRoles;
    }

    public void setOldRoles(List<GroupDTO> oldRoles) {
        this.oldRoles = oldRoles;
    }

    public List<GroupDTO> getNewRoles() {
        return newRoles;
    }

    public void setNewRoles(List<GroupDTO> newRoles) {
        this.newRoles = newRoles;
    }
}

