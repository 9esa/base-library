package org.zuzuk.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.R;
import org.zuzuk.tasks.AggregationTask;
import org.zuzuk.tasks.TaskExecutorHelper;
import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.RequestCacheWrapper;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.RequestWrapper;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Fragment that include base logic of loading, remote requesting and refreshing data.
 * Also it is responsible to show current state of requesting (loading/need refresh/loaded)
 */
public abstract class LoadingFragment extends BaseFragment
        implements TaskExecutor, RequestExecutor, AggregationTask {
    private final TaskExecutorHelper taskExecutorHelper = new TaskExecutorHelper();

    @Override
    public boolean isLoadingNeeded() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskExecutorHelper.addLoadingTask(this);
    }

    /* Returns content view. It will blocks on loading and screen requests */
    protected abstract View createContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        ViewGroup contentContainerView = (ViewGroup) view.findViewById(R.id.contentContainer);
        View contentView = createContentView(inflater, contentContainerView, savedInstanceState);
        contentContainerView.addView(contentView);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViewById(R.id.refreshBtn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                taskExecutorHelper.reload(false);
            }
        });

        taskExecutorHelper.onCreate(view.getContext());
    }

    @Override
    public void load(boolean isInBackground) {
        loadFragment();
    }

    @Override
    public void onLoadingStarted(boolean isInBackground) {
        if (!isInBackground) {
            findViewById(R.id.refreshBtn).setVisibility(View.GONE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.contentContainer).setVisibility(isLoaded() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onLoaded() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        if (isLoaded()) {
            findViewById(R.id.contentContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.refreshBtn).setVisibility(View.GONE);
        } else {
            findViewById(R.id.contentContainer).setVisibility(View.INVISIBLE);
            findViewById(R.id.refreshBtn).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFailed(Exception ex) {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        if (!isLoaded()) {
            findViewById(R.id.contentContainer).setVisibility(View.INVISIBLE);
            findViewById(R.id.refreshBtn).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        taskExecutorHelper.onResume();
    }

    /* Logic of loading fragment's content */
    protected abstract void loadFragment();

    @Override
    public void onPause() {
        super.onPause();
        taskExecutorHelper.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        taskExecutorHelper.onDestroy();
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request, RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequest(new RequestCacheWrapper<>(request), requestListener);
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper, RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequest(requestWrapper, requestListener);
    }

    @Override
    public <T> void executeRequestBackground(RemoteRequest<T> request, RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequestBackground(new RequestCacheWrapper<>(request), requestListener);
    }

    @Override
    public <T> void executeRequestBackground(RequestWrapper<T> requestWrapper, RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequestBackground(requestWrapper, requestListener);
    }

    @Override
    public <T> void executeTask(Task<T> task, RequestListener<T> requestListener) {
        taskExecutorHelper.executeTask(task, requestListener);
    }

    @Override
    public void executeTask(LocalTask task) {
        taskExecutorHelper.executeTask(task);
    }

    @Override
    public <T> void executeTaskBackground(Task<T> task, RequestListener<T> requestListener) {
        taskExecutorHelper.executeTaskBackground(task, requestListener);
    }

    @Override
    public void executeTaskBackground(LocalTask task) {
        taskExecutorHelper.executeTaskBackground(task);
    }
}
