package org.zuzuk.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.AggregationTask;
import org.zuzuk.tasks.TaskExecutorHelper;
import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.RequestWrapper;
import org.zuzuk.ui.R;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Fragment that include base logic of loading, remote requesting and refreshing data.
 * Also it is responsible to show current state of requesting (loading/need refresh/loaded)
 */
public abstract class LoadingFragment extends BaseFragment
        implements TaskExecutor, RequestExecutor, AggregationTask {
    private final TaskExecutorHelper taskExecutorHelper = new TaskExecutorHelper() {
        @Override
        public AggregationTask createTemporaryTask() {
            return new AggregationTask() {
                @Override
                public boolean isLoadingNeeded() {
                    return false;
                }

                @Override
                public boolean isLoaded() {
                    return true;
                }

                @Override
                public void load(boolean b) {
                }

                @Override
                public void onLoadingStarted(boolean b) {
                    findViewById(R.id.loadingProgressBar).setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoaded() {
                    findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
                }

                @Override
                public void onFailed(Exception e) {
                    findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
                }
            };
        }
    };

    @Override
    public boolean isLoadingNeeded() {
        return !isLoaded();
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
        int loadingFragmentLayoutId = R.layout.fragment_loading;
        TypedValue a = new TypedValue();
        if (inflater.getContext().getTheme().resolveAttribute(R.attr.loadingFragmentLayout, a, true)) {
            loadingFragmentLayoutId = a.resourceId;
        }

        View view = inflater.inflate(loadingFragmentLayoutId, container, false);

        ViewGroup contentContainerView = (ViewGroup) view.findViewById(R.id.loadingContentContainer);
        View contentView = createContentView(inflater, contentContainerView, savedInstanceState);
        contentContainerView.addView(contentView);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViewById(R.id.loadingRefreshButton).setOnClickListener(new View.OnClickListener() {
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
            findViewById(R.id.loadingRefreshButton).setVisibility(View.GONE);
            findViewById(R.id.loadingProgressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.loadingContentContainer).setVisibility(isLoaded() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onLoaded() {
        findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        if (isLoaded()) {
            findViewById(R.id.loadingContentContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.loadingRefreshButton).setVisibility(View.GONE);
        } else {
            findViewById(R.id.loadingContentContainer).setVisibility(View.INVISIBLE);
            findViewById(R.id.loadingRefreshButton).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFailed(Exception ex) {
        findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        if (!isLoaded()) {
            findViewById(R.id.loadingContentContainer).setVisibility(View.INVISIBLE);
            findViewById(R.id.loadingRefreshButton).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        taskExecutorHelper.onResume();
        taskExecutorHelper.reload(false);
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

    /* Reloads fragment */
    public void reload(boolean isInBackground) {
        taskExecutorHelper.reload(isInBackground);
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request, RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequest(request, requestListener);
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        taskExecutorHelper.executeRequest(requestWrapper);
    }

    @Override
    public <T> void executeRequestBackground(RemoteRequest<T> request, RequestListener<T> requestListener) {
        taskExecutorHelper.executeRequestBackground(request, requestListener);
    }

    @Override
    public <T> void executeRequestBackground(RequestWrapper<T> requestWrapper) {
        taskExecutorHelper.executeRequestBackground(requestWrapper);
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
