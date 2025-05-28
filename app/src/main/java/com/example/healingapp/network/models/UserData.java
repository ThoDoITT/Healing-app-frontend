package com.example.healingapp.network.models;

import com.google.gson.annotations.SerializedName;

public class UserData {
    @SerializedName("token")
    private String token;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("username")
    private String username;

    @SerializedName("full_name")
    private String fullName; // Có thể null

    @SerializedName("avatar")
    private String avatar; // Có thể null
    @SerializedName("is_config_profile")
    private boolean isConfigProfile;

    // Getters
    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isConfigProfile() { return isConfigProfile; }
    public String getFullName() { return fullName; }
    public String getAvatar() { return avatar; }
}
