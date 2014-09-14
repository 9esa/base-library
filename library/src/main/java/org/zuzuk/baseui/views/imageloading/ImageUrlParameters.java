package org.zuzuk.baseui.views.imageloading;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Parameters to load image
 */
public interface ImageUrlParameters {

    /* ImageLoader that is using for loading */
    ImageLoader getImageLoader();

    /* ImageOptions that is using for loading */
    DisplayImageOptions getDisplayImageOptions();

    /* Loading URL */
    String getUrl();
}