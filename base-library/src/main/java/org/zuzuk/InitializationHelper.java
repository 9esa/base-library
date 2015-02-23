package org.zuzuk;

import android.content.Context;
import android.util.DisplayMetrics;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Created by Gavriil Sitnikov on 11/10/2014.
 * Helper that holds default instances creation logic
 */
public class InitializationHelper {

    /* Standard DisplayImageOptions */
    public static DisplayImageOptions.Builder createDefaultDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .resetViewBeforeLoading(true)
                .displayer(new FadeInBitmapDisplayer(250, true, true, false))
                .delayBeforeLoading(100);
    }

    /* Standard ImageLoaderConfiguration */
    public static ImageLoaderConfiguration.Builder createDefaultImageLoaderConfiguration(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int maxImageSizeInMemory = Math.max(metrics.widthPixels, metrics.widthPixels) / 3;

        return new ImageLoaderConfiguration.Builder(context)
                .diskCache(new LimitedAgeDiscCache(StorageUtils.getIndividualCacheDirectory(context),
                        StorageUtils.getIndividualCacheDirectory(context),
                        new Md5FileNameGenerator(), 24 * 60 * 60))
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache((int) (Runtime.getRuntime().maxMemory() / 8)))
                .memoryCacheExtraOptions(maxImageSizeInMemory, maxImageSizeInMemory)
                .defaultDisplayImageOptions(createDefaultDisplayImageOptions().build());
    }

    /* Standard ImageLoader initialization */
    public static void initializeDefaultImageLoader(Context context) {
        ImageLoader.getInstance().init(createDefaultImageLoaderConfiguration(context).build());
    }

}
