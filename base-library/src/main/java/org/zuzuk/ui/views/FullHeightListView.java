package org.zuzuk.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import org.zuzuk.ui.views.hacked.CustomListView;

/**
 * Created by Gavriil Sitnikov on 03/10/2014.
 * ListView that measures with full height
 */
public class FullHeightListView extends CustomListView {

    public FullHeightListView(Context context) {
        super(context);
    }

    public FullHeightListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullHeightListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(0x00ffffff, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}