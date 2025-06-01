package com.example.healingapp.network.models;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterResponse {
    @SerializedName("code")
    private String code;
    @SerializedName("error")
    private String error;
    @SerializedName("data")
    private Object data;
}
