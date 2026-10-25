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
    private long lastTickMillis = 0L;
    private boolean lockScreenShown = false;

    public static void startTimer(Context context) {
        context.startService(new Intent(context, LockTimerService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMonitoring();
        return START_STICKY;
    }

    private void startMonitoring() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
        lastTickMillis = System.currentTimeMillis();
        timerRunnable = new Runnable() {
            @Override public void run() {
                long now = System.currentTimeMillis();
                long delta = Math.max(0L, Math.min(now - lastTickMillis, 60000L));
                lastTickMillis = now;

                boolean inHours = PrefUtils.isWithinAllowedHours(LockTimerService.this);
                int limitMinutes = PrefUtils.getDailyLimitMinutes(LockTimerService.this);
                long usedMillis = PrefUtils.getDailyUsedMillis(LockTimerService.this);
                long limitMillis = Math.max(0, limitMinutes) * 60000L;

                boolean dailyLimitReached = limitMinutes > 0 && usedMillis >= limitMillis;
                boolean timeExpired = !inHours || dailyLimitReached;

                if (timeExpired) {
                    PrefUtils.setScreenLocked(LockTimerService.this, true);
                    if (!lockScreenShown) {
                        lockScreenShown = true;
                        showLockScreen();
                    }
                } else {
                    if (PrefUtils.isScreenLocked(LockTimerService.this)) {
                        PrefUtils.setScreenLocked(LockTimerService.this, false);
                    }
                    lockScreenShown = false;
                    PrefUtils.addDailyUsedMillis(LockTimerService.this, delta);
                }
                handler.postDelayed(this, 5000L);
            }
        };
        handler.post(timerRunnable);
    }

    private void showLockScreen() {
        Intent intent = new Intent(this, LockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override public void onDestroy() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
