package com.kairemor.test.service.dto;

/**
 * A DTO Representing a keycloak Group
 */
public class GroupDTO {

    private String id;

    private String name;

    public String getId() {
        return id;
    }

    public GroupDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
