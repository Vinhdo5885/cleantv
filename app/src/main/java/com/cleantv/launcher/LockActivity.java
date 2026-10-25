package com.cleantv.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.cleantv.launcher.utils.PrefUtils;

public class LockActivity extends Activity {
    private final Handler handler = new Handler();
    private final Runnable unlockCheck = new Runnable() {
        @Override public void run() {
            if (!PrefUtils.isScreenLocked(LockActivity.this)) {
                finish();
                return;
            }
            handler.postDelayed(this, 3000L);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(unlockCheck);
        handler.post(unlockCheck);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(unlockCheck);
        super.onPause();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Không có mã PIN mở khóa ở màn hình này; PIN chỉ bảo vệ Cài đặt.
        // Chặn Back/OK để tránh đóng màn hình khóa bằng remote.
        if (keyCode == KeyEvent.KEYCODE_BACK ||
                keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                keyCode == KeyEvent.KEYCODE_ENTER) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override public void onBackPressed() {
        // Màn hình khóa chỉ tự đóng khi dịch vụ cho phép xem lại.
    }
}
