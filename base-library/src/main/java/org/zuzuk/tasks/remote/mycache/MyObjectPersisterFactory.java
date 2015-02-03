package org.zuzuk.tasks.remote.mycache;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class MyObjectPersisterFactory extends ObjectPersisterFactory {

    public MyObjectPersisterFactory(Application application) {
        super(application);
    }

    @Override
    public <DATA> ObjectPersister<DATA> createObjectPersister(Class<DATA> clazz) throws CacheCreationException {
        return new MyObjectPersister<>(getApplication(), clazz);
    }

}
