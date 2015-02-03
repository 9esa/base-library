package org.zuzuk.tasks.remote.mycache;

import android.app.Application;

import com.octo.android.robospice.GoogleHttpClientSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class MyCacheService extends GoogleHttpClientSpiceService {
    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new MyObjectPersisterFactory(application));
        return cacheManager;
    }
}
