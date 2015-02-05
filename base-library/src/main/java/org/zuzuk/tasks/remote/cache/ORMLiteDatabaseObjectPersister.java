package org.zuzuk.tasks.remote.cache;

import android.app.Application;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

import org.zuzuk.database.DBUtils;
import org.zuzuk.utils.serialization.FSTSerializer;
import org.zuzuk.utils.serialization.Serializer;

import java.util.ArrayList;
import java.util.List;

public class ORMLiteDatabaseObjectPersister<TObject> extends ObjectPersister<TObject> {

    private Serializer serializer = FSTSerializer.Instance;

    public ORMLiteDatabaseObjectPersister(Application application, Class<TObject> clazz) {
        super(application, clazz);
    }

    protected void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public TObject loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException {
        ORMLiteDatabaseCacheDbEntry cacheDbEntry = getMyCacheDbEntry(cacheKey);
        if (isCachedAndNotExpired(cacheDbEntry, maxTimeInCache)) {
            return serializer.deserialize(cacheDbEntry.getData());
        } else {
            return null;
        }
    }

    @Override
    public List<TObject> loadAllDataFromCache() throws CacheLoadingException {
        List<Object> allCacheKeys = getAllCacheKeys();
        List<TObject> result = new ArrayList<>(allCacheKeys.size());
        for (Object key : allCacheKeys) {
            result.add(loadDataFromCache(key, DurationInMillis.ALWAYS_RETURNED));
        }
        return result;
    }

    @Override
    public List<Object> getAllCacheKeys() {
        List<Object> result = new ArrayList<>();
        List<ORMLiteDatabaseCacheDbEntry> cacheDbEntries = getCacheDbTable().queryForAll();
        for (ORMLiteDatabaseCacheDbEntry cacheDbEntry : cacheDbEntries) {
            result.add(cacheDbEntry.getKey());
        }
        return result;
    }

    @Override
    public TObject saveDataToCacheAndReturnData(TObject object, Object cacheKey) throws CacheSavingException {
        byte[] serializedData = serializer.serialize(object);
        ORMLiteDatabaseCacheDbEntry cacheDbEntry = new ORMLiteDatabaseCacheDbEntry(cacheKey.toString(), serializedData);
        getCacheDbTable().createOrUpdate(cacheDbEntry);
        return object;
    }

    @Override
    public boolean removeDataFromCache(Object cacheKey) {
        int deletedCount = getCacheDbTable().deleteById(cacheKey);
        return deletedCount == 1;
    }

    @Override
    public void removeAllDataFromCache() {
        DBUtils.deleteAll(getCacheDbTable());
    }

    private RuntimeExceptionDao<ORMLiteDatabaseCacheDbEntry, Object> getCacheDbTable() {
        return ORMLiteDatabaseCacheDbHelper.getInstance(getApplication()).getDbTable(ORMLiteDatabaseCacheDbEntry.class);
    }

    @Override
    public long getCreationDateInCache(Object cacheKey) throws CacheLoadingException {
        ORMLiteDatabaseCacheDbEntry cacheDbEntry = getMyCacheDbEntry(cacheKey);
        if (cacheDbEntry != null) {
            return cacheDbEntry.getLastModified();
        } else {
            throw new CacheLoadingException("Data could not be found in cache for cacheKey=" + cacheKey);
        }
    }

    @Override
    public boolean isDataInCache(Object cacheKey, long maxTimeInCacheBeforeExpiry) {
        ORMLiteDatabaseCacheDbEntry cacheDbEntry = getMyCacheDbEntry(cacheKey);
        return isCachedAndNotExpired(cacheDbEntry, maxTimeInCacheBeforeExpiry);
    }

    private ORMLiteDatabaseCacheDbEntry getMyCacheDbEntry(Object cacheKey) {
        return getCacheDbTable().queryForId(cacheKey);
    }

    private boolean isCachedAndNotExpired(ORMLiteDatabaseCacheDbEntry cacheDbEntry, long maxTimeInCacheBeforeExpiry) {
        if (cacheDbEntry != null) {
            long timeInCache = System.currentTimeMillis() - cacheDbEntry.getLastModified();
            if (maxTimeInCacheBeforeExpiry == DurationInMillis.ALWAYS_RETURNED || timeInCache <= maxTimeInCacheBeforeExpiry) {
                return true;
            }
        }
        return false;
    }

}
