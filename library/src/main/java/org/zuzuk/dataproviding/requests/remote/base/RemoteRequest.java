package org.zuzuk.dataproviding.requests.remote.base;

import com.octo.android.robospice.request.SpiceRequest;

import org.zuzuk.dataproviding.requests.base.Task;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base remote request that supports caching if possible
 */
public abstract class RemoteRequest<T> extends SpiceRequest<T> implements Task<T> {

    /* Returns is request offline (may be cached) or not */
    public boolean isOffline() {
        return false;
    }

    /* Returns cache key */
    public abstract Object getCacheKey();

    /**
     * Returns cache expiration time.
     * Return DurationInMillis.ALWAYS_EXPIRED if it always expired
     */
    public abstract long getCacheExpiryDuration();

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
