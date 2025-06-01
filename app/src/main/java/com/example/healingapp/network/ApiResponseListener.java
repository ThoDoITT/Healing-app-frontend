package com.example.healingapp.network;

public interface ApiResponseListener<S, E> {
    void onSuccess(S response);
    void onError(E errorResponse);
}
