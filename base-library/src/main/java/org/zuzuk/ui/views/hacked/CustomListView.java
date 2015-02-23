package org.zuzuk.ui.views.hacked;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ListView;

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
}
