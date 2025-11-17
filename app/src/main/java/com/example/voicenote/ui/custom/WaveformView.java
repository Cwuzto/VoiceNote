package com.example.voicenote.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {

    private Paint paint;
    private float[] amplitudes;
    private int numBars;
    private float currentTargetAmplitude = 0f; // Biên độ mục tiêu hiện tại (sẽ giảm dần)
    private float currentMaxHeight = 0f;       // Cache chiều cao tối đa

    // === Thông số Tùy chỉnh ===

    // 1. Độ thẩm mỹ (Mượt hơn / Đẹp hơn)
    private int barWidth = 6;           // Độ rộng cột nhỏ hơn (trước là 8)
    private int barSpace = 4;           // Khoảng cách cột nhỏ hơn (trước là 6)
    // -> Nhiều cột hơn, trông "mịn" hơn

    private static final float DECAY_FACTOR = 0.85f; // Sóng "rộng" hơn (trước là 0.7f)
    private static final float VERTICAL_STRETCH = 2.2f; // Kéo dài sóng theo chiều dọc (1.0 = bình thường)

    // 2. Độ nhạy / Phản hồi (Nhạy hơn)
    private static final float SMOOTHING = 0.5f;     // Phản hồi nhanh hơn (trước là 0.3f)
    private static final float AMP_SCALE = 30f;      // Nhạy hơn với âm thanh nhỏ (trước là 50f)
    private static final float TARGET_DECAY = 0.9f;  // Tốc độ "quên" biên độ khi không có input

    // ============================

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(0xFF1A73E8);
        paint.setStrokeWidth(barWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        currentMaxHeight = h / 2f; // Cache lại chiều cao
        numBars = w / (barWidth + barSpace);
        if (numBars % 2 == 0) numBars--; // Đảm bảo số lẻ để có cột trung tâm

        amplitudes = new float[numBars];
    }

    /**
     * Nhận biên độ mới.
     * Hàm này chỉ "đặt mục tiêu" mới, việc cập nhật animation sẽ ở onDraw.
     */
    public void addAmplitude(int amp) {
        if (numBars == 0 || currentMaxHeight == 0) return;

        // Scale biên độ và giới hạn
        float target = amp / AMP_SCALE;
        if (target > currentMaxHeight) target = currentMaxHeight;

        // Đặt biên độ mục tiêu mới
        // Chỉ lấy giá trị cao hơn để sóng "phản ứng" với đỉnh
        if (target > currentTargetAmplitude) {
            this.currentTargetAmplitude = target;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (amplitudes == null || numBars == 0) return;

        int centerY = getHeight() / 2;
        int mid = numBars / 2;

        // === PHẦN 1: CẬP NHẬT LOGIC SÓNG ===
        // Logic này được chuyển từ addAmplitude vào onDraw
        // để tạo ra một vòng lặp animation mượt mà.

        // 1. Cập nhật cột trung tâm (hướng về currentTargetAmplitude)
        amplitudes[mid] += (currentTargetAmplitude - amplitudes[mid]) * SMOOTHING;

        // 2. Lan sóng sang hai bên
        for (int i = 1; i <= mid; i++) {
            float sideTarget = amplitudes[mid] * (float) Math.pow(DECAY_FACTOR, i);

            amplitudes[mid - i] += (sideTarget - amplitudes[mid - i]) * SMOOTHING;
            amplitudes[mid + i] += (sideTarget - amplitudes[mid + i]) * SMOOTHING;
        }

        // 3. Giảm dần biên độ mục tiêu (TARGET_DECAY)
        // Điều này làm cho sóng tự động "lắng xuống" khi không có âm thanh
        // thay vì bị "đóng băng" như code cũ.
        currentTargetAmplitude *= TARGET_DECAY;
        if (currentTargetAmplitude < 0.1f) {
            currentTargetAmplitude = 0f;
        }


        // === PHẦN 2: VẼ SÓNG ===
        int x = 0;
        for (float amp : amplitudes) {
            // Áp dụng kéo dài chiều dọc
            float stretchedAmp = amp * VERTICAL_STRETCH;

            canvas.drawLine(
                    x,
                    centerY - stretchedAmp,
                    x,
                    centerY + stretchedAmp,
                    paint
            );
            x += barWidth + barSpace;
        }

        // Yêu cầu vẽ lại ở khung hình tiếp theo
        // Đây là mấu chốt để tạo ra animation liên tục
        postInvalidateOnAnimation();
    }
}