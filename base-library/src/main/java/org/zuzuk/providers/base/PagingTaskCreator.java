package org.zuzuk.providers.base;

import org.zuzuk.tasks.aggregationtask.AggregationPagingTask;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that needed to create and parse tasks for paging-based providers
 */
public interface PagingTaskCreator<TItem> {

    /* Creates page loading task */
    public abstract AggregationPagingTask<TItem> createPagingTask(int offset, int limit);

}