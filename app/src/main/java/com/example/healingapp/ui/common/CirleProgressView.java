package com.example.healingapp.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CirleProgressView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;

    private RectF arcRect = new RectF();
    private float progress = 0; // 0-100

    public CirleProgressView(Context context) {
        super(context);
        init();
    }

    public CirleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Vòng tròn nền
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(20f);

        // Vòng tròn tiến trình
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(Color.BLUE);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(20f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        // Text phần trăm
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(50f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float size = Math.min(w, h) - 20; // Trừ đi padding
        float left = (w - size) / 2f;
        float top = (h - size) / 2f;

        arcRect.set(left, top, left + size, top + size);

        // Cập nhật text size theo kích thước view
        textPaint.setTextSize(size * 0.2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Vẽ vòng tròn nền
        canvas.drawArc(arcRect, 0, 360, false, backgroundPaint);

        // Vẽ phần tiến trình (bắt đầu từ góc -90 độ - đỉnh trên cùng)
        float sweepAngle = 360 * (progress / 100f);
        canvas.drawArc(arcRect, -90, sweepAngle, false, progressPaint);

        // Vẽ text phần trăm
        String text = String.format("%d%%", (int)progress);
        float textY = arcRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2;
        canvas.drawText(text, arcRect.centerX(), textY, textPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.min(100, Math.max(0, progress)); // Đảm bảo trong khoảng 0-100
        invalidate(); // Yêu cầu vẽ lại
    }

    public float getProgress() {
        return progress;
    }

    public void setProgressColor(int color) {
        progressPaint.setColor(color);
        invalidate();
    }

    public void setBackgroundColor(int color) {
        backgroundPaint.setColor(color);
        invalidate();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }
    public void setCircleWidth(float width) {
        backgroundPaint.setStrokeWidth(width);
        progressPaint.setStrokeWidth(width);
        invalidate();
    }
}
