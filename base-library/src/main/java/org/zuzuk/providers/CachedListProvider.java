package org.zuzuk.providers;

import com.octo.android.robospice.SpiceManager;

import org.zuzuk.providers.base.ItemsProvider;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.cache.CacheEntry;
import org.zuzuk.tasks.remote.cache.CacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 16/11/2014.
 * Provider that based on simple list with cached data
 */
public class CachedListProvider<TItem> extends ItemsProvider<TItem> {
    private List<TItem> items;
    private List<CacheEntry> cacheInfo;

    @Override
    public int getTotalCount() {
        return isInitialized() ? items.size() : 0;
    }

    /* Returns if provider is initialized */
    public boolean isInitialized() {
        return items != null
                && cacheInfo != null;
    }

    /* Returns if cached data is expired */
    public boolean isDataExpired(SpiceManager spiceManager) {
        return !isInitialized()
                || CacheUtils.isCachedDataExpired(spiceManager, cacheInfo);
    }

    /* Returns if provider stores valid data */
    public boolean isValid(SpiceManager spiceManager) {
        return isInitialized() && !isDataExpired(spiceManager);
    }

    @Override
    public TItem getItem(int position) {
        return items.get(position);
    }

    /* Sets items as source and initializes provider if it wasn't */
    public void setItems(List<TItem> items, RemoteRequest... cachedRequests) {
        if (cachedRequests == null || cachedRequests.length == 0)
            throw new RuntimeException("Cached requests list is empty for cached object. Fix it or use non-cached data provider");

        this.items = items;
        long initializationTime = System.currentTimeMillis();
        cacheInfo = new ArrayList<>(cachedRequests.length);
        for (RemoteRequest request : cachedRequests) {
            cacheInfo.add(new CacheEntry(initializationTime, request));
        }
        onDataSetChanged();
    }

    @Override
    protected void resetInternal() {
        items = null;
        cacheInfo = null;
    }
}