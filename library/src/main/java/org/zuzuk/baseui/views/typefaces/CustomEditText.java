package org.zuzuk.baseui.views.typefaces;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import org.zuzuk.baseui.R;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * EditText that supports fonts from Typefaces class
 */
public class CustomEditText extends EditText {

    public CustomEditText(Context context) {
        this(context, null);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
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
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);
            externalFont = a.getString(R.styleable.CustomTextView_externalFont);
            a.recycle();
        } else {
            externalFont = Typefaces.getDefaultTypefaceName();
        }

        Typeface typeface = getTypeface();
        setTypeface(externalFont, typeface != null ? typeface.getStyle() : Typeface.NORMAL);
    }
}