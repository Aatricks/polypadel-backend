package com.polypadel.auth.dto;

public class LoginResponse {
    private String token;
    private UserSummary user;

    public LoginResponse() {}
    public LoginResponse(String token, UserSummary user) {
        this.token = token; this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserSummary getUser() { return user; }
    public void setUser(UserSummary user) { this.user = user; }
}
