package org.zuzuk.providers;

import com.octo.android.robospice.SpiceManager;

import org.zuzuk.providers.base.DataProvider;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.cache.CacheEntry;
import org.zuzuk.tasks.remote.cache.CacheUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 16/11/2014.
 * Provider that stores a cached object
 */
public class CachedObjectProvider<TObject> extends DataProvider {
    private TObject object;
    private List<CacheEntry> cacheInfo;

    /* Returns stored data object */
    public TObject get() {
        return object;
    }

    /* Sets data object */
    public void set(TObject object, RemoteRequest... cachedRequests) {
        if (cachedRequests == null || cachedRequests.length == 0)
            throw new RuntimeException("Cached requests list is empty for cached object. Fix it or use non-cached data provider");

        this.object = object;
        long initializationTime = System.currentTimeMillis();
        cacheInfo = new ArrayList<>(cachedRequests.length);
        for (RemoteRequest request : cachedRequests) {
            cacheInfo.add(new CacheEntry(initializationTime, request));
        }
        onDataSetChanged();
    }

    /* Returns if cached data is expired */
    public boolean isDataExpired(SpiceManager spiceManager) {
        return CacheUtils.isCachedDataExpired(spiceManager, cacheInfo);
    }

    /* Returns if provider stores valid data */
    public boolean isValid(SpiceManager spiceManager) {
        return !isDataExpired(spiceManager);
    }

    @Override
    protected void resetInternal() {
        object = null;
        cacheInfo = null;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(cacheInfo != null);
        if (cacheInfo != null) {
            out.writeObject(object);
            out.writeObject(cacheInfo);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            object = (TObject) in.readObject();
            cacheInfo = (List<CacheEntry>) in.readObject();
        }
    }
}
