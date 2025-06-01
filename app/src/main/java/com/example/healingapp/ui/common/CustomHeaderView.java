package com.example.healingapp.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.healingapp.R;

public class CustomHeaderView extends ConstraintLayout {
    private ImageButton backButton;
    // private TextView titleTextView; // Bỏ dòng này


    public CustomHeaderView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CustomHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomHeaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, @Nullable AttributeSet attrs) {
        // Inflate layout XML của header vào custom view này
        LayoutInflater.from(context).inflate(R.layout.view_common_header, this, true); // Đặt tên file XML của bạn ở đây

        backButton = findViewById(R.id.imageButton3);
        // titleTextView = findViewById(R.id.textView7); // Bỏ dòng này

        // Set sự kiện click mặc định cho nút back
        if (backButton != null) {
            backButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getContext() instanceof Activity) {
                        ((Activity) getContext()).finish();
                        Activity currentActivity = (Activity) getContext();


                    }

                }
            });
        }

    }



    /**
     * Lấy ImageButton của nút back để tùy chỉnh thêm nếu cần.
     * @return ImageButton của nút back.
     */
    public ImageButton getBackButton() {
        return backButton;
    }

    /**
     * Cho phép ghi đè hành động của nút back từ Activity.
     * @param listener OnClickListener cho nút back.
     */
    public void setBackButtonClickListener(OnClickListener listener) {
        if (backButton != null) {
            backButton.setOnClickListener(listener);
        }
    }

}
