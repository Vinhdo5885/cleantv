package com.cleantv.launcher.model;

import android.graphics.drawable.Drawable;

public class AppModel implements Comparable<AppModel> {
    private String packageName;
    private String activityName;
    private String label;
    private Drawable icon;
    private boolean isFavorite;

    public AppModel(String packageName, String activityName, String label, Drawable icon) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.label = label;
        this.icon = icon;
        this.isFavorite = false;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getLabel() {
        return label != null ? label : "";
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public int compareTo(AppModel other) {
        return this.getLabel().compareToIgnoreCase(other.getLabel());
    }
}
