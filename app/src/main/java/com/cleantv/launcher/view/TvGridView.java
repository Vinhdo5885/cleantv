package com.cleantv.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class TvGridView extends GridView {

    public TvGridView(Context context) {
        super(context);
    }

    public TvGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TvGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Mở rộng toàn bộ chiều cao của GridView để cuộn mượt mà trong ScrollView
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
