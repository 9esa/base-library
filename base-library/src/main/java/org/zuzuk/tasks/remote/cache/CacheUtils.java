package org.zuzuk.tasks.remote.cache;

import android.os.Looper;

import com.octo.android.robospice.SpiceManager;

import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.ui.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 16/11/2014.
 * Some helpers to work with cache
 */
public class CacheUtils {

    /* Returns if cached data is expired */
    public static boolean isCachedDataExpired(SpiceManager spiceManager, Iterable<CacheEntry> cacheEntries) {
        if(UIUtils.isCurrentThreadMain())
            throw new IllegalStateException("Cache checking could be called only on non-UI thread");

        for (CacheEntry cacheEntry : cacheEntries) {
            if (cacheEntry.isExpired(spiceManager)) {
                return true;
            }
        }

        return false;
    }
}
