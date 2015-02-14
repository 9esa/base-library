package org.zuzuk.tasks.aggregationtask;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 08/02/2015.
 * Aggregation task to get items of page
 */
public interface AggregationPagingTask<TItem> extends AggregationTask {

    /* Returns items got during task execution */
    public abstract List<TItem> getPageItems();

}
