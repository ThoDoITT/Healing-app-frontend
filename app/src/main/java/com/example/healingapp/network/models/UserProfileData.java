package com.example.healingapp.network.models;

import com.google.gson.annotations.SerializedName;

public class UserProfileData {
    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("avatar")
    private String avatar; // String or null

    @SerializedName("address")
    private String address; // String or null

    @SerializedName("user_group_id")
    private String userGroupId; // String or null, or another object type if applicable

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone; // String or null

    @SerializedName("status")
    private String status; // String or null, or int if it represents a status code

    @SerializedName("height")
    private int height;

    @SerializedName("weight")
    private int weight;

    @SerializedName("gender")
    private int gender;

    // Getters for the fields you need
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getAvatar() { return avatar; }
    public String getAddress() { return address; }
    public String getUserGroupId() { return userGroupId; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStatus() { return status; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public int getGender() { return gender; }
}
