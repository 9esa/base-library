package org.zuzuk.baseui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.SpiceManager;

import org.zuzuk.baseui.R;
import org.zuzuk.dataproviding.requests.base.ResultListener;
import org.zuzuk.dataproviding.requests.base.Task;
import org.zuzuk.dataproviding.requests.base.TaskResultController;
import org.zuzuk.dataproviding.requests.local.base.LocalLoader;
import org.zuzuk.dataproviding.requests.local.base.TaskExecutor;
import org.zuzuk.dataproviding.requests.remote.base.RemoteRequest;
import org.zuzuk.dataproviding.requests.remote.base.RequestCacheWrapper;
import org.zuzuk.dataproviding.requests.remote.base.RequestExecutor;
import org.zuzuk.dataproviding.requests.remote.base.SpiceManagerProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Fragment that include base logic of loading, remote requesting and refreshing data.
 * Also it is responsible to show current state of requesting (loading/need refresh/loaded)
 */
public abstract class LoadingFragment extends BaseFragment
        implements TaskExecutor, RequestExecutor {
    private static int lastGeneratedId = 1;
    private final List<Integer> loadersIds = new ArrayList<>();
    private final List<TaskResultController> taskResultControllers = new ArrayList<>();
    private SpiceManager spiceManager;

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
        if (getActivity().getApplication() instanceof SpiceManagerProvider) {
            spiceManager = ((SpiceManagerProvider) getActivity().getApplication()).createSpiceManager();
        }
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
                                   ResultListener resultListener) {
        addResultController(taskResultController);

        if (task instanceof RemoteRequest) {
            executeRequest((RemoteRequest) task, resultListener);
        } else {
            executeTask(task, resultListener);
        }
    }

    /**
     * Continues started request over fragment with blocking UI interactions
     * till resultController calls fireOnFailure or fireOnSuccess.
     */
    public void continueScreenRequest(TaskResultController taskResultController,
                                      Task task,
                                      ResultListener resultListener) {
        if (!taskResultControllers.contains(taskResultController))
            throw new IllegalStateException("Sent resultController has wrong id");

        if (task instanceof RemoteRequest) {
            executeRequest((RemoteRequest) task, resultListener);
        } else {
            executeTask(task, resultListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (spiceManager != null) {
            spiceManager.start(getActivity());
        }

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

        for (Integer loaderId : loadersIds) {
            getLoaderManager().getLoader(loaderId).stopLoading();
            getLoaderManager().destroyLoader(loaderId);
        }
        loadersIds.clear();

        if (spiceManager != null) {
            spiceManager.shouldStop();
        }
    }

    /* Executing request via AsyncTask */
    @Override
    public <T> Loader<T> executeTask(Task<T> task, ResultListener<T> resultListener) {
        int loaderId = lastGeneratedId++;
        loadersIds.add(loaderId);
        return getLoaderManager().initLoader(loaderId, null, new LocalLoader<>(getActivity(), task, resultListener));
    }

    /* Executing request via RoboSpice SpiceManager with cache provided by RequestCacheWrapper */
    @Override
    public <T> void executeRequest(RemoteRequest<T> request, ResultListener<T> resultListener) {
        if (spiceManager == null)
            throw new RuntimeException("To execute request you should let Application implement SpiceManagerProvider");

        new RequestCacheWrapper<>(request, resultListener).execute(spiceManager);
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
