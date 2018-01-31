package com.example.antho.tchatfirebase.entities;

/**
 * Created by antho on 31/01/2018.
 */

public class User {

    private String id;

    private String username;

    public User() {}

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
