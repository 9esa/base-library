package org.zuzuk.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.R;
import org.zuzuk.tasks.TaskExecutorHelper;
import org.zuzuk.tasks.TaskResultController;
import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.RequestCacheWrapper;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.RequestWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Fragment that include base logic of loading, remote requesting and refreshing data.
 * Also it is responsible to show current state of requesting (loading/need refresh/loaded)
 */
public abstract class LoadingFragment extends BaseFragment
        implements TaskExecutor, RequestExecutor {
    private final List<TaskResultController> taskResultControllers = new ArrayList<>();
    private final TaskExecutorHelper taskExecutorHelper = new TaskExecutorHelper();

    /**
     * Returns is fragment needs something to load or not.
     * If true then fragment will call loadFragment() method when fragment is resumed
     */
    protected boolean isLoadingNeeded() {
        return false;
    }

    /**
     * Returns is fragment have something to show.
     * If true then fragment will show view returned by createContentView() method
     */
    protected boolean isContentLoaded() {
        return true;
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
                refresh();
            }
        });

        findViewById(R.id.progressBar).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // need to block content view from user touches
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        taskExecutorHelper.onCreate(getActivity());
    }

    private void addResultController(final TaskResultController taskResultController) {
        taskResultControllers.add(taskResultController);

        if (taskResultControllers.size() == 1) {
            updateProgressViewState();
        }

        taskResultController.setOnCompletionListener(new TaskResultController.OnCompletionListener() {
            @Override
            public void onCompleted() {
                taskResultControllers.remove(taskResultController);

                if (taskResultControllers.isEmpty()) {
                    updateProgressViewState();
                }
            }
        });
    }

    /**
     * Starts request over fragment with blocking UI interactions
     * till resultController calls fireOnFailure or fireOnSuccess.
     */
    public void startScreenRequest(TaskResultController taskResultController,
                                   Task task,
                                   RequestListener requestListener) {
        addResultController(taskResultController);

        if (task instanceof RemoteRequest) {
            executeRequest((RemoteRequest) task, requestListener);
        } else {
            executeTask(task, requestListener);
        }
    }

    /**
     * Continues started request over fragment with blocking UI interactions
     * till resultController calls fireOnFailure or fireOnSuccess.
     */
    public void continueScreenRequest(TaskResultController taskResultController,
                                      Task task,
                                      RequestListener requestListener) {
        if (!taskResultControllers.contains(taskResultController))
            throw new IllegalStateException("Sent resultController has wrong id");

        if (task instanceof RemoteRequest) {
            executeRequest((RemoteRequest) task, requestListener);
        } else {
            executeTask(task, requestListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        taskExecutorHelper.onResume();

        updateProgressViewState();
        hideRefreshView();
        if (!isContentLoaded() || isLoadingNeeded()) {
            refresh();
        } else {
            onLoadSuccess();
            updateContentViewState();
        }
    }

    /* Logic of loading fragment's content */
    protected void loadFragment(TaskResultController taskResultController) {
        hideRefreshView();
        updateContentViewState();
    }

    /* Refreshing fragment's content by calling loadFragment() */
    protected void refresh() {
        TaskResultController taskResultController = new TaskResultController() {
            @Override
            public void fireOnSuccess() {
                super.fireOnSuccess();
                onLoadSuccess();
                updateContentViewState();
            }

            @Override
            public void fireOnFailure(Exception ex) {
                super.fireOnFailure(ex);
                onLoadFailure(ex);
                updateContentViewState();
                if (!isContentLoaded()) {
                    showRefreshView();
                }
            }
        };
        addResultController(taskResultController);

        loadFragment(taskResultController);
    }

    /* Raises after success loading of fragment */
    protected void onLoadSuccess() {
    }

    /* Raises after failed loading of fragment */
    protected void onLoadFailure(Exception ex) {
    }

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
    public void executeTask(LocalTask task) {
        taskExecutorHelper.executeTask(task);
    }

    @Override
    public <T> void executeTask(Task<T> task, RequestListener<T> requestListener) {
        taskExecutorHelper.executeTask(task, requestListener);
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        taskExecutorHelper.executeRequest(requestWrapper);
    }

    /* Executing request via RoboSpice SpiceManager with cache provided by RequestCacheWrapper */
    @Override
    public <T> void executeRequest(RemoteRequest<T> request, RequestListener<T> requestListener) {
        executeRequest(new RequestCacheWrapper<>(request, requestListener));
    }

    private void updateContentViewState() {
        if (isContentLoaded()) {
            findViewById(R.id.contentContainer).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.contentContainer).setVisibility(View.INVISIBLE);
        }
    }

    private void updateProgressViewState() {
        if (taskResultControllers.isEmpty()) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        } else {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }
    }

    private void showRefreshView() {
        findViewById(R.id.refreshBtn).setVisibility(View.VISIBLE);
    }

    private void hideRefreshView() {
        findViewById(R.id.refreshBtn).setVisibility(View.GONE);
    }
}
