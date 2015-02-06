package org.zuzuk.providers;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.providers.base.PagingProvider;
import org.zuzuk.providers.base.PagingTaskCreator;
import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.IteratorInitializationRequest;
import org.zuzuk.tasks.local.IteratorRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Stack;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that based on OrmLite (database) iterator
 */
public class IteratorProvider<TItem extends Serializable> extends PagingProvider<TItem> implements PagingTaskCreator<TItem, List> {
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
    protected void initializeInternal(final int startPosition) {
        updateIterator(startPosition);
    }

    /* Manually updating provider (if caller know that database have changed) */
    public void updateIterator() {
        updateIterator(null);
    }

    private void updateIterator(final Integer startPosition) {
        taskExecutor.executeRealLoadingTask(queryBuilder != null
                        ? new IteratorInitializationRequest<>(queryBuilder, dao, isKnownCount)
                        : new IteratorInitializationRequest<>(where, dao, isKnownCount),
                new RequestListener<IteratorInitializationRequest.Response>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                        onInitializationFailed(spiceException);
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public void onRequestSuccess(IteratorInitializationRequest.Response response) {
                        setTotalCount(response.getCount());
                        iterator = response.getIterator();
                        currentPosition = 0;
                        getRequestingPages().clear();
                        getPages().clear();
                        if (startPosition != null) {
                            IteratorProvider.super.initializeInternal(startPosition);
                        } else {
                            onDataSetChanged();
                        }

                    }
                }, null);
    }

    @Override
    protected void requestPage(final int index) {
        if (getRequestingPages().isEmpty()) {
            getRequestingPages().add(index);
            taskExecutor.executeRealLoadingTask(createTask(index * DEFAULT_ITEMS_ON_PAGE, DEFAULT_ITEMS_ON_PAGE),
                    new RequestListener<List>() {
                        @Override
                        public void onRequestSuccess(List list) {
                            onPageLoaded(index, parseResponse(list));
                        }

                        @Override
                        public void onRequestFailure(SpiceException spiceException) {
                            onPageLoadingFailed(index, spiceException);
                        }
                    }, null);
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
    @SuppressWarnings("unchecked")
    public List<TItem> parseResponse(List items) {
        return (List<TItem>) items;
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

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new RuntimeException("This object cannot be serialized");
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new RuntimeException("This object cannot be deserialized");
    }
}
