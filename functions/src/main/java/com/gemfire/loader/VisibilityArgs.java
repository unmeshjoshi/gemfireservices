package com.gemfire.loader;

public class VisibilityArgs {
    private String username;
    private String password;

    public VisibilityArgs(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
