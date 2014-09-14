package org.zuzuk.dataproviding.providers;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.zuzuk.dataproviding.providers.base.PagingProvider;
import org.zuzuk.dataproviding.providers.base.PagingTaskCreator;
import org.zuzuk.dataproviding.requests.base.ResultListener;
import org.zuzuk.dataproviding.requests.base.Task;
import org.zuzuk.dataproviding.requests.local.IteratorInitializationRequest;
import org.zuzuk.dataproviding.requests.local.IteratorRequest;
import org.zuzuk.dataproviding.requests.local.base.TaskExecutor;

import java.util.List;
import java.util.Stack;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on OrmLite (database) iterator
 */
public class IteratorProvider<TItem> extends PagingProvider<TItem> implements PagingTaskCreator<TItem, List> {
    private final TaskExecutor taskExecutor;
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

    public IteratorProvider(TaskExecutor taskExecutor, QueryBuilder<TItem, ?> queryBuilder, RuntimeExceptionDao<TItem, ?> dao) {
        this.taskExecutor = taskExecutor;
        this.queryBuilder = queryBuilder;
        this.dao = dao;
    }

    public IteratorProvider(TaskExecutor taskExecutor, Where<TItem, ?> where, RuntimeExceptionDao<TItem, ?> dao) {
        this.taskExecutor = taskExecutor;
        this.where = where;
        this.dao = dao;
    }

    @Override
    protected void initialize(final int startPosition) {
        updateIterator(startPosition);
    }

    /* Manually updating provider (if caller know that database have changed) */
    public void updateIterator() {
        updateIterator(null);
    }

    private void updateIterator(final Integer startPosition) {
        taskExecutor.executeTask(queryBuilder != null
                ? new IteratorInitializationRequest<>(queryBuilder, dao, isKnownCount)
                : new IteratorInitializationRequest<>(where, dao, isKnownCount), new ResultListener<IteratorInitializationRequest.Response>() {
            @Override
            public void onSuccess(IteratorInitializationRequest.Response response) {
                setTotalCount(response.getCount());
                iterator = response.getIterator();
                currentPosition = 0;
                getRequestingPages().clear();
                getPages().clear();
                if (startPosition != null) {
                    IteratorProvider.super.initialize(startPosition);
                } else {
                    onDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception ex) {
                onInitializationFailed(ex);
            }
        });
    }

    @Override
    protected void requestPage(final int index) {
        if (getRequestingPages().isEmpty()) {
            getRequestingPages().add(index);
            taskExecutor.executeTask(createTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE), new ResultListener<List>() {
                @Override
                public void onSuccess(List response) {
                    onPageLoaded(index, parseResponse(response));
                    getRequestingPages().remove(index);
                    onDataSetChanged();
                    if (!isInitialized()) {
                        onInitialized();
                    }
                }

                @Override
                public void onFailure(Exception ex) {
                    getRequestingPages().remove(index);
                    if (!isInitialized()) {
                        onInitializationFailed(ex);
                    }
                }
            });
        } else {
            waitingForRequestPages.push(index);
        }
    }

    @Override
    protected void onPageLoaded(int pageIndex, List<TItem> items) {
        super.onPageLoaded(pageIndex, items);
        currentPosition = pageIndex * DEFAULT_ITEMS_ON_PAGE + items.size();
        if (!waitingForRequestPages.isEmpty()) {
            requestPage(waitingForRequestPages.pop());
        }
    }

    @Override
    public Task<List> createTask(int offset, int limit) {
        return new IteratorRequest<>(iterator, currentPosition, offset, limit);
    }

    @Override
    public List<TItem> parseResponse(List items) {
        return (List<TItem>) items;
    }

    /* Disposes iterator to avoid memory leaks and open cursors */
    public void dispose() {
        getRequestingPages().clear();
        getPages().clear();
        if (iterator != null) {
            iterator.closeQuietly();
        }
    }
}
