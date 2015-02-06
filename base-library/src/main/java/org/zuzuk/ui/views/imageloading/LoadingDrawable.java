package org.zuzuk.ui.views.imageloading;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.reflect.Field;

/**
 * Created by Gavriil Sitnikov on 07/09/2014.
 * Base drawable to use for loading images
 */
public abstract class LoadingDrawable extends Drawable implements ImageAware, ImageLoadingObject {
    private Drawable drawable;
    private Matrix drawMatrix;
    private ImageView.ScaleType imageScaleType = ImageView.ScaleType.CENTER_CROP;
    private String lastLoadedUri = null;
    private ImageLoadingListener innerImageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            if (imageLoadingListener != null) {
                onLoadingStarted(imageUri, view);
            }
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (imageLoadingListener != null) {
                onLoadingFailed(imageUri, view, failReason);
            }
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            BitmapsGlobalRecycler.Instance.addReference(imageUri, loadedImage);
            lastLoadedUri = imageUri;
            if (imageLoadingListener != null) {
                onLoadingComplete(imageUri, view, loadedImage);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            if (imageLoadingListener != null) {
                onLoadingCancelled(imageUri, view);
            }
        }
    };

    private ImageLoadingListener imageLoadingListener;

    /* Sets listener to listen image loading events */
    public void setImageLoadingListener(ImageLoadingListener imageLoadingListener) {
        this.imageLoadingListener = imageLoadingListener;
    }

    /* Returns ImageView-like scale type */
    public ImageView.ScaleType getImageScaleType() {
        return imageScaleType;
    }

    /* Sets ImageView-like scale type */
    public void setImageScaleType(ImageView.ScaleType scaleType) {
        if (scaleType.equals(this.imageScaleType)) {
            return;
        }

        this.imageScaleType = scaleType;
        configureBounds();
    }

    /* Indicates if image is possible to loading */
    protected boolean canLoad() {
        return true;
    }

    /* Start reloading image. Use it only when loading parameters is changed */
    protected void reload() {
        drawable = null;
        getImageLoader().cancelDisplayTask(this);
        if (lastLoadedUri != null) {
            BitmapsGlobalRecycler.Instance.removeReference(lastLoadedUri);
            lastLoadedUri = null;
        }

        if (canLoad()) {
            getImageLoader().displayImage(getUrl(),
                    this,
                    getDisplayImageOptions(),
                    innerImageLoadingListener);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        configureBounds();
    }

    private Callback getInternalCallback() {
        if (Build.VERSION.SDK_INT >= 11) {
            return getCallback();
        } else {
            try {
                Field field = Drawable.class.getDeclaredField("mCallback");
                field.setAccessible(true);
                return (Callback) field.get(this);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void unscheduleSelf(Runnable what) {
        super.unscheduleSelf(what);
        if (getInternalCallback() == null) {
            getImageLoader().cancelDisplayTask(this);
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
        switch (imageScaleType) {
            case CENTER_CROP:
                return ViewScaleType.CROP;
            default:
                return ViewScaleType.FIT_INSIDE;
        }
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

        configureBounds();
        invalidateSelf();
        return true;
    }

    @Override
    public boolean setImageBitmap(Bitmap bitmap) {
        return setImageDrawable(new BitmapDrawable(Resources.getSystem(), bitmap));
    }

    private void configureBounds() {
        if (drawable == null) {
            drawMatrix = null;
            return;
        }

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        int width = getWidth();
        int height = getHeight();

        boolean fits = (drawableWidth < 0 || width == drawableWidth) &&
                (drawableHeight < 0 || height == drawableHeight);

        if (fits || drawableWidth <= 0 || drawableHeight <= 0
                || width == 0 || height == 0) {
            drawMatrix = null;
            return;
        }

        switch (imageScaleType) {
            case FIT_XY:
                drawMatrix = null;
                break;
            case CENTER: {
                drawMatrix = new Matrix();
                drawMatrix.setTranslate((int) ((width - drawableWidth) * 0.5f + 0.5f),
                        (int) ((height - drawableHeight) * 0.5f + 0.5f));
                break;
            }
            case CENTER_CROP: {
                drawMatrix = new Matrix();

                float scale;
                float dx = 0, dy = 0;

                if (drawableWidth * height > width * drawableHeight) {
                    scale = (float) height / (float) drawableHeight;
                    dx = (width - drawableWidth * scale) * 0.5f + 0.5f;
                } else {
                    scale = (float) width / (float) drawableWidth;
                    dy = (height - drawableHeight * scale) * 0.5f + 0.5f;
                }

                drawMatrix.setScale(scale, scale);
                drawMatrix.postTranslate((int) dx, (int) dy);
                break;
            }
            case FIT_CENTER:
            case CENTER_INSIDE: {
                drawMatrix = new Matrix();

                float scale = Math.min((float) width / (float) drawableWidth,
                        (float) height / (float) drawableHeight);

                float dx = (int) ((width - drawableWidth * scale) * 0.5f + 0.5f);
                float dy = (int) ((height - drawableHeight * scale) * 0.5f + 0.5f);

                drawMatrix.setScale(scale, scale);
                drawMatrix.postTranslate(dx, dy);
                break;
            }
            case MATRIX:
            case FIT_END:
            case FIT_START:
                throw new IllegalStateException("Scale type not supported: " + imageScaleType);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (drawable != null) {
            if (drawMatrix == null) {
                drawable.setBounds(getBounds());
                drawable.draw(canvas);
            } else {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                int saveCount = canvas.getSaveCount();
                canvas.save();
                canvas.concat(drawMatrix);
                drawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
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
