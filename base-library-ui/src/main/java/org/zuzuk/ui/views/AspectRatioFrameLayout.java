package org.zuzuk.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.zuzuk.ui.R;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * FrameLayout that holds special aspect ratio sizes
 */
public class AspectRatioFrameLayout extends FrameLayout {
    private final static float DEFAULT_ASPECT_RATIO = 1.0f;
    private float aspectRatio;
    private boolean wrapToContent;

    /* Returns aspect ratio of layout */
    public float getAspectRatio() {
        return aspectRatio;
    }

    /* Sets aspect ratio of layout */
    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio == this.aspectRatio) {
            return;
        }

        this.aspectRatio = aspectRatio;
        requestLayout();
    }

    /* Returns if layout is wrapping to content but holds aspect ratio */
    public boolean isWrapToContent() {
        return wrapToContent;
    }

    /* Sets if layout is wrapping to content but holds aspect ratio */
    public void setWrapToContent(boolean wrapToContent) {
        if (wrapToContent == this.wrapToContent) {
            return;
        }

        this.wrapToContent = wrapToContent;
        requestLayout();
    }

    public AspectRatioFrameLayout(Context context) {
        this(context, null);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout);
            wrapToContent = a.getBoolean(R.styleable.AspectRatioFrameLayout_wrapToContent, false);
            aspectRatio = a.getFloat(R.styleable.AspectRatioFrameLayout_aspectRatio, DEFAULT_ASPECT_RATIO);
            a.recycle();
        } else {
            wrapToContent = false;
            aspectRatio = DEFAULT_ASPECT_RATIO;
        }
    }

    private void setMeasuredDimensionWithAspectOfLesser(int measuredWidth, int measuredHeight) {
        float heightBasedOnMW = measuredWidth * aspectRatio;
        if (heightBasedOnMW > measuredHeight) {
            setMeasuredDimension((int) (measuredHeight / aspectRatio), measuredHeight);
        } else {
            setMeasuredDimension(measuredWidth, (int) heightBasedOnMW);
        }
    }

    private void setMeasuredDimensionWithAspectOfHigher(int measuredWidth, int measuredHeight) {
        float heightBasedOnMW = measuredWidth * aspectRatio;
        if (heightBasedOnMW < measuredHeight) {
            setMeasuredDimension((int) (measuredHeight / aspectRatio), measuredHeight);
        } else {
            setMeasuredDimension(measuredWidth, (int) heightBasedOnMW);
        }
    }

    private Point measureWrapChildren(int widthMeasureSpec, int heightMeasureSpec) {
        Point result = new Point();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, heightMeasureSpec);
            if (result.x < child.getMeasuredWidth()) {
                result.x = child.getMeasuredWidth();
            }
            if (result.y < child.getMeasuredHeight()) {
                result.y = child.getMeasuredHeight();
            }
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (wrapToContent) {
            Point bounds = measureWrapChildren(widthMeasureSpec, heightMeasureSpec);
            width = widthMode == MeasureSpec.UNSPECIFIED ? bounds.x : Math.min(bounds.x, width);
            height = heightMode == MeasureSpec.UNSPECIFIED ? bounds.y : Math.min(bounds.y, height);
        }

        if (widthMode == MeasureSpec.UNSPECIFIED) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                if (wrapToContent) {
                    setMeasuredDimensionWithAspectOfHigher(width, height);
                } else {
                    DisplayMetrics metrics = getResources().getDisplayMetrics();
                    setMeasuredDimensionWithAspectOfLesser(metrics.widthPixels, metrics.heightPixels);
                }
            } else {
                if (wrapToContent) {
                    if (width < (int) (height / aspectRatio)) {
                        setMeasuredDimension((int) (height / aspectRatio), height);
                    } else {
                        setMeasuredDimension(width, (int) (width * aspectRatio));
                    }
                } else {
                    setMeasuredDimension((int) (height / aspectRatio), height);
                }
            }
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            if (wrapToContent) {
                if (width < (int) (height / aspectRatio)) {
                    setMeasuredDimension((int) (height / aspectRatio), height);
                } else {
                    setMeasuredDimension(width, (int) (width * aspectRatio));
                }
            } else {
                setMeasuredDimension(width, (int) (width * aspectRatio));
            }
        } else {
            if (wrapToContent) {
                setMeasuredDimensionWithAspectOfHigher(width, height);
            } else {
                setMeasuredDimensionWithAspectOfLesser(width, height);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            int widthMeasureSpec, heightMeasureSpec;
            int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            switch (lp.width) {
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                    break;
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
                    break;
                default:
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                    break;
            }

            switch (lp.height) {
                case ViewGroup.LayoutParams.MATCH_PARENT:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                    break;
                case ViewGroup.LayoutParams.WRAP_CONTENT:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
                    break;
                default:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                    break;
            }

            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        super.onLayout(changed, left, top, right, bottom);
    }
}