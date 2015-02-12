package org.zuzuk.ui.views.hacked;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.text.TextPaint;
import android.util.AttributeSet;

import java.lang.reflect.Field;

/**
 * Created by Gavriil Sitnikov on 12/02/2015.
 * SwitchCompat with fixed bug of text size
 */
public class CustomSwitchCompat extends SwitchCompat {

    public CustomSwitchCompat(Context context) {
        super(context);
    }

    public CustomSwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setSwitchTextAppearance(Context context, int resid) {
        TypedArray appearance = context.obtainStyledAttributes(resid, new int[]{android.R.attr.textSize});
        int ts = appearance.getDimensionPixelSize(0, 0);

        try {
            Field field = SwitchCompat.class.getDeclaredField("mTextPaint");
            field.setAccessible(true);

            TextPaint textPaint = (TextPaint) field.get(this);
            textPaint.setTextSize(ts);

        } catch (Exception e) {
            e.printStackTrace();
        }

        appearance.recycle();

        super.setSwitchTextAppearance(context, resid);
    }
}
