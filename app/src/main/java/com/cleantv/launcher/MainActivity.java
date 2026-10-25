package com.cleantv.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cleantv.launcher.adapter.AppGridAdapter;
import com.cleantv.launcher.model.AppModel;
import com.cleantv.launcher.service.LockTimerService;
import com.cleantv.launcher.utils.PrefUtils;
import com.cleantv.launcher.view.TvGridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity implements AppGridAdapter.OnAppActionListener {

    // Thành phần Top Shelf & Lời chào "Xin chào, Vinhdh"
    private ImageView shelfAppIcon;
    private TextView tvGreeting;
    private TextView shelfAppDesc;

    private TextView tvDate;
    private Button btnSettings;
    private LinearLayout layoutDockApps;
    private TextView tvTitleFavorites;
    private View scrollDock;
    private TvGridView gridAllApps;
    private TextView tvLoading;
    private Button btnAllApps;
    private TextView tvTitleAllApps;
    private boolean showingAllApps = false;

    private List<AppModel> allApps = new ArrayList<>();
    private List<AppModel> favoriteApps = new ArrayList<>();
    private AppGridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        updateDate();

        // Khởi động dịch vụ theo dõi hẹn giờ khóa màn hình
        LockTimerService.startTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Nếu thiết bị đang trong trạng thái bị khóa, lập tức mở màn hình khóa
        if (PrefUtils.isScreenLocked(this)) {
            Intent lockIntent = new Intent(this, LockActivity.class);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(lockIntent);
            return;
        }

        loadInstalledApps();
    }

    private void initViews() {
        shelfAppIcon = (ImageView) findViewById(R.id.shelf_app_icon);
        tvGreeting = (TextView) findViewById(R.id.tv_greeting);
        shelfAppDesc = (TextView) findViewById(R.id.shelf_app_desc);

        tvDate = (TextView) findViewById(R.id.tv_date);
        btnSettings = (Button) findViewById(R.id.btn_settings);
        layoutDockApps = (LinearLayout) findViewById(R.id.layout_dock_apps);
        tvTitleFavorites = (TextView) findViewById(R.id.tv_title_favorites);
        scrollDock = findViewById(R.id.scroll_dock);
        gridAllApps = (TvGridView) findViewById(R.id.grid_all_apps);
        tvLoading = (TextView) findViewById(R.id.tv_loading);
        btnAllApps = (Button) findViewById(R.id.btn_all_apps);
        tvTitleAllApps = (TextView) findViewById(R.id.tv_title_all_apps);
        btnAllApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showingAllApps = !showingAllApps;
                gridAllApps.setVisibility(showingAllApps ? View.VISIBLE : View.GONE);
                layoutDockApps.getParent();
                tvTitleFavorites.setVisibility(showingAllApps ? View.GONE : View.VISIBLE);
                scrollDock.setVisibility(showingAllApps ? View.GONE : View.VISIBLE);
                btnAllApps.setText(showingAllApps ? "‹  Về màn hình chính" : "Tất cả ứng dụng  ›");
                btnAllApps.requestFocus();
            }
        });

        tvGreeting.setText(PrefUtils.getUserGreeting(this));
        shelfAppDesc.setText("Hôm nay bạn muốn xem gì?");

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsMenu();
            }
        });
    }

    private void updateDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi", "VN"));
            String currentDate = sdf.format(new Date());
            currentDate = currentDate.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + currentDate.substring(1);
            tvDate.setText(currentDate);
        } catch (Exception e) {
            tvDate.setText("");
        }
    }

    public void updateTopShelf(AppModel app) {
        if (app == null) {
            shelfAppDesc.setText("Hôm nay bạn muốn xem gì?");
            return;
        }

        shelfAppIcon.setImageDrawable(app.getIcon());
        shelfAppDesc.setText("▶  " + app.getLabel() + "  •  Nhấn OK để mở");

        shelfAppDesc.setAlpha(0.6f);
        shelfAppDesc.animate().alpha(1.0f).setDuration(150).start();
    }

    private void loadInstalledApps() {
        new AsyncTask<Void, Void, Void>() {
            private List<AppModel> tempAll = new ArrayList<>();
            private List<AppModel> tempFavs = new ArrayList<>();

            @Override
            protected void onPreExecute() {
                if (allApps.isEmpty()) {
                    tvLoading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected Void doInBackground(Void... params) {
                PackageManager pm = getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                List<ResolveInfo> activities = pm.queryIntentActivities(mainIntent, 0);
                Set<String> hiddenApps = PrefUtils.getHiddenApps(MainActivity.this);
                Set<String> favorites = PrefUtils.getFavorites(MainActivity.this);
                String myPackage = getPackageName();

                if (activities != null) {
                    for (ResolveInfo ri : activities) {
                        String pkg = ri.activityInfo.packageName;
                        if (pkg.equals(myPackage)) continue;
                        if (hiddenApps.contains(pkg)) continue;

                        String label = ri.loadLabel(pm).toString();
                        Drawable icon = ri.loadIcon(pm);
                        String activityName = ri.activityInfo.name;

                        AppModel app = new AppModel(pkg, activityName, label, icon);
                        if (favorites.contains(pkg)) {
                            app.setFavorite(true);
                            tempFavs.add(app);
                        }
                        tempAll.add(app);
                    }
                }

                Collections.sort(tempAll);
                Collections.sort(tempFavs);

                if (tempFavs.isEmpty() && !tempAll.isEmpty()) {
                    int count = Math.min(tempAll.size(), 4);
                    for (int i = 0; i < count; i++) {
                        AppModel defaultFav = tempAll.get(i);
                        defaultFav.setFavorite(true);
                        tempFavs.add(defaultFav);
                        PrefUtils.addFavorite(MainActivity.this, defaultFav.getPackageName());
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                tvLoading.setVisibility(View.GONE);
                allApps = tempAll;
                favoriteApps = tempFavs;

                renderDock();
                renderGrid();

                if (!favoriteApps.isEmpty()) {
                    updateTopShelf(favoriteApps.get(0));
                } else if (!allApps.isEmpty()) {
                    updateTopShelf(allApps.get(0));
                }
            }
        }.execute();
    }

    private void renderDock() {
        layoutDockApps.removeAllViews();
        if (favoriteApps.isEmpty()) {
            tvTitleFavorites.setVisibility(View.GONE);
            scrollDock.setVisibility(View.GONE);
            return;
        }

        tvTitleFavorites.setVisibility(View.VISIBLE);
        scrollDock.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (final AppModel app : favoriteApps) {
            View card = inflater.inflate(R.layout.item_dock_card, layoutDockApps, false);
            ImageView img = (ImageView) card.findViewById(R.id.img_icon);
            TextView lbl = (TextView) card.findViewById(R.id.tv_label);

            img.setImageDrawable(app.getIcon());
            lbl.setText(app.getLabel());

            card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        v.animate().scaleX(1.10f).scaleY(1.10f).setDuration(140).start();
                        v.bringToFront();
                        updateTopShelf(app);
                    } else {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    }
                }
            });

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAppClick(app);
                }
            });

            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onAppLongClick(app);
                    return true;
                }
            });

            layoutDockApps.addView(card);
        }
    }

    private void renderGrid() {
        gridAdapter = new AppGridAdapter(this, allApps, this);
        gridAllApps.setAdapter(gridAdapter);
        gridAllApps.setVisibility(showingAllApps ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onAppClick(AppModel app) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName(app.getPackageName(), app.getActivityName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở: " + app.getLabel(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAppFocused(AppModel app) {
        updateTopShelf(app);
    }

    @Override
    public void onAppLongClick(final AppModel app) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app_options);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView imgIcon = (ImageView) dialog.findViewById(R.id.dialog_app_icon);
        TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_app_title);
        Button btnOpen = (Button) dialog.findViewById(R.id.btn_action_open);
        Button btnFav = (Button) dialog.findViewById(R.id.btn_action_favorite);
        Button btnHide = (Button) dialog.findViewById(R.id.btn_action_hide);
        Button btnInfo = (Button) dialog.findViewById(R.id.btn_action_info);
        Button btnUninstall = (Button) dialog.findViewById(R.id.btn_action_uninstall);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_action_cancel);

        imgIcon.setImageDrawable(app.getIcon());
        tvTitle.setText(app.getLabel());

        final boolean isFav = PrefUtils.isFavorite(this, app.getPackageName());
        btnFav.setText(isFav ? "★  Bỏ ghim khỏi Yêu Thích" : "★  Ghim lên Yêu Thích");

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onAppClick(app);
            }
        });

        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (isFav) {
                    PrefUtils.removeFavorite(MainActivity.this, app.getPackageName());
                } else {
                    PrefUtils.addFavorite(MainActivity.this, app.getPackageName());
                }
                loadInstalledApps();
            }
        });

        btnHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PrefUtils.hideApp(MainActivity.this, app.getPackageName());
                Toast.makeText(MainActivity.this, "Đã ẩn: " + app.getLabel(), Toast.LENGTH_SHORT).show();
                loadInstalledApps();
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Không thể mở cài đặt", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                    intent.setData(Uri.parse("package:" + app.getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Không thể gỡ ứng dụng", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showSettingsMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_app_options);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = (TextView) dialog.findViewById(R.id.dialog_app_title);
        Button btnOpenSettings = (Button) dialog.findViewById(R.id.btn_action_open);
        Button btnFilterVnpt = (Button) dialog.findViewById(R.id.btn_action_favorite);
        Button btnUnhideAll = (Button) dialog.findViewById(R.id.btn_action_hide);
        Button btnTimerLock = (Button) dialog.findViewById(R.id.btn_action_info);
        Button btnUninstall = (Button) dialog.findViewById(R.id.btn_action_uninstall);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_action_cancel);

        tvTitle.setText("Cài đặt CleanTV");
        btnOpenSettings.setText("⚙  Cài đặt hệ thống TV");
        btnFilterVnpt.setText("🧹  Dọn sạch & Ẩn toàn bộ app rác VNPT");
        btnUnhideAll.setText("👁  Khôi phục tất cả ứng dụng đã ẩn");

        // Nút Hẹn giờ khóa màn hình
        btnTimerLock.setVisibility(View.VISIBLE);
        int currentMinutes = PrefUtils.getLockDurationMinutes(this);
        if (currentMinutes > 0) {
            btnTimerLock.setText("⏱  Hẹn giờ khóa: Đang bật (" + currentMinutes + " phút)");
        } else {
            btnTimerLock.setText("⏱  Hẹn giờ khóa màn hình (15p, 30p, 1h...)");
        }

        btnUninstall.setVisibility(View.GONE);

        btnOpenSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Không thể mở cài đặt", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnFilterVnpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PrefUtils.restoreDefaultVnptFilter(MainActivity.this);
                Toast.makeText(MainActivity.this, "Đã kích hoạt bộ lọc rác VNPT!", Toast.LENGTH_SHORT).show();
                loadInstalledApps();
            }
        });

        btnUnhideAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PrefUtils.unhideAll(MainActivity.this);
                Toast.makeText(MainActivity.this, "Đã hiện lại tất cả ứng dụng", Toast.LENGTH_SHORT).show();
                loadInstalledApps();
            }
        });

        btnTimerLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showTimerSelectionDialog();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * Hộp thoại chọn thời gian cho phép xem TV trước khi khóa màn hình
     */
    private void showTimerSelectionDialog() {
        final String[] options = {
                "Tắt hẹn giờ (Không khóa)",
                "15 phút",
                "30 phút",
                "45 phút",
                "1 giờ (60 phút)",
                "2 giờ (120 phút)",
                "3 giờ (180 phút)"
        };
        final int[] minutesValues = {0, 15, 30, 45, 60, 120, 180};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thời gian xem TV trước khi khóa:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedMinutes = minutesValues[which];
                PrefUtils.setLockDurationMinutes(MainActivity.this, selectedMinutes);
                PrefUtils.setLockStartTime(MainActivity.this, System.currentTimeMillis());
                PrefUtils.setScreenLocked(MainActivity.this, false);

                if (selectedMinutes > 0) {
                    Toast.makeText(MainActivity.this, "✓ Đã bật hẹn giờ: Khóa sau " + selectedMinutes + " phút!", Toast.LENGTH_LONG).show();
                    LockTimerService.startTimer(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Đã tắt hẹn giờ khóa màn hình", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            View focused = getCurrentFocus();
            if (focused != null && focused.getTag() instanceof AppGridAdapter.ViewHolder) {
                focused.performLongClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // Chặn nút Back để không bị văng launcher
    }
}
