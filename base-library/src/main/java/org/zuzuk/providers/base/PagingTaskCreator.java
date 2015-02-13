package org.zuzuk.providers.base;

import org.zuzuk.tasks.aggregationtask.AggregationPagingTask;
import org.zuzuk.tasks.aggregationtask.RequestAndTaskExecutor;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that needed to create and parse tasks for paging-based providers
 */
public interface PagingTaskCreator<TItem, TRequestAndTaskExecutor extends RequestAndTaskExecutor<TRequestAndTaskExecutor>> {

    /* Creates page loading task */
    public abstract AggregationPagingTask<TItem, TRequestAndTaskExecutor> createPagingTask(int offset, int limit);

}