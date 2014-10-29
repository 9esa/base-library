package org.zuzuk.ui.views;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * Created by Gavriil Sitnikov on 15/10/2014.
 * Span to wrap text with custom typeface
 */
public class TypefaceSpan extends MetricAffectingSpan {
    private final Typeface typeface;

    public TypefaceSpan(Typeface typeface) {
        this.typeface = typeface;
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTypeface(typeface);
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.setTypeface(typeface);
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}