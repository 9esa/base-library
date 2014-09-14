package org.zuzuk.baseui.views.imageloading;

import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 07/09/2014.
 * Simple LoadingDrawable based on direct url
 */
public abstract class SimpleUrlLoadingDrawable extends LoadingDrawable {
    private String url;

    /* Sets base url */
    public void setUrl(String url) {
        if (!Utils.objectsEquals(this.url, url)) {
            this.url = url;
            reload();
        }
    }

    @Override
    public String getUrl() {
        return url;
    }
}
