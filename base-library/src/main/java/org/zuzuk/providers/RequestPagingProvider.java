package org.zuzuk.providers;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.providers.base.PagingProvider;
import org.zuzuk.providers.base.PagingTaskCreator;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on remote paging requests
 */
public class RequestPagingProvider<TItem extends Serializable, TResponse> extends PagingProvider<TItem> {
    private TaskExecutor requestExecutor;
    private PagingTaskCreator<TItem, TResponse> requestCreator;

    /* Returns object that executing paging requests */
    public TaskExecutor getRequestExecutor() {
        return requestExecutor;
    }

    /**
     * Sets object that executing paging requests.
     * It is not good to execute requests directly inside pure logic classes because requests
     * usually depends on UI state and application parts lifecycle (activities, fragments).
     * If you need totally async loading in background then you should create Service object for
     * that purposes and make bridge between Service and UI
     */
    public void setRequestExecutor(TaskExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    public RequestPagingProvider(TaskExecutor requestExecutor, PagingTaskCreator<TItem, TResponse> requestCreator) {
        this.requestExecutor = requestExecutor;
        this.requestCreator = requestCreator;
    }

    @Override
    protected void requestPage(final int index) {
        getRequestingPages().add(index);
        requestExecutor.executeTaskBackground(requestCreator.createTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE),
                new RequestListener<TResponse>() {

                    @Override
                    public void onRequestSuccess(TResponse response) {
                        onPageLoaded(index, requestCreator.parseResponse(response));
                        getRequestingPages().remove(index);
                        onDataSetChanged();
                        if (!isInitialized()) {
                            onInitialized();
                        }
                    }

                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                        getRequestingPages().remove(index);
                        if (!isInitialized()) {
                            onInitializationFailed(spiceException);
                        }
                    }
                }
        );
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(requestCreator);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        requestCreator = (PagingTaskCreator<TItem, TResponse>) in.readObject();
    }
}
