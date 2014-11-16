package org.zuzuk.tasks.remote.base;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;

import org.zuzuk.tasks.base.Task;

import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base remote request that supports caching if possible
 */
public abstract class RemoteRequest<T> extends Task<T> {

    /* Returns associated cache request */
    public CachedSpiceRequest<T> wrapAsCacheRequest() {
        CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<>(this, getCacheKey(), getCacheExpiryDuration());
        cachedSpiceRequest.setOffline(isOffline());
        cachedSpiceRequest.setAggregatable(true);
        cachedSpiceRequest.setAcceptingDirtyCache(isAcceptDirtyCache());
        return cachedSpiceRequest;
    }

    /* Returns if request could use expired cache data */
    public boolean isAcceptDirtyCache() {
        return false;
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
