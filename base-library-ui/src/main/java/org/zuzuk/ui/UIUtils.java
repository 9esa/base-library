package org.zuzuk.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;

import org.zuzuk.ui.views.TypefaceSpan;

/**
 * Created by Gavriil Sitnikov on 15/10/2014.
 * Some utils for UI common issues
 */
public class UIUtils {

    /* Creates text with custom typeface for action bar title */
    public static CharSequence wrapTextWithTypeface(Context context, String text, Typeface typeface) {
        SpannableString spanTitle = new SpannableString(text);
        spanTitle.setSpan(new TypefaceSpan(typeface), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanTitle;
    }
}
