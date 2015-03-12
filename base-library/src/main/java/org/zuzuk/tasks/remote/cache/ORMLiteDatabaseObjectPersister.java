package org.zuzuk.tasks.remote.cache;

import android.app.Application;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

import org.apache.commons.io.FileUtils;
import org.zuzuk.utils.serialization.KryoSerializer;
import org.zuzuk.utils.serialization.Serializer;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ORMLiteDatabaseObjectPersister<TObject> extends ObjectPersister<TObject> {
    private static final String DEFAULT_ROOT_CACHE_DIR = "robospice-cache";
    private static final int MAX_BYTE_ARRAY_SIZE_IN_DB = 512 * 1024; // 512Kb

    private Serializer serializer = KryoSerializer.Instance;
    private File cacheFolder;

    protected void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    protected File getCacheFolder() throws CacheCreationException {
        if (cacheFolder == null) {
            cacheFolder = new File(getApplication().getCacheDir(), DEFAULT_ROOT_CACHE_DIR);
        }
        synchronized (cacheFolder.getAbsolutePath().intern()) {
            if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
                throw new CacheCreationException("The cache folder " + cacheFolder.getAbsolutePath() + " could not be created.");
            }
        }
        return cacheFolder;
    }

    protected File getCacheFile(String cacheKey) throws CacheCreationException {
        return new File(getCacheFolder(), cacheKey);
    }

    public ORMLiteDatabaseObjectPersister(Application application, Class<TObject> clazz) {
        super(application, clazz);
    }

    @Override
    public TObject loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException {
        ORMLiteDatabaseCacheDbEntry cacheDbEntry = getMyCacheDbEntry(cacheKey);
        if (isCachedAndNotExpired(cacheDbEntry, maxTimeInCache)) {
            byte[] data = cacheDbEntry.getData();
            if (data == null) {
                try {
                    File cachedFile = getCacheFile(cacheKey.toString());
                    data = FileUtils.readFileToByteArray(cachedFile);
                } catch (Exception e) {
                    throw new CacheLoadingException(e);
                }
            }
            try {
                return serializer.deserialize(data);
            } catch (Exception e) {
                throw new CacheLoadingException(e);
            }
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
        byte[] serializedData;
        try {
            serializedData = serializer.serialize(object);
        } catch (Exception e) {
            throw new CacheSavingException(e);
        }
        ORMLiteDatabaseCacheDbEntry cacheDbEntry;
        if (serializedData.length < MAX_BYTE_ARRAY_SIZE_IN_DB) {
            cacheDbEntry = new ORMLiteDatabaseCacheDbEntry(cacheKey.toString(), getHandledClass(), serializedData);
        } else {
            cacheDbEntry = new ORMLiteDatabaseCacheDbEntry(cacheKey.toString(), getHandledClass(), null);
            try {
                FileUtils.writeByteArrayToFile(getCacheFile(cacheKey.toString()), serializedData);
            } catch (Exception e) {
                throw new CacheSavingException(e);
            }
        }
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
        try {
            DeleteBuilder<ORMLiteDatabaseCacheDbEntry, Object> deleteBuilder = getCacheDbTable().deleteBuilder();
            deleteBuilder.where().eq(ORMLiteDatabaseCacheDbEntry.CLAZZ_COLUMN, getHandledClass().getName());
            deleteBuilder.delete();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
