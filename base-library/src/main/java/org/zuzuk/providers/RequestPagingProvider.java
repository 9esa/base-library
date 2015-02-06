package org.zuzuk.providers;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.providers.base.PagingProvider;
import org.zuzuk.providers.base.PagingTaskCreator;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.cache.CacheEntry;
import org.zuzuk.tasks.remote.cache.CacheUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on remote paging requests
 */
public class RequestPagingProvider<TItem extends Serializable, TResponse> extends PagingProvider<TItem> {
    private RequestExecutor requestExecutor;
    private PagingTaskCreator<TItem, TResponse> requestCreator;
    private HashMap<Integer, CacheEntry> cacheInfo = new HashMap<>();
    private Long initializationTime;

    /* Returns object that executing paging requests */
    public RequestExecutor getRequestExecutor() {
        return requestExecutor;
    }

    /**
     * Sets object that executing paging requests.
     * It is not good to execute requests directly inside pure logic classes because requests
     * usually depends on UI state and application parts lifecycle (activities, fragments).
     * If you need totally async loading in background then you should create Service object for
     * that purposes and make bridge between Service and UI
     */
    public void setRequestExecutor(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    /* Returns if cached data is expired */
    public boolean isDataExpired(SpiceManager spiceManager) {
        return !isInitialized()
                || CacheUtils.isCachedDataExpired(spiceManager, cacheInfo.values());
    }

    /* Returns if provider stores valid data */
    public boolean isValid(SpiceManager spiceManager) {
        return isInitialized() && !isDataExpired(spiceManager);
    }

    public RequestPagingProvider(RequestExecutor requestExecutor, PagingTaskCreator<TItem, TResponse> requestCreator) {
        this.requestExecutor = requestExecutor;
        this.requestCreator = requestCreator;
    }

    @Override
    protected void requestPage(final int index) {
        getRequestingPages().add(index);
        final RemoteRequest<TResponse> request = (RemoteRequest<TResponse>) requestCreator.createTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE);
        requestExecutor.executeRealLoadingRequest(request, new RequestListener<TResponse>() {
            @Override
            public void onRequestSuccess(TResponse response) {
                onPageLoaded(index, requestCreator.parseResponse(response));
                cacheInfo.put(index, new CacheEntry(System.currentTimeMillis(), request));
                if (initializationTime == null) {
                    initializationTime = System.currentTimeMillis();
                }
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                onPageLoadingFailed(index, spiceException);
            }
        }, null);
    }

    @Override
    protected void resetInternal() {
        super.resetInternal();
        cacheInfo.clear();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(requestCreator);
        out.writeBoolean(isInitialized());
        if (isInitialized()) {
            out.writeObject(cacheInfo);
            out.writeLong(initializationTime);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        requestCreator = (PagingTaskCreator<TItem, TResponse>) in.readObject();
        if (in.readBoolean()) {
            cacheInfo = (HashMap<Integer, CacheEntry>) in.readObject();
            initializationTime = in.readLong();
        } else {
            cacheInfo = new HashMap<>();
        }
    }
}
