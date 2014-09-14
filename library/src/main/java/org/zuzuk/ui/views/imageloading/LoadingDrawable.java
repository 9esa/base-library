package org.zuzuk.ui.views.imageloading;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

import java.lang.reflect.Field;

/**
 * Created by Gavriil Sitnikov on 07/09/2014.
 * Base drawable to use for loading images
 */
public abstract class LoadingDrawable extends Drawable implements ImageAware, ImageUrlParameters {
    private Drawable drawable;

    /* Indicates if image is possible to loading */
    protected boolean canLoad() {
        return true;
    }

    /* Start reloading image. Use it only when loading parameters is changed */
    protected void reload() {
        getImageLoader().cancelDisplayTask(this);
        if (canLoad()) {
            getImageLoader().displayImage(getUrl(), this, getDisplayImageOptions());
        }
    }

    private Callback getInternalCallback() {
        if (Build.VERSION.SDK_INT >= 11)
            return getCallback();
        else
            try {
                Field f = Drawable.class.getDeclaredField("mCallback");
                f.setAccessible(true);
                return (Callback) f.get(this);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
    }

    @Override
    public int getWidth() {
        return getBounds().width();
    }

    @Override
    public int getHeight() {
        return getBounds().height();
    }

    @Override
    public ViewScaleType getScaleType() {
        return ViewScaleType.FIT_INSIDE;
    }

    @Override
    public View getWrappedView() {
        Callback callback = getInternalCallback();
        if (callback instanceof View) {
            return (View) callback;
        }
        return null;
    }

    @Override
    public boolean isCollected() {
        return false;
    }

    @Override
    public boolean setImageDrawable(Drawable drawable) {
        if (this.drawable != null) {
            this.drawable.setCallback(null);
        }
        this.drawable = drawable;
        if (this.drawable != null) {
            this.drawable.setCallback(getInternalCallback());
        }
        invalidateSelf();
        return true;
    }

    @Override
    public boolean setImageBitmap(Bitmap bitmap) {
        return setImageDrawable(new BitmapDrawable(Resources.getSystem(), bitmap));
    }

    @Override
    public void draw(Canvas canvas) {
        if (drawable != null) {
            drawable.setBounds(getBounds());
            drawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (drawable != null) {
            drawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (drawable != null) {
            drawable.setColorFilter(cf);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public int getId() {
        return ((Object) this).hashCode();
    }
}
