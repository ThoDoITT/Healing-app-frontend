package com.example.healingapp.network.models.response;

import com.example.healingapp.network.models.UserProfileData;
import com.google.gson.annotations.SerializedName;

public class GetUserProfileResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("error")
    private Object error; // Can be String or null

    @SerializedName("data")
    private UserProfileData data;

    // Getters
    public String getCode() { return code; }
    public Object getError() { return error; }
    public UserProfileData getData() { return data; }

    // Helper method to check if the API call was logically successful
    public boolean isSuccess() {
        return "200".equals(code) && data != null;
    }
}
