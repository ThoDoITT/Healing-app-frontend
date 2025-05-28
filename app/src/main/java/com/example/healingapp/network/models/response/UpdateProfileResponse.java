package com.example.healingapp.network.models.response;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileResponse {
    @SerializedName("code")
    private String code;
    @SerializedName("error")
    private String error;
    @SerializedName("data")
    private Object data;

    public String getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    public Object getData() {
        return data;
    }


    public boolean isSuccessFromApi() {
        return "200".equals(code);
    }
}
