package org.zuzuk.ui.views.hacked;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Drawable that looks like old Hamburger
 */
public class OldUpIndicatorDrawable extends Drawable {
    private final Drawable logo;
    private final float logoPadding;

    private final Drawable upIndicator;
    private float upIndicatorPadding;
    private final float upIndicatorWidth;

    public OldUpIndicatorDrawable(Resources resources, Integer upIndicatorResourceId, int logoResourceId) {
        DisplayMetrics metrics = resources.getDisplayMetrics();

        logo = resources.getDrawable(logoResourceId);
        logoPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, metrics);

        if (upIndicatorResourceId != null) {
            upIndicator = resources.getDrawable(upIndicatorResourceId);
            upIndicatorPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics);
            upIndicatorWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        } else {
            upIndicator = null;
            upIndicatorPadding = 0;
            upIndicatorWidth = 0;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        float height = getBounds().height();

        drawCenterInside(logo, canvas,
                getBounds().width() - logoPadding * 2 - upIndicatorWidth, height - logoPadding * 2,
                logoPadding, upIndicatorWidth);

        if (upIndicator != null) {
            drawCenterInside(upIndicator, canvas,
                    upIndicatorWidth - upIndicatorPadding * 2, height - upIndicatorPadding * 2,
                    upIndicatorPadding, upIndicatorPadding * 2);
        }
    }

    private void drawCenterInside(Drawable drawable, Canvas canvas, float width, float height, float padding, float marginLeft) {
        Matrix drawMatrix = new Matrix();

        float scale = Math.min(width / (float) drawable.getIntrinsicWidth(),
                height / (float) drawable.getIntrinsicHeight());

        float dx = (int) ((width - drawable.getIntrinsicWidth() * scale) * 0.5f + 0.5f) + padding + marginLeft;
        float dy = (int) ((height - drawable.getIntrinsicHeight() * scale) * 0.5f + 0.5f) + padding;

        drawMatrix.setScale(scale, scale);
        drawMatrix.postTranslate(dx, dy);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.concat(drawMatrix);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
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
}
