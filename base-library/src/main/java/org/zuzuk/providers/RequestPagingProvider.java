package org.zuzuk.providers;

import com.octo.android.robospice.SpiceManager;

import org.zuzuk.providers.base.PagingProvider;
import org.zuzuk.providers.base.PagingTaskCreator;
import org.zuzuk.tasks.aggregationtask.AggregationPagingTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskExecutor;
import org.zuzuk.tasks.aggregationtask.AggregationTaskStageListener;
import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;
import org.zuzuk.tasks.aggregationtask.WrappedAggregationTask;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on remote paging requests
 */
public class RequestPagingProvider<TItem> extends PagingProvider<TItem> {

    private AggregationTaskExecutor aggregationTaskExecutor;
    private PagingTaskCreator<TItem> requestCreator;
    private HashMap<Integer, List<TItem>> items = new HashMap<>();
    private Long initializationTime;

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

    public RequestPagingProvider(AggregationTaskExecutor aggregationTaskExecutor, PagingTaskCreator<TItem> requestCreator) {
        this.aggregationTaskExecutor = aggregationTaskExecutor;
        this.requestCreator = requestCreator;
    }

    private AggregationPagingTask createTask(int index) {
        return requestCreator.createPagingTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE);
    }

    @SuppressWarnings("unchecked")
    private void processOnPageLoaded(AggregationPagingTask aggregationTask, int index) {
        List<TItem> items = aggregationTask.getPageItems();
        if (items != null) {
            RequestPagingProvider.this.items.put(index, items);
            if (initializationTime == null) {
                initializationTime = System.currentTimeMillis();
            }
            onPageLoaded(index, items);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void requestPage(final int index) {
        final AggregationPagingTask aggregationTask = createTask(index);
        aggregationTaskExecutor.executeAggregationTask(new WrappedAggregationTask(aggregationTask) {
            @Override
            public void load(RequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
                getRequestingPages().add(index);
                super.load(executor, currentTaskStageState);
                currentTaskStageState.addListener(new AggregationTaskStageListener() {
                    @Override
                    public void onLoadingStarted(AggregationTaskStageState currentTaskStageState) {
                    }

                    @Override
                    public void onLoaded(AggregationTaskStageState currentTaskStageState) {
                        processOnPageLoaded(aggregationTask, index);
                    }

                    @Override
                    public void onFailed(AggregationTaskStageState currentTaskStageState) {
                        onPageLoadingFailed(index, currentTaskStageState.getExceptions());
                    }
                });
            }

            @Override
            public void onLoaded(AggregationTaskStageState currentTaskStageState) {
                super.onLoaded(currentTaskStageState);
                processOnPageLoaded(aggregationTask, index);
            }

            @Override
            public void onFailed(AggregationTaskStageState currentTaskStageState) {
                super.onFailed(currentTaskStageState);
                onPageLoadingFailed(index, currentTaskStageState.getExceptions());
            }
        });
    }

    @Override
    protected void resetInternal() {
        super.resetInternal();
        items.clear();
    }

}
