package org.zuzuk.tasks.remote.cache;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.CachedSpiceRequest;

import org.zuzuk.tasks.remote.base.RemoteRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Gavriil Sitnikov on 16/11/2014.
 * Cache information about request
 */
public class CacheEntry implements Serializable {
    private long initializationTime;
    private Class<?> responseType;
    private Object cacheKey;
    private long cacheDuration;

    public CacheEntry(long initializationTime, RemoteRequest remoteRequest) {
        this.initializationTime = initializationTime;
        this.responseType = remoteRequest.getResultType();
        this.cacheKey = remoteRequest.getCacheKey();
        this.cacheDuration = remoteRequest.getCacheExpiryDuration();
    }

    /* Returns if cache is expired */
    public boolean isExpired(SpiceManager spiceManager) {
        try {
            Date cachedDate = spiceManager.getDateOfDataInCache(responseType, cacheKey).get();
            if (cachedDate.getTime() > initializationTime
                    || cachedDate.getTime() > initializationTime + cacheDuration) {
                return true;
            }
        } catch (Exception e) {
            return true;
        }

        return false;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeLong(initializationTime);
        out.writeObject(responseType);
        out.writeObject(cacheKey);
        out.writeLong(cacheDuration);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.initializationTime = in.readLong();
        this.responseType = (Class<?>) in.readObject();
        this.cacheKey = in.readObject();
        this.cacheDuration = in.readLong();
    }
}
