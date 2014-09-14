package org.zuzuk.dataproviding.providers;

import org.zuzuk.dataproviding.providers.base.PagingProvider;
import org.zuzuk.dataproviding.providers.base.PagingTaskCreator;
import org.zuzuk.dataproviding.requests.base.ResultListener;
import org.zuzuk.dataproviding.requests.remote.base.RemoteRequest;
import org.zuzuk.dataproviding.requests.remote.base.RequestExecutor;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on remote paging requests
 */
public class RequestPagingProvider<TItem, TResponse> extends PagingProvider<TItem> {
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
        requestExecutor.executeRequest((RemoteRequest) requestCreator.createTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE),
                new ResultListener<TResponse>() {
                    @Override
                    public void onSuccess(TResponse response) {
                        onPageLoaded(index, requestCreator.parseResponse(response));
                        getRequestingPages().remove(index);
                        onDataSetChanged();
                        if (!isInitialized()) {
                            onInitialized();
                        }
                    }

                    //TODO: request again?
                    @Override
                    public void onFailure(Exception ex) {
                        getRequestingPages().remove(index);
                        if (!isInitialized()) {
                            onInitializationFailed(ex);
                        }
                    }
                }
        );
    }
}
