package org.zuzuk.ui.views.typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import org.zuzuk.ui.R;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * EditText that supports fonts from Typefaces class
 */
public class TypefacedEditText extends EditText {

    public TypefacedEditText(Context context) {
        super(context);
        initialize(context, null);
    }

    public TypefacedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public TypefacedEditText(Context context, AttributeSet attrs, int defStyle) {
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