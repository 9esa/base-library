package org.zuzuk.ui;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;

import org.zuzuk.ui.views.TypefaceSpan;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 15/10/2014.
 * Some utils for UI common issues
 */
public class UIUtils {

    /* Creates text with custom typeface for action bar title */
    public static CharSequence wrapTextWithTypeface(Context context, CharSequence text, Typeface typeface) {
        SpannableString spanTitle = new SpannableString(text);
        spanTitle.setSpan(new TypefaceSpan(typeface), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanTitle;
    }

    /* Creates intent filter based on list of actions */
    public static IntentFilter createIntentFilter(List<String> strings) {
        IntentFilter result = new IntentFilter();
        for (String eventName : strings) {
            result.addAction(eventName);
        }
        return result;
    }

    /* Returns if current thread is Main UI thread */
    public static boolean isCurrentThreadMain() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
