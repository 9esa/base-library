package org.zuzuk.tasks.aggregationtask;

public interface AggregationTaskExecutor {

    /* Executes aggregation task */
    public void executeAggregationTask(AggregationTask aggregationTask);

    /* Cancels aggregation task */
    public void cancelAggregationTask(AggregationTask aggregationTask);

}
