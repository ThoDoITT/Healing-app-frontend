package com.example.healingapp.network.models;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("id")
    private String id; // User ID

    @SerializedName("gender")
    private int gender; // 0, 1, hoặc giá trị int khác tùy theo API

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("height")
    private int height;

    @SerializedName("weight")
    private int weight;

    @SerializedName("birth_date")
    private String birthDate; // Định dạng "YYYY/MM/DD" hoặc theo API

    public UpdateProfileRequest(String id, int gender, String fullName, int height, int weight, String birthDate) {
        this.id = id;
        this.gender = gender;
        this.fullName = fullName;
        this.height = height;
        this.weight = weight;
        this.birthDate = birthDate;
    }

    // Getters (không bắt buộc nếu Gson truy cập trực tiếp, nhưng nên có)
    public String getId() { return id; }
    public int getGender() { return gender; }
    public String getFullName() { return fullName; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public String getBirthDate() { return birthDate; }
}
