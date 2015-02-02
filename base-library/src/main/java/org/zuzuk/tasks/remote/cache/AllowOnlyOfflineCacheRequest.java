package org.zuzuk.tasks.remote.cache;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import com.octo.android.robospice.retry.RetryPolicy;

/**
 * Created by Gavriil Sitnikov on 02/12/2014.
 * Cache request that is doing only local cache loading if isOffline() returns true
 */
public class AllowOnlyOfflineCacheRequest<T> extends CachedSpiceRequest<T> {
    private final SpiceManager spiceManager;

    public AllowOnlyOfflineCacheRequest(SpiceManager spiceManager, SpiceRequest<T> spiceRequest, Object requestCacheKey, long cacheDuration) {
        super(spiceRequest, requestCacheKey, cacheDuration);
        this.spiceManager = spiceManager;
    }

    @Override
    public void setAcceptingDirtyCache(boolean isAcceptingDirtyCache) {
        throw new RuntimeException("Dirty cache is deprecated in this request. Use isOffline() parameter to get dirty cache instead");
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        if (isOffline()) {
            return new DefaultRetryPolicy(0, 0, 0);
        }
        return super.getRetryPolicy();
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        if (isOffline()) {
            T result = spiceManager.getDataFromCache(getResultType(), getRequestCacheKey()).get();
            if (result != null) {
                return result;
            } else {
                throw new Exception("Cached data not found for: '" + getRequestCacheKey() + "' during offline cache request");
            }
        }
        return super.loadDataFromNetwork();
    }
}