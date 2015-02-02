package org.zuzuk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.octo.android.robospice.Jackson2GoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;

import org.zuzuk.tasks.local.LocalSpiceService;
import org.zuzuk.utils.Lc;

/**
 * Created by Gavriil Sitnikov on 11/10/2014.
 * Helper that holds default instances creation logic
 */
public class InitializationHelper {

    /* Standard console logger */
    public static Lc.LogProcessor createDefaultLogProcessor() {
        return new Lc.LogProcessor() {
            @Override
            public void processLogMessage(int logLevel, String tag, String message) {
                switch (logLevel) {
                    case Log.DEBUG:
                        Log.d(tag, message);
                        break;
                    case Log.INFO:
                        Log.i(tag, message);
                        break;
                    case Log.WARN:
                        Log.w(tag, message);
                        break;
                    case Log.ERROR:
                        Log.e(tag, message);
                        break;
                    default:
                        throw new IllegalStateException("Unsupported log level: " + logLevel);
                }
            }

            @Override
            public void processLogMessage(int logLevel, String tag, String message, @NonNull Throwable ex) {
                switch (logLevel) {
                    case Log.DEBUG:
                        Log.d(tag, message, ex);
                        break;
                    case Log.INFO:
                        Log.i(tag, message, ex);
                        break;
                    case Log.WARN:
                        Log.w(tag, message, ex);
                        break;
                    case Log.ERROR:
                        Log.e(tag, message, ex);
                        break;
                    default:
                        throw new IllegalStateException("Unsupported log level: " + logLevel);
                }
            }
        };
    }

    /* Standard spice manager for local tasks */
    public static SpiceManager createDefaultLocalSpiceManager() {
        return new SpiceManager(LocalSpiceService.class);
    }

    /* Standard spice manager for local tasks */
    public static SpiceManager createDefaultJsonSpiceManager() {
        return new SpiceManager(Jackson2GoogleHttpClientSpiceService.class);
    }

    /* Standard DisplayImageOptions */
    public static DisplayImageOptions.Builder createDefaultDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .displayer(new FadeInBitmapDisplayer(300, true, true, false))
                .delayBeforeLoading(100);
    }

    /* Standard ImageLoaderConfiguration */
    public static ImageLoaderConfiguration.Builder createDefaultImageLoaderConfiguration(Context context) {
        return new ImageLoaderConfiguration.Builder(context)
                .diskCache(new LimitedAgeDiscCache(StorageUtils.getIndividualCacheDirectory(context),
                        StorageUtils.getIndividualCacheDirectory(context),
                        new Md5FileNameGenerator(), 24 * 60 * 60))
                .defaultDisplayImageOptions(createDefaultDisplayImageOptions().build());
    }

    /* Standard ImageLoader initialization */
    public static void initializeDefaultImageLoader(Context context) {
        ImageLoader.getInstance().init(createDefaultImageLoaderConfiguration(context).build());
    }
}
