package org.zuzuk.tasks.remote.cache;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class ORMLiteDatabasePersisterFactory extends ObjectPersisterFactory {

    public ORMLiteDatabasePersisterFactory(Application application) {
        super(application);
    }

    @Override
    public <TObject> ObjectPersister<TObject> createObjectPersister(Class<TObject> clazz) throws CacheCreationException {
        return new ORMLiteDatabaseObjectPersister<>(getApplication(), clazz);
    }

}
