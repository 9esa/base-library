package org.zuzuk.providers;

import com.octo.android.robospice.SpiceManager;

import org.zuzuk.providers.base.PagingProvider;
import org.zuzuk.providers.base.PagingTaskCreator;
import org.zuzuk.tasks.aggregationtask.AggregationPagingTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskExecutor;
import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;
import org.zuzuk.tasks.aggregationtask.WrappedAggregationTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on remote paging requests
 */
public class RequestPagingProvider<TItem extends Serializable> extends PagingProvider<TItem> {
    private AggregationTaskExecutor executor;
    private PagingTaskCreator<TItem> requestCreator;
    private HashMap<Integer, List<TItem>> items = new HashMap<>();
    private Long initializationTime;

    /* Returns object that executing paging requests */
    public AggregationTaskExecutor getExecutor() {
        return executor;
    }

    /**
     * Sets object that executing paging requests.
     * It is not good to execute requests directly inside pure logic classes because requests
     * usually depends on UI state and application parts lifecycle (activities, fragments).
     * If you need totally async loading in background then you should create Service object for
     * that purposes and make bridge between Service and UI
     */
    public void setExecutor(AggregationTaskExecutor executor) {
        this.executor = executor;
    }

    /* Returns if cached data is expired */
    public boolean isDataExpired(SpiceManager spiceManager) {
        if (!isInitialized()) {
            return true;
        }

        for (Integer index : items.keySet()) {
            if (createTask(index).isLoadingNeeded(AggregationTaskStageState.createPreLoadingStageState())) {
                return true;
            }
        }
        return false;
    }

    /* Returns if provider stores valid data */
    public boolean isValid(SpiceManager spiceManager) {
        return isInitialized() && !isDataExpired(spiceManager);
    }

    public RequestPagingProvider(AggregationTaskExecutor executor, PagingTaskCreator<TItem> requestCreator) {
        this.executor = executor;
        this.requestCreator = requestCreator;
    }

    private AggregationPagingTask<TItem> createTask(int index) {
        return requestCreator.createPagingTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE);
    }

    @Override
    protected void requestPage(final int index, AggregationTaskStageState stageState) {
        final AggregationPagingTask<TItem> aggregationTask = createTask(index);

        if (stageState == null) {
            executor.executeAggregationTask(new WrappedAggregationTask(aggregationTask) {
                @Override
                public void load(AggregationTaskStageState currentTaskStageState) {
                    getRequestingPages().add(index);
                    super.load(currentTaskStageState);
                }

                @Override
                public void onLoaded(AggregationTaskStageState currentTaskStageState) {
                    super.onLoaded(currentTaskStageState);
                    List<TItem> items = aggregationTask.getPageItems();
                    if (items != null) {
                        RequestPagingProvider.this.items.put(index, items);
                        if (initializationTime == null) {
                            initializationTime = System.currentTimeMillis();
                        }
                        onPageLoaded(index, items);
                    }
                }

                @Override
                public void onFailed(AggregationTaskStageState currentTaskStageState) {
                    super.onFailed(currentTaskStageState);
                    onPageLoadingFailed(index, currentTaskStageState.getExceptions());
                }
            });
        } else {
            getRequestingPages().add(index);
            aggregationTask.load(stageState);
        }
    }

    @Override
    protected void resetInternal() {
        super.resetInternal();
        items.clear();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(requestCreator);
        out.writeBoolean(isInitialized());
        if (isInitialized()) {
            out.writeObject(items);
            out.writeLong(initializationTime);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        requestCreator = (PagingTaskCreator<TItem>) in.readObject();
        if (in.readBoolean()) {
            items = (HashMap<Integer, List<TItem>>) in.readObject();
            initializationTime = in.readLong();
        } else {
            items = new HashMap<>();
        }
    }
}
