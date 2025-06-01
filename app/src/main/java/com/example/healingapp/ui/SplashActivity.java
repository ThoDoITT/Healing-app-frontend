package com.example.healingapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingapp.R;
import com.example.healingapp.ui.profile.ProfileActivity;
import com.example.healingapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private static final int SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(getApplicationContext());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                // if forget config profile -> config profile
                if (!sessionManager.isConfigProfile()) {
                    // set config profile
                    Intent configIntent = new Intent(SplashActivity.this, ProfileActivity.class);
//                    Intent configIntent = new Intent(SplashActivity.this, ConfigProfileActivity.class);
                    startActivity(configIntent);

                } else {
                    // islogin -> home
                    Log.d("SplashActivity", "Người dùng đã đăng nhập. Chuyển đến MainActivity.");
                    Intent mainIntent = new Intent(SplashActivity.this, HomeActivity.class); // Thay MainActivity bằng Activity chính của bạn
                    // Xóa các activity trước đó khỏi back stack
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                }
            } else {
                // Người dùng chưa đăng nhập -> Chuyển đến SigninActivity
                Log.d("SplashActivity", "Người dùng chưa đăng nhập. Chuyển đến SigninActivity.");
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
            finish();
        }, SPLASH_DELAY);
    }
}
