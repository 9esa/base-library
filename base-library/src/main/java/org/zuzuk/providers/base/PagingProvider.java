package org.zuzuk.providers.base;

import android.util.SparseArray;

import org.zuzuk.tasks.aggregationtask.AggregationTaskStageState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that supports paging-based loading
 */
public abstract class PagingProvider<TItem> extends LoadingItemsProvider<TItem> {
    public static final int DEFAULT_ITEMS_ON_PAGE = 20;

    private OnPageLoadedListener<TItem> onPageLoadedListener;
    private Integer totalCount = null;
    private SparseArray<ArrayList<TItem>> pages = new SparseArray<>();
    private HashSet<Integer> requestingPages = new HashSet<>();

    /* Sets total count of items */
    protected void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    /* Returns loaded pages of data */
    protected SparseArray<ArrayList<TItem>> getPages() {
        return pages;
    }

    /* Returns currently requesting pages of data */
    protected HashSet<Integer> getRequestingPages() {
        return requestingPages;
    }

    /* Sets page loading listener */
    public void setOnPageLoadedListener(OnPageLoadedListener<TItem> onPageLoadedListener) {
        this.onPageLoadedListener = onPageLoadedListener;
    }

    @Override
    public TItem getItem(int position) {
        int pageIndex = position / DEFAULT_ITEMS_ON_PAGE;
        int itemIndex = position % DEFAULT_ITEMS_ON_PAGE;
        ArrayList<TItem> pageItems = pages.get(pageIndex);

        if (pageItems == null && !requestingPages.contains(pageIndex)) {
            requestPage(pageIndex, null);
        } else if (pageIndex > 0 && itemIndex < DEFAULT_ITEMS_ON_PAGE / 2
                && pages.get(pageIndex - 1) == null && !requestingPages.contains(pageIndex - 1)) {
            requestPage(pageIndex - 1, null);
        } else if (totalCount == null && itemIndex > DEFAULT_ITEMS_ON_PAGE / 2
                && pages.get(pageIndex + 1) == null && !requestingPages.contains(pageIndex + 1)) {
            requestPage(pageIndex + 1, null);
        }

        return pageItems != null ? pageItems.get(itemIndex) : null;
    }

    @Override
    public TItem getAvailableItem(int position) {
        return pages.valueAt(position / DEFAULT_ITEMS_ON_PAGE).get(position % DEFAULT_ITEMS_ON_PAGE);
    }

    @Override
    public int getTotalCount() {
        if (totalCount != null) {
            return totalCount;
        }

        if (!isInitialized()) {
            return 0;
        }

        int maxPageIndex = -1;
        for (int i = 0; i < pages.size(); i++) {
            maxPageIndex = Math.max(maxPageIndex, pages.keyAt(i));
        }
        return (maxPageIndex + 1) * DEFAULT_ITEMS_ON_PAGE + 1;
    }

    @Override
    public int getAvailableCount() {
        if (!isInitialized()) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < pages.size(); i++) {
            result += pages.valueAt(i).size();
        }
        return result;
    }

    @Override
    protected void initializeInternal(int startPosition, AggregationTaskStageState stageState) {
        requestPage(startPosition / DEFAULT_ITEMS_ON_PAGE, stageState);
        if (startPosition >= DEFAULT_ITEMS_ON_PAGE) {
            requestPage((startPosition / DEFAULT_ITEMS_ON_PAGE) - 1, stageState);
        }
    }

    /* Logic of page requesting */
    protected abstract void requestPage(int index, AggregationTaskStageState stageState);

    /* Raises when page loaded. Use it in child classes */
    protected void onPageLoaded(int pageIndex, List<TItem> items) {
        if (!getRequestingPages().contains(pageIndex)) {
            return;
        }

        int itemsSize = items != null ? items.size() : 0;
        if (itemsSize > DEFAULT_ITEMS_ON_PAGE)
            throw new RuntimeException("Wrong result items count: " + itemsSize);

        ArrayList<TItem> pageItems = items != null ? new ArrayList<>(items) : new ArrayList<TItem>(0);
        pages.put(pageIndex, pageItems);
        if (totalCount == null && pageItems.size() < DEFAULT_ITEMS_ON_PAGE) {
            setTotalCount(pageIndex * DEFAULT_ITEMS_ON_PAGE + itemsSize);
        }

        if (onPageLoadedListener != null) {
            onPageLoadedListener.onPageLoaded(pageIndex, items);
        }

        getRequestingPages().remove(pageIndex);
        if (!isInitialized()) {
            onInitialized();
        } else {
            onDataSetChanged();
        }
    }

    protected void onPageLoadingFailed(int pageIndex, List<Exception> exceptions) {
        if (!getRequestingPages().contains(pageIndex)) {
            return;
        }

        getRequestingPages().remove(pageIndex);
        if (!isInitialized()) {
            onInitializationFailed(exceptions);
        }
    }

    @Override
    protected void resetInternal() {
        getRequestingPages().clear();
        getPages().clear();
        totalCount = null;
    }
}
