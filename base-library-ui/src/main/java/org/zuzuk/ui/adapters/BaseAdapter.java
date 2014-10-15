package org.zuzuk.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Adapter that forces developer to override only 2 methods:
 * 1) newView where developer should create view
 * 2) bindView where developer should fill view with data
 */
public abstract class BaseAdapter<TItem> extends android.widget.BaseAdapter {
    private final HashMap<View, HashMap<Integer, View>> viewsHolder = new HashMap<>();

    /* Returns item of special class by position */
    public abstract TItem get(int position);

    @Override
    public Object getItem(int position) {
        return get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = newView(position, LayoutInflater.from(parent.getContext()), parent);
        }
        bindView(view, get(position), position);
        return view;
    }

    /**
     * Create view for item here. If view is using in several adapters then create special
     * class that extends view
     */
    protected abstract View newView(int position, LayoutInflater inflater, ViewGroup parent);

    /* Fills view with item data */
    protected abstract void bindView(View view, TItem item, int position);

    /* Finds view by id in parent layout and caches it in viewsHolder */
    @SuppressWarnings("unchecked")
    protected <TView extends View> TView findViewById(View parent, int viewId) {
        HashMap<Integer, View> parentViews = viewsHolder.get(parent);
        if (parentViews == null) {
            parentViews = new HashMap<>();
            viewsHolder.put(parent, parentViews);
        }
        View result = parentViews.get(viewId);
        if (result == null) {
            result = parent.findViewById(viewId);
            parentViews.put(viewId, result);
        }
        return (TView) result;
    }
}
