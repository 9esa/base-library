package org.zuzuk.providers.base;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.remote.base.RemoteRequest;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that needed to create and parse tasks for paging-based providers
 */
public interface PagingTaskCreator<TItem, TResponse> extends Serializable {

    /* Creates page loading task */
    public abstract Task<TResponse> createTask(int offset, int limit);

    /* Parses response of task result */
    public abstract List<TItem> parseResponse(TResponse response);
}