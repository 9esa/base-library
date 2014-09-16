package org.zuzuk.tasks.base;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Base class for long (prefer async) tasks like loading local data or remote request
 */
public abstract class Task<T> extends SpiceRequest<T> {

    /* Listener that's doing nothing on task result */
    public static <T> RequestListener<T> createMockListener() {
        return new RequestListener<T>() {
            public void onRequestFailure(SpiceException spiceException) {
            }

            public void onRequestSuccess(T response) {
            }
        };
    }

    public Task(Class<T> clazz) {
        super(clazz);
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        return execute();
    }

    /* Executes task */
    public abstract T execute() throws Exception;
}
