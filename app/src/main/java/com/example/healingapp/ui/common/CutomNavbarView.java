package com.example.healingapp.ui.common;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.healingapp.R;
import com.example.healingapp.ui.HomeActivity;
import com.example.healingapp.ui.body.body1Activity;
import com.example.healingapp.ui.profile.ProfileViewActivity;
import com.example.healingapp.ui.sleep.StartSleepActivity;
import com.example.healingapp.ui.workout.RunningActivity;
import com.example.healingapp.ui.workout.SelectWorkoutActivity;

public class CutomNavbarView extends CoordinatorLayout {
    private Context mContext;
    private LinearLayout navHomeItem, navbarWorkout, navbarProfile, navbarSleep;
    private ImageButton navAddButton;

    // Các Activity tương ứng (bạn cần tạo các Activity này)
    private Class<?> homeActivityClass = HomeActivity.class;
    private Class<?> body1ActivityClass = body1Activity.class;
    private Class<?> sleepActivityClass = StartSleepActivity.class;
    private Class<?> ProfileActivityClass = body1Activity.class;
    private Class<?> selectWorkout = SelectWorkoutActivity.class;
    private Class<?> profileView = ProfileViewActivity.class;
    public CutomNavbarView(Context context) {
        super(context);
        init(context, null);
    }

    public CutomNavbarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CutomNavbarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        mContext = context;
        // Inflate layout XML của bạn vào custom view này.
        // File XML này chính là file bạn đã cung cấp, đã được sửa ID.
        LayoutInflater.from(context).inflate(R.layout.view_custom_nav, this, true);

        // Ánh xạ các View bằng ID duy nhất đã đặt
        navHomeItem = findViewById(R.id.navHomeItem);
        navbarWorkout = findViewById(R.id.navbarWorkout);
        navbarSleep = findViewById(R.id.navbarSleep);
        navbarProfile = findViewById(R.id.navbarProfile);
        navAddButton = findViewById(R.id.btnAddNavbar);

        // Set sự kiện click
        setupClickListeners();
    }

    private void setupClickListeners() {
        if (navHomeItem != null) {
            navHomeItem.setOnClickListener(v -> navigateTo(homeActivityClass));
        }
        if (navbarWorkout != null) {
            navbarWorkout.setOnClickListener(v -> navigateTo(selectWorkout));
        }
        if (navbarSleep != null) {
            navbarSleep.setOnClickListener(v -> navigateTo(sleepActivityClass));
        }

        if (navbarProfile != null) {
            navbarProfile.setOnClickListener(v -> navigateTo(profileView));
        }

    }

    private void navigateTo(Class<?> activityClass) {
        if (activityClass != null && mContext != null) {
            Intent intent = new Intent(mContext, activityClass);
            // Bạn có thể thêm cờ cho Intent nếu cần, ví dụ:
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }

    // Các phương thức public để tùy chỉnh hành động hoặc đích đến nếu cần từ Activity
//    public void setNavigationActivity(NavItem item, Class<?> activityClass) {
//        switch (item) {
//            case HOME:
//                homeActivityClass = activityClass;
//                break;
////            case BUILD:
////                buildActivityClass = activityClass;
////                break;
////            case ADD:
////                addActivityClass = activityClass;
////                break;
////            case CHART:
////                chartActivityClass = activityClass;
////                break;
////            case USER:
////                userActivityClass = activityClass;
////                break;
//        }
//        // Gọi lại setupClickListeners nếu bạn muốn cập nhật ngay lập tức
//        // hoặc đảm bảo rằng các listener sử dụng các biến class này
//    }

//    public enum NavItem {
//        HOME, BUILD, ADD, CHART, USER
//    }
}
