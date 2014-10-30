package org.zuzuk.providers.base;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Listener that listen to page loading
 */
public interface OnPageLoadedListener<T> {

    /* Raises when page loaded */
    void onPageLoaded(int pageIndex, List<T> items);
}
