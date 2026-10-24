package com.cleantv.launcher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cleantv.launcher.R;
import com.cleantv.launcher.model.AppModel;

import java.util.List;

public class AppGridAdapter extends BaseAdapter {

    public interface OnAppActionListener {
        void onAppClick(AppModel app);
        void onAppLongClick(AppModel app);
    }

    private final Context context;
    private final List<AppModel> appList;
    private final LayoutInflater inflater;
    private final OnAppActionListener listener;

    public AppGridAdapter(Context context, List<AppModel> appList, OnAppActionListener listener) {
        this.context = context;
        this.appList = appList;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public AppModel getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_app_card, parent, false);
            holder = new ViewHolder();
            holder.imgIcon = (ImageView) convertView.findViewById(R.id.img_icon);
            holder.tvLabel = (TextView) convertView.findViewById(R.id.tv_label);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AppModel app = getItem(position);
        holder.tvLabel.setText(app.getLabel());
        holder.imgIcon.setImageDrawable(app.getIcon());

        // Hiệu ứng phóng to mượt mà chuẩn Apple TV khi remote di chuyển đến
        convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    v.animate().scaleX(1.10f).scaleY(1.10f).setDuration(120).start();
                    // Đưa view lên trên cùng để viền không bị các view khác che
                    v.bringToFront();
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAppClick(app);
                }
            }
        });

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onAppLongClick(app);
                    return true;
                }
                return false;
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        public ImageView imgIcon;
        public TextView tvLabel;
    }
}
