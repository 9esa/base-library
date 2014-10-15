package org.zuzuk.ui.views.typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.zuzuk.ui.R;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * TextView that supports fonts from Typefaces class
 */
public class TypefacedTextView extends TextView {

    public TypefacedTextView(Context context) {
        super(context);
        initialize(context, null);
    }

    public TypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public TypefacedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    public void setTypeface(String name, int style) {
        setTypeface(Typefaces.getByName(getContext(), name), style);
    }

    public void setTypeface(String name) {
        setTypeface(name, Typeface.NORMAL);
    }

    private void initialize(Context context, AttributeSet attrs) {
        String customTypeface = null;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypefacedView);
            customTypeface = a.getString(R.styleable.TypefacedView_customTypeface);
            a.recycle();
        }

        if (customTypeface != null && !isInEditMode()) {
            Typeface typeface = getTypeface();
            setTypeface(customTypeface, typeface != null ? typeface.getStyle() : Typeface.NORMAL);
        }
    }
}