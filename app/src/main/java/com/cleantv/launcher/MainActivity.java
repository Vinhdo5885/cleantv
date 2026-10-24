package com.cleantv.launcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
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

    private TextView tvDate;
    private Button btnSettings;
    private LinearLayout layoutDockApps;
    private TextView tvTitleFavorites;
    private TvGridView gridAllApps;
    private TextView tvLoading;

    private List<AppModel> allApps = new ArrayList<>();
    private List<AppModel> favoriteApps = new ArrayList<>();
    private AppGridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        updateDate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInstalledApps();
    }

    private void initViews() {
        tvDate = (TextView) findViewById(R.id.tv_date);
        btnSettings = (Button) findViewById(R.id.btn_settings);
        layoutDockApps = (LinearLayout) findViewById(R.id.layout_dock_apps);
        tvTitleFavorites = (TextView) findViewById(R.id.tv_title_favorites);
        gridAllApps = (TvGridView) findViewById(R.id.grid_all_apps);
        tvLoading = (TextView) findViewById(R.id.tv_loading);

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
            // Viết hoa chữ cái đầu
            currentDate = currentDate.substring(0, 1).toUpperCase(new Locale("vi", "VN")) + currentDate.substring(1);
            tvDate.setText(currentDate);
        } catch (Exception e) {
            tvDate.setText("");
        }
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
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                tvLoading.setVisibility(View.GONE);
                allApps = tempAll;
                favoriteApps = tempFavs;

                renderDock();
                renderGrid();
            }
        }.execute();
    }

    private void renderDock() {
        layoutDockApps.removeAllViews();
        if (favoriteApps.isEmpty()) {
            tvTitleFavorites.setVisibility(View.GONE);
            layoutDockApps.setVisibility(View.GONE);
            return;
        }

        tvTitleFavorites.setVisibility(View.VISIBLE);
        layoutDockApps.setVisibility(View.VISIBLE);

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
                        v.animate().scaleX(1.10f).scaleY(1.10f).setDuration(120).start();
                        v.bringToFront();
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
            Toast.makeText(this, "Không thể mở ứng dụng: " + app.getLabel(), Toast.LENGTH_SHORT).show();
        }
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
        btnFav.setText(isFav ? "★  Bỏ ghim khỏi Mục Yêu Thích" : "★  Ghim lên Mục Yêu Thích");

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
                Toast.makeText(MainActivity.this, getString(R.string.msg_app_hidden), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "Không thể gỡ ứng dụng hệ thống", Toast.LENGTH_SHORT).show();
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
        Button btnUnhide = (Button) dialog.findViewById(R.id.btn_action_favorite);
        Button btnHide = (Button) dialog.findViewById(R.id.btn_action_hide);
        Button btnInfo = (Button) dialog.findViewById(R.id.btn_action_info);
        Button btnUninstall = (Button) dialog.findViewById(R.id.btn_action_uninstall);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_action_cancel);

        tvTitle.setText("Cài đặt CleanTV");
        btnOpenSettings.setText("⚙  Mở Cài đặt hệ thống TV");
        btnUnhide.setText("👁  Khôi phục tất cả ứng dụng đã ẩn");

        btnHide.setVisibility(View.GONE);
        btnInfo.setVisibility(View.GONE);
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

        btnUnhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                PrefUtils.unhideAll(MainActivity.this);
                Toast.makeText(MainActivity.this, getString(R.string.msg_unhidden_all), Toast.LENGTH_SHORT).show();
                loadInstalledApps();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Nút Menu trên điều khiển remote TV
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            View focused = getCurrentFocus();
            if (focused != null && focused.getTag() instanceof AppGridAdapter.ViewHolder) {
                // Kích hoạt dialog khi bấm nút Menu
                focused.performLongClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // Chặn nút Back để không bị thoát Launcher ra màn hình đen
    }
}
