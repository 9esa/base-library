package org.zuzuk.dataproviding.providers.base;

import org.zuzuk.dataproviding.requests.base.Task;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that needed to create and parse tasks for paging-based providers
 */
public interface PagingTaskCreator<TItem, TResponse> {

    /* Creates request */
    public abstract Task<TResponse> createTask(int offset, int limit);

    /* Parses response of task result */
    public abstract List<TItem> parseResponse(TResponse response);
}