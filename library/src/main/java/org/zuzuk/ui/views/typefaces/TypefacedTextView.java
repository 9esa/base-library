package org.zuzuk.ui.views.typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.zuzuk.R;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * TextView that supports fonts from Typefaces class
 */
public class TypefacedTextView extends TextView {

    public TypefacedTextView(Context context) {
        this(context, null);
    }

    public TypefacedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypefacedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    public void setTypeface(String name, int style) {
        setTypeface(Typefaces.getByName(name), style);
    }

    public void setTypeface(String name) {
        setTypeface(name, Typeface.NORMAL);
    }

    private void initialize(Context context, AttributeSet attrs) {
        String externalFont;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypefacedView);
            externalFont = a.getString(R.styleable.TypefacedView_customTypeface);
            a.recycle();
        } else {
            externalFont = Typefaces.getDefaultTypefaceName();
        }

        Typeface typeface = getTypeface();
        setTypeface(externalFont, typeface != null ? typeface.getStyle() : Typeface.NORMAL);
    }
}