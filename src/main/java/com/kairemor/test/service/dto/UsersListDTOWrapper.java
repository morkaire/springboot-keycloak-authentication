package com.kairemor.test.service.dto;

import java.util.List;

public class UsersListDTOWrapper {
    private List<UserDTO> users;

    public List<UserDTO> getUsers() {
        return this.users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }
}
