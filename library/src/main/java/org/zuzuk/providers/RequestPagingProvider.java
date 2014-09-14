package org.zuzuk.providers;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.providers.base.PagingProvider;
import org.zuzuk.providers.base.PagingTaskCreator;
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
    private final RequestExecutor requestExecutor;
    private PagingTaskCreator<TItem, TResponse> requestCreator;

    /* Returns object that executing paging requests */
    protected RequestExecutor getRequestExecutor() {
        return requestExecutor;
    }

    /**
     * Sets object that executing paging requests.
     * It is not good to execute requests directly inside pure logic classes because requests
     * usually depends on UI state and application parts lifecycle (activities, fragments).
     * If you need totally async loading in background then you should create Service object for
     * that purposes and make bridge between Service and UI
     */
    protected void setRequestCreator(PagingTaskCreator<TItem, TResponse> requestCreator) {
        this.requestCreator = requestCreator;
    }

    public RequestPagingProvider(RequestExecutor requestExecutor, PagingTaskCreator<TItem, TResponse> requestCreator) {
        this.requestExecutor = requestExecutor;
        this.requestCreator = requestCreator;
    }

    @Override
    protected void requestPage(final int index) {
        getRequestingPages().add(index);
        requestExecutor.executeRequest((RemoteRequest<TResponse>) requestCreator.createTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE),
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

    @Override
    protected void writeObject(ObjectOutputStream out) throws IOException {
        super.writeObject(out);
        out.writeObject(requestCreator);
    }

    @Override
    protected void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readObject(in);
        requestCreator = (PagingTaskCreator<TItem, TResponse>) in.readObject();
    }
}
