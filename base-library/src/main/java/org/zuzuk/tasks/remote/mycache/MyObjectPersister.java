package org.zuzuk.tasks.remote.mycache;

import android.app.Application;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

import org.nustaq.serialization.FSTConfiguration;
import org.zuzuk.database.DBUtils;

import java.util.ArrayList;
import java.util.List;

public class MyObjectPersister<DATA> extends ObjectPersister<DATA> {

    public MyObjectPersister(Application application, Class<DATA> clazz) {
        super(application, clazz);
    }

    @Override
    public DATA loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException {
        MyCacheDbEntry myCacheDbEntry = getMyCacheDbEntry(cacheKey);
        if (isCachedAndNotExpired(myCacheDbEntry, maxTimeInCache)) {
            DATA deserialize = deserialize(myCacheDbEntry.getData());
            return deserialize;
        } else {
            return null;
        }
    }

    @Override
    public List<DATA> loadAllDataFromCache() throws CacheLoadingException {
        List<Object> allCacheKeys = getAllCacheKeys();
        List<DATA> result = new ArrayList<>(allCacheKeys.size());
        for (Object key : allCacheKeys) {
            result.add(loadDataFromCache(key, DurationInMillis.ALWAYS_RETURNED));
        }
        return result;
    }

    @Override
    public List<Object> getAllCacheKeys() {
        List<Object> result = new ArrayList<>();
        List<MyCacheDbEntry> myCacheDbEntries = getDbTable().queryForAll();
        for (MyCacheDbEntry entry : myCacheDbEntries) {
            result.add(entry.getKey());
        }
        return result;
    }

    @Override
    public DATA saveDataToCacheAndReturnData(DATA data, Object cacheKey) throws CacheSavingException {
        byte[] serializedData = serialize(data);
        MyCacheDbEntry entry = new MyCacheDbEntry(cacheKey.toString(), serializedData);
        getDbTable().createOrUpdate(entry);
        return data;
    }

    @Override
    public boolean removeDataFromCache(Object cacheKey) {
        getDbTable().deleteById(cacheKey);
        //TODO
        return true;
    }

    @Override
    public void removeAllDataFromCache() {
        DBUtils.deleteAll(getDbTable());
    }

    private RuntimeExceptionDao<MyCacheDbEntry, Object> getDbTable() {
        return MyCacheDbHelper.getInstance(getApplication()).getDbTable(MyCacheDbEntry.class);
    }

    @Override
    public long getCreationDateInCache(Object cacheKey) throws CacheLoadingException {
        MyCacheDbEntry myCacheDbEntry = getMyCacheDbEntry(cacheKey);
        if (myCacheDbEntry != null) {
            return myCacheDbEntry.getLastModified();
        } else {
            throw new CacheLoadingException(
                    "Data could not be found in cache for cacheKey=" + cacheKey);
        }
    }

    @Override
    public boolean isDataInCache(Object cacheKey, long maxTimeInCacheBeforeExpiry) {
        MyCacheDbEntry myCacheDbEntry = getMyCacheDbEntry(cacheKey);
        return isCachedAndNotExpired(myCacheDbEntry, maxTimeInCacheBeforeExpiry);
    }

    private MyCacheDbEntry getMyCacheDbEntry(Object cacheKey) {
        MyCacheDbEntry myCacheDbEntry = getDbTable().queryForId(cacheKey);
        return myCacheDbEntry;
    }

    private boolean isCachedAndNotExpired(MyCacheDbEntry myCacheDbEntry, long maxTimeInCacheBeforeExpiry) {
        if (myCacheDbEntry != null) {
            long timeInCache = System.currentTimeMillis() - myCacheDbEntry.getLastModified();
            if (maxTimeInCacheBeforeExpiry == DurationInMillis.ALWAYS_RETURNED || timeInCache <= maxTimeInCacheBeforeExpiry) {
                return true;
            }
        }
        return false;
    }

    static ThreadLocal<FSTConfiguration> conf = new ThreadLocal<FSTConfiguration>() {
        public FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }};

    private DATA deserialize(byte[] data) {
        return (DATA) conf.get().asObject(data);
    }

    private byte[] serialize(DATA data) {
        return conf.get().asByteArray(data);
    }

//    private DATA deserialize(byte[] data) {
//        try {
//            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data));
//            return (DATA) objectInputStream.readObject();
//        }
//        catch (Exception e) {
//            return null;
//        }
//    }
//
//    private byte[] serialize(DATA data) {
//        try {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
//            objectOutputStream.writeObject(data);
//            objectOutputStream.close();
//            byte[] buffer = stream.toByteArray(); // Serialization done, get bytes
//
//            return buffer;
//
//        }
//        catch (Exception e) {
//            return null;
//        }
//
//    }

}
