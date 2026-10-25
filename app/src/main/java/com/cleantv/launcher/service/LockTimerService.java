package com.cleantv.launcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.cleantv.launcher.LockActivity;
import com.cleantv.launcher.utils.PrefUtils;

public class LockTimerService extends Service {

    private final Handler handler = new Handler();
    private Runnable timerRunnable;

    public static void startTimer(Context context) {
        Intent intent = new Intent(context, LockTimerService.class);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMonitoring();
        return START_STICKY;
    }

    private void startMonitoring() {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int durationMinutes = PrefUtils.getLockDurationMinutes(LockTimerService.this);
                if (durationMinutes > 0) {
                    long startTime = PrefUtils.getLockStartTime(LockTimerService.this);
                    if (startTime == 0) {
                        startTime = System.currentTimeMillis();
                        PrefUtils.setLockStartTime(LockTimerService.this, startTime);
                    }

                    long elapsedMillis = System.currentTimeMillis() - startTime;
                    long limitMillis = (long) durationMinutes * 60 * 1000L;

                    if (elapsedMillis >= limitMillis || PrefUtils.isScreenLocked(LockTimerService.this)) {
                        // Kích hoạt khóa màn hình
                        PrefUtils.setScreenLocked(LockTimerService.this, true);
                        showLockScreen();
                    }
                }

                // Kiểm tra lại mỗi 5 giây
                handler.postDelayed(this, 5000);
            }
        };

        handler.post(timerRunnable);
    }

    private void showLockScreen() {
        Intent lockIntent = new Intent(this, LockActivity.class);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(lockIntent);
    }

    @Override
    public void onDestroy() {
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
