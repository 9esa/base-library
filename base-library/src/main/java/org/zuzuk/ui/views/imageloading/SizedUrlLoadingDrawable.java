package org.zuzuk.ui.views.imageloading;

/**
 * Created by Gavriil Sitnikov on 07/09/2014.
 * LoadingDrawable that's url is based on sizes of view
 */
public abstract class SizedUrlLoadingDrawable extends LoadingDrawable {

    /* Returns width of requested image */
    protected int getRequestedWidth() {
        return getBounds().width();
    }

    /* Returns height of requested image */
    protected int getRequestedHeight() {
        return getBounds().height();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        boolean doReload = getBounds().width() != right - left
                || getBounds().height() != bottom - top;

        super.setBounds(left, top, right, bottom);

        if (doReload) {
            reload();
        }
    }

    @Override
    protected boolean canLoad() {
        return getBounds().height() > 0 && getBounds().width() > 0;
    }
}
