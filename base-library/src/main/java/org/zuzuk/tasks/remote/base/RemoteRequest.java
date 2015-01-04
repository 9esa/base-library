package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.remote.cache.AllowOnlyOfflineCacheRequest;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base remote request that supports caching if possible
 */
public abstract class RemoteRequest<T> extends Task<T> {

    /* Returns associated cache request */
    public AllowOnlyOfflineCacheRequest<T> wrapAsCacheRequest(SpiceManager spiceManager) {
        AllowOnlyOfflineCacheRequest<T> cachedSpiceRequest
                = new AllowOnlyOfflineCacheRequest<>(spiceManager, this, getCacheKey(), getCacheExpiryDuration());
        cachedSpiceRequest.setOffline(isOffline());
        cachedSpiceRequest.setAggregatable(true);
        return cachedSpiceRequest;
    }

    /* Returns is request offline (may be cached) or not */
    public boolean isOffline() {
        return false;
    }

    /* Returns cache key */
    public abstract Object getCacheKey();

    /* Returns cache expiration time */
    public long getCacheExpiryDuration() {
        return DurationInMillis.ALWAYS_EXPIRED;
    }

    protected RemoteRequest(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        return execute();
    }

    /* Executes request */
    public abstract T execute() throws Exception;
}
