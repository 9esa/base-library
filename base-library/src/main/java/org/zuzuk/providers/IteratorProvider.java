package org.zuzuk.providers;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.providers.base.PagingProvider;
import org.zuzuk.tasks.aggregationtask.AggregationTask;
import org.zuzuk.tasks.aggregationtask.AggregationTaskExecutor;
import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;
import org.zuzuk.tasks.aggregationtask.OnlyRealLoadingAggregationTask;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;
import org.zuzuk.tasks.local.IteratorInitializationRequest;
import org.zuzuk.tasks.local.IteratorRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on OrmLite (database) iterator
 */
public class IteratorProvider<TItem, TRequestAndTaskExecutor extends RequestAndTaskExecutor> extends PagingProvider<TItem, TRequestAndTaskExecutor> {

    private final AggregationTaskExecutor<TRequestAndTaskExecutor> aggregationTaskExecutor;
    private final Stack<Integer> waitingForRequestPages = new Stack<>();
    private final RuntimeExceptionDao<TItem, ?> dao;
    private QueryBuilder<TItem, ?> queryBuilder;
    private Where<TItem, ?> where;
    private CloseableIterator<TItem> iterator;
    private int currentPosition;
    private boolean isKnownCount = true;

    /**
     * Sets is count of query should be requested.
     * Requesting count is long operation and avoiding this operation can reduce data loading time
     */
    public void setKnownCount(boolean isKnownCount) {
        this.isKnownCount = isKnownCount;
    }

    public IteratorProvider(AggregationTaskExecutor<TRequestAndTaskExecutor> aggregationTaskExecutor, QueryBuilder<TItem, ?> queryBuilder, RuntimeExceptionDao<TItem, ?> dao) {
        this.aggregationTaskExecutor = aggregationTaskExecutor;
        this.queryBuilder = queryBuilder;
        this.dao = dao;
    }

    public IteratorProvider(AggregationTaskExecutor<TRequestAndTaskExecutor> aggregationTaskExecutor, Where<TItem, ?> where, RuntimeExceptionDao<TItem, ?> dao) {
        this.aggregationTaskExecutor = aggregationTaskExecutor;
        this.where = where;
        this.dao = dao;
    }

    @Override
    protected void initializeInternal(int startPosition, TRequestAndTaskExecutor executor, AggregationTaskStageState stageState) {
        updateIterator(startPosition, executor, stageState);
    }

    /* Manually updating provider (if caller know that database have changed) */
    public void updateIterator() {
        updateIterator(null, null, null);
    }

    /* Manually updating provider (if caller know that database have changed) */
    public void updateIterator(TRequestAndTaskExecutor executor, AggregationTaskStageState stageState) {
        updateIterator(null, executor, stageState);
    }

    private void updateIterator(final Integer startPosition, TRequestAndTaskExecutor executor, AggregationTaskStageState stageState) {
        AggregationTask<TRequestAndTaskExecutor> aggregationTask = new OnlyRealLoadingAggregationTask<TRequestAndTaskExecutor>() {
            @Override
            protected void realLoad(final TRequestAndTaskExecutor executor, final AggregationTaskStageState currentTaskStageState) {
                executor.executeTask(queryBuilder != null ?
                                new IteratorInitializationRequest<>(queryBuilder, dao, isKnownCount) :
                                new IteratorInitializationRequest<>(where, dao, isKnownCount),
                        new RequestListener<IteratorInitializationRequest.Response>() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public void onRequestSuccess(IteratorInitializationRequest.Response response) {
                                setTotalCount(response.getCount());
                                iterator = response.getIterator();
                                currentPosition = 0;
                                getRequestingPages().clear();
                                getPages().clear();
                                if (startPosition != null) {
                                    IteratorProvider.super.initializeInternal(startPosition, executor, currentTaskStageState);
                                } else {
                                    onDataSetChanged();
                                }
                            }

                            @Override
                            public void onRequestFailure(SpiceException spiceException) {
                                onInitializationFailed(Arrays.asList((Exception) spiceException));
                            }
                        });
            }
        };

        if (executor == null) {
            aggregationTaskExecutor.executeAggregationTask(aggregationTask);
        } else {
            aggregationTask.load(executor, stageState);
        }
    }

    @Override
    protected void requestPage(final int index, TRequestAndTaskExecutor executor, AggregationTaskStageState stageState) {
        if (getRequestingPages().isEmpty()) {
            AggregationTask<TRequestAndTaskExecutor> aggregationTask = new OnlyRealLoadingAggregationTask<TRequestAndTaskExecutor>() {
                @Override
                protected void realLoad(TRequestAndTaskExecutor executor, AggregationTaskStageState currentTaskStageState) {
                    //TODO: is it ok?
                    getRequestingPages().add(index);
                    executor.executeTask(new IteratorRequest<>(iterator, currentPosition, index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE),
                            new RequestListener<List>() {
                                @SuppressWarnings("unchecked")
                                @Override
                                public void onRequestSuccess(List list) {
                                    onPageLoaded(index, (List<TItem>) list);
                                }

                                @Override
                                public void onRequestFailure(SpiceException spiceException) {
                                    onPageLoadingFailed(index, Arrays.asList((Exception) spiceException));
                                }
                            });
                }
            };

            if (stageState == null) {
                aggregationTaskExecutor.executeAggregationTask(aggregationTask);
            } else {
                //TODO: is it ok?
                getRequestingPages().add(index);
                aggregationTask.load(executor, stageState);
            }
        } else {
            waitingForRequestPages.push(index);
        }
    }

    @Override
    protected void onPageLoaded(int pageIndex, List<TItem> items) {
        super.onPageLoaded(pageIndex, items);
        currentPosition = pageIndex * DEFAULT_ITEMS_ON_PAGE + items.size();
        if (!waitingForRequestPages.isEmpty()) {
            requestPage(waitingForRequestPages.pop(), null, null);
        }
    }

    @Override
    protected void resetInternal() {
        super.resetInternal();
        waitingForRequestPages.clear();
    }

    /* Disposes iterator to avoid memory leaks and open cursors */
    public void dispose() {
        resetInternal();
        if (iterator != null) {
            iterator.closeQuietly();
        }
    }

}
