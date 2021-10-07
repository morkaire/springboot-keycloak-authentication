package com.kairemor.test.service.dto;

public class UserLogDTO {

    private String id;
    private String email;
    private int status;
    private int retry;
    private String details;

    public UserLogDTO(String id, String email, int status) {
        this.id = id;
        this.email = email;
        this.status = status;
    }

    public UserLogDTO(String id, String email, int status, String details) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.details = details ;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserLogDTO{" +
            "id='" + id + '\'' +
            ", email='" + email + '\'' +
            ", status=" + status +
            ", retry=" + retry +
            ", details=" + details +
            '}';
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
