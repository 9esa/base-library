package org.zuzuk.ui.views.hacked;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Drawable that looks like old Hamburger
 */
public class OldHamburgerDrawable extends Drawable
        implements CustomActionBarDrawerToggle.DrawerToggle {
    private static final int DEFAULT_PAINT_FLAGS =
            Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;

    private Drawable logoBitmap;
    private float logoPadding;

    private float position = 0;
    private final Paint hamburgerPaint = new Paint(DEFAULT_PAINT_FLAGS);
    private final float hamburgerMaxWidth;
    private final float hamburgerWidthDifference;
    private final float hamburgerPartHeight;
    private final float hamburgerIntervalHeight;
    private final float hamburgerHeight;

    public OldHamburgerDrawable(Resources resources, int hamburgerColor, int logoResourceId) {
        DisplayMetrics metrics = resources.getDisplayMetrics();

        logoBitmap = resources.getDrawable(logoResourceId);
        logoPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, metrics);

        hamburgerPaint.setColor(hamburgerColor);
        float hamburgerMinWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
        hamburgerMaxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        hamburgerWidthDifference = hamburgerMaxWidth - hamburgerMinWidth;
        hamburgerPartHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, metrics);
        hamburgerIntervalHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
        hamburgerHeight = hamburgerPartHeight * 3 + hamburgerIntervalHeight * 2;
    }

    @Override
    public void draw(Canvas canvas) {
        float width = getBounds().width() - hamburgerMaxWidth - logoPadding * 2;
        float height = getBounds().height() - logoPadding * 2;

        Matrix drawMatrix = new Matrix();

        float scale = Math.min(width / (float) logoBitmap.getIntrinsicWidth(),
                height / (float) logoBitmap.getIntrinsicHeight());

        float dx = (int) ((width - logoBitmap.getIntrinsicWidth() * scale) * 0.5f + 0.5f) + logoPadding + hamburgerMaxWidth;
        float dy = (int) ((height - logoBitmap.getIntrinsicHeight() * scale) * 0.5f + 0.5f) + logoPadding;

        drawMatrix.setScale(scale, scale);
        drawMatrix.postTranslate(dx, dy);

        logoBitmap.setBounds(0, 0, logoBitmap.getIntrinsicWidth(), logoBitmap.getIntrinsicHeight());
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.concat(drawMatrix);
        logoBitmap.draw(canvas);
        canvas.restoreToCount(saveCount);

        float hamburgerDy = (int) ((height - hamburgerHeight) * 0.5f + 0.5f);

        canvas.drawRect(0, hamburgerDy,
                hamburgerMaxWidth - hamburgerWidthDifference * getPositionOfPart(position, 0.3f, 1f),
                hamburgerDy + hamburgerPartHeight,
                hamburgerPaint);

        hamburgerDy += hamburgerPartHeight + hamburgerIntervalHeight;
        canvas.drawRect(0, hamburgerDy,
                hamburgerMaxWidth - hamburgerWidthDifference * getPositionOfPart(position, 0.15f, 0.85f),
                hamburgerDy + hamburgerPartHeight,
                hamburgerPaint);

        hamburgerDy += hamburgerPartHeight + hamburgerIntervalHeight;
        canvas.drawRect(0, hamburgerDy,
                hamburgerMaxWidth - hamburgerWidthDifference * getPositionOfPart(position, 0f, 0.7f),
                hamburgerDy + hamburgerPartHeight,
                hamburgerPaint);
    }

    private float getPositionOfPart(float position, float start, float end) {
        if (position <= start) {
            return 0;
        }
        if (position >= end) {
            return 1;
        }
        return (position - start) / (end - start);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setPosition(float position) {
        this.position = position;
        invalidateSelf();
    }

    @Override
    public float getPosition() {
        return position;
    }
}
