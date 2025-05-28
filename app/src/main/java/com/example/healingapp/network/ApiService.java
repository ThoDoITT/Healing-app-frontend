package com.example.healingapp.network;

import static com.example.healingapp.network.ConstRoute.API_LOGIN_USER;
import static com.example.healingapp.network.ConstRoute.API_REGISTER;
import static com.example.healingapp.network.ConstRoute.API_UPDATE_PROFILE;

import com.example.healingapp.network.models.LoginRequest;
import com.example.healingapp.network.models.LoginResponse;
import com.example.healingapp.network.models.RegisterRequest;
import com.example.healingapp.network.models.RegisterResponse;
import com.example.healingapp.network.models.UpdateProfileRequest;
import com.example.healingapp.network.models.response.UpdateProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST(API_REGISTER)
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);
    @POST(API_LOGIN_USER)
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);
    @POST(API_UPDATE_PROFILE)
    Call<UpdateProfileResponse> updateUserProfile(
            @Header("Authorization") String authToken,
            @Body UpdateProfileRequest profileRequest
    );
}
