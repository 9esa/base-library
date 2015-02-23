package org.zuzuk.ui.views.hacked;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * ScrollView that allows usage of onScroll listeners
 * <p/>
 * Created by azelikov on 9/4/2014.
 */
public class CustomScrollView extends ScrollView {

    /**
     * Listener to provide data about scroll changes.
     */
    public static interface OnScrollChangedListener {
        /**
         * Same method as
         * {@link android.view.View#onScrollChanged(int, int, int, int)}
         *
         * @param l    Current horizontal scroll origin.
         * @param t    Current vertical scroll origin.
         * @param oldl Previous horizontal scroll origin.
         * @param oldt Previous vertical scroll origin.
         */
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    private OnScrollChangedListener onScrollChangedListener;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        // Do not call super method to prevent scroll to occasional focused child.
    }
}
