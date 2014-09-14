package org.zuzuk.providers.base;

import android.util.SparseArray;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Provider that supports paging-based loading
 */
public abstract class PagingProvider<TItem extends Serializable> extends LoadingItemsProvider<TItem> {
    public static final int DEFAULT_ITEMS_ON_PAGE = 20;

    private Integer totalCount = null;
    private SparseArray<ArrayList<TItem>> pages = new SparseArray<>();
    private final HashSet<Integer> requestingPages = new HashSet<>();

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

    @Override
    public TItem getItem(int position) {
        int pageIndex = position / DEFAULT_ITEMS_ON_PAGE;
        int itemIndex = position % DEFAULT_ITEMS_ON_PAGE;
        ArrayList<TItem> pageItems = pages.get(pageIndex);

        if (pageItems == null && !requestingPages.contains(pageIndex)) {
            requestPage(pageIndex);
        } else if (pageIndex > 0 && itemIndex < DEFAULT_ITEMS_ON_PAGE / 2
                && pages.get(pageIndex - 1) == null && !requestingPages.contains(pageIndex - 1)) {
            requestPage(pageIndex - 1);
        } else if (totalCount == null && itemIndex > DEFAULT_ITEMS_ON_PAGE / 2
                && pages.get(pageIndex + 1) == null && !requestingPages.contains(pageIndex + 1)) {
            requestPage(pageIndex + 1);
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
    protected void initialize(int startPosition) {
        requestPage(startPosition / DEFAULT_ITEMS_ON_PAGE);
    }

    /* Logic of page requesting */
    protected abstract void requestPage(int index);

    /* Raises when page loaded. Use it in child classes */
    protected void onPageLoaded(int pageIndex, List<TItem> items) {
        if (items.size() > DEFAULT_ITEMS_ON_PAGE) {
            throw new RuntimeException("Wrong result items count: " + items.size());
        }

        ArrayList<TItem> pageItems = new ArrayList<>(items);
        pages.put(pageIndex, pageItems);
        if (totalCount == null && pageItems.size() < DEFAULT_ITEMS_ON_PAGE) {
            setTotalCount(pageIndex * DEFAULT_ITEMS_ON_PAGE + items.size());
        }
    }

    @Override
    protected void writeObject(ObjectOutputStream out) throws IOException {
        super.writeObject(out);
        out.writeInt(totalCount != null ? totalCount : -1);
        out.writeObject(pages);
    }

    @Override
    protected void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readObject(in);
        totalCount = in.readInt();
        if (totalCount == -1) {
            totalCount = null;
        }

        pages = (SparseArray<ArrayList<TItem>>) in.readObject();
    }
}
