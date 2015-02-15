package org.zuzuk.tasks.remote.cache;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class ORMLiteDatabaseCacheService extends SpiceService {

    @Override
    public int getCoreThreadCount() {
        return 5;
    }

    @Override
    public int getMaximumThreadCount() {
        return 10;
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new ORMLiteDatabasePersisterFactory(application));
        return cacheManager;
    }

}
