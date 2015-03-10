package org.zuzuk.ui.views.hacked;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Gavriil Sitnikov on 23/02/2015.
 * Hacked list view to prevent errors with support library transition of fragments
 */
public class CustomListView extends ListView {

    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            // samsung error
        }
    }

    @NonNull
    private ArrayList<FixedViewInfo> getHeadersInfo() {
        try {
            Field field = ListView.class.getDeclaredField("mHeaderViewInfos");
            field.setAccessible(true);
            return (ArrayList<FixedViewInfo>) field.get(this);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    @NonNull
    private ArrayList<FixedViewInfo> getFootersInfo() {
        try {
            Field field = ListView.class.getDeclaredField("mFooterViewInfos");
            field.setAccessible(true);
            return (ArrayList<FixedViewInfo>) field.get(this);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private void setHeadersInfo(ArrayList<FixedViewInfo> headersInfo) {
        try {
            Field field = ListView.class.getDeclaredField("mHeaderViewInfos");
            field.setAccessible(true);
            field.set(this, headersInfo);
        } catch (Exception ignored) {
        }
    }

    private void setFootersInfo(ArrayList<FixedViewInfo> footersInfo) {
        try {
            Field field = ListView.class.getDeclaredField("mFooterViewInfos");
            field.setAccessible(true);
            field.set(this, footersInfo);
        } catch (Exception ignored) {
        }
    }

    private void clearFootersAndHeaders() {
        setFootersInfo(new ArrayList<FixedViewInfo>());
        setHeadersInfo(new ArrayList<FixedViewInfo>());
    }

    @Override
    public void addHeaderView(View v, Object data, boolean isSelectable) {
        if (getAdapter() != null) {
            super.setAdapter(new CustomHeaderViewListAdapter(getHeadersInfo(), getFootersInfo(), getAdapter()));
        }
        super.addHeaderView(v, data, isSelectable);
    }

    @Override
    public void addFooterView(View v, Object data, boolean isSelectable) {
        if (getAdapter() != null) {
            super.setAdapter(new CustomHeaderViewListAdapter(getHeadersInfo(), getFootersInfo(), getAdapter()));
        }
        super.addFooterView(v, data, isSelectable);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        ArrayList<FixedViewInfo> headersInfo = getHeadersInfo();
        ArrayList<FixedViewInfo> footersInfo = getFootersInfo();
        clearFootersAndHeaders();
        super.setAdapter(adapter);
        if (headersInfo.size() > 0 || footersInfo.size() > 0) {
            super.setAdapter(new CustomHeaderViewListAdapter(headersInfo, footersInfo, adapter));
        }
        setHeadersInfo(headersInfo);
        setFootersInfo(footersInfo);
    }

    private class CustomHeaderViewListAdapter extends HeaderViewListAdapter {

        public CustomHeaderViewListAdapter(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
            super(headerViewInfos, footerViewInfos, adapter);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                return super.getView(position, convertView, parent);
            } catch (Exception e) {
                return new View(parent.getContext());
            }
        }
    }
}
