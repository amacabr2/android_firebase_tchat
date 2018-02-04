package com.example.antho.tchatfirebase.entities;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.Map;

/**
 * Created by antho on 04/02/2018.
 */

public class Message {

    private String uid;
    private String userId;
    private String username;
    private String content;
    private String imageUrl;
    private long date;

    public Message() {}

    public Message(String userId, String username, String content, String imageUrl) {
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Exclude
    public long getLongDate() {
        return date;
    }

    public Map<String, String> getDate() {
        return ServerValue.TIMESTAMP;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
