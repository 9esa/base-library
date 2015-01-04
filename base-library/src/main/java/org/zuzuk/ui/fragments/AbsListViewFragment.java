package org.zuzuk.ui.fragments;

import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.apache.commons.lang3.ArrayUtils;
import org.zuzuk.ui.adapters.CheckableAdapter;
import org.zuzuk.ui.adapters.ProviderAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Loading fragment that based on ListView so it have common restore and select logic
 */
public abstract class AbsListViewFragment extends LoadingFragment {
    /* Selection type of ListView */
    protected enum SelectionType {
        // No selection supports
        None,
        // Supports only single item selection by tap
        SingleChoice,
        // Supports multiple items selection by taps
        MultipleChoice,
        // Supports multiple items selection by activating selection mode
        SelectionMode
    }

    private final static String LIST_POSITION_KEY = "LIST_POSITION_KEY";
    private final static String LIST_TOP_MARGIN_KEY = "LIST_TOP_MARGIN_KEY";
    private final static String CHECKED_ITEMS_KEY = "CHECKED_ITEMS_KEY";

    private int listPosition;
    private int listTopMargin;
    private List<Integer> checkedItems;
    private boolean isInActionMode = false;
    private boolean isListViewStateValid = false;
    private AbsListView absListView;
    private final Runnable updatePositionAction = new Runnable() {
        public void run() {
            restoreListViewState();
        }
    };

    /**
     * Returns adapter of base ListView so developer should create it with view initialization.
     * It is normal because adapter should only responses for UI but not for logic
     */
    protected Adapter getAdapter() {
        if (absListView == null) {
            return null;
        }

        ListAdapter baseAdapter = absListView.getAdapter();
        if (baseAdapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) baseAdapter).getWrappedAdapter();
        }
        return baseAdapter;
    }

    /* Returns last list position of ListView */
    protected int getListPosition() {
        return listPosition;
    }

    /* Resets fragment current state. (e.g. clears cached list position of ListView) */
    protected void resetFragmentState() {
        listPosition = 0;
        listTopMargin = 0;
        checkedItems = null;
    }

    /* Returns selection type of ListView */
    protected SelectionType getSelectionType() {
        return SelectionType.None;
    }

    /**
     * Returns callback needed by selection mode.
     * Developer should firstly implement ActionMode.Callback class or use
     * AbsListViewFragment.SelectionModeCallback class as base.
     */
    protected ActionMode.Callback getSelectionModeCallback() {
        return null;
    }

    /* Activates selection mode */
    protected void activateSelectionMode(int initialPosition) {
        if (isInActionMode) {
            return;
        }

        if (initialPosition >= 0) {
            ((CheckableAdapter) getAdapter()).setItemChecked(initialPosition,
                    true,
                    getSelectionType() != SelectionType.MultipleChoice);
        }

        getBaseActivity().startSupportActionMode(getSelectionModeCallback());
    }

    /* Restores list view state. Usually after fragment loading */
    protected void restoreListViewState() {
        if (absListView instanceof ListView) {
            ((ListView) absListView).setSelectionFromTop(listPosition, listTopMargin);
        } else {
            absListView.setSelection(listPosition);
        }

        if (checkedItems != null) {
            CheckableAdapter adapter = (CheckableAdapter) getAdapter();
            boolean isSingleChoice = getSelectionType() != SelectionType.MultipleChoice;
            for (int item : checkedItems) {
                adapter.setItemChecked(item, true, isSingleChoice);
                onSelectionChanged();
            }
        }
        isListViewStateValid = true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        absListView = findViewByType(AbsListView.class, getView());
    }

    @Override
    public void onLoaded(boolean isInBackground, boolean isFromCache) {
        super.onLoaded(isInBackground, isFromCache);
        if (!isListViewStateValid) {
            getPostHandler().post(updatePositionAction);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        absListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int headersCount = absListView instanceof ListView ? ((ListView) absListView).getHeaderViewsCount() : 0;
                int listItemPosition = position - headersCount;
                if (listItemPosition < 0 || listItemPosition >= getAdapter().getCount()) {
                    return;
                }

                if (getSelectionType() == SelectionType.None) {
                    AbsListViewFragment.this.onItemClick(listItemPosition);
                    return;
                }

                CheckableAdapter adapter = (CheckableAdapter) getAdapter();
                switch (getSelectionType()) {
                    case SingleChoice:
                        adapter.setItemChecked(listItemPosition, !adapter.isItemChecked(listItemPosition), true);
                        onSelectionChanged();
                        break;
                    case MultipleChoice:
                        adapter.setItemChecked(listItemPosition, !adapter.isItemChecked(listItemPosition), false);
                        onSelectionChanged();
                        break;
                    case SelectionMode:
                        if (isInActionMode) {
                            adapter.setItemChecked(listItemPosition, !adapter.isItemChecked(listItemPosition), false);
                            onSelectionChanged();
                        } else {
                            AbsListViewFragment.this.onItemClick(listItemPosition);
                        }
                        break;
                }
            }
        });

        if (getSelectionType() == SelectionType.SelectionMode) {
            absListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    int headersCount = absListView instanceof ListView ? ((ListView) absListView).getHeaderViewsCount() : 0;
                    activateSelectionMode(position - headersCount);
                    return true;
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        listPosition = absListView.getFirstVisiblePosition();
        View topItem = absListView.getChildAt(0);
        listTopMargin = topItem != null ? topItem.getTop() : 0;

        if (getAdapter() instanceof CheckableAdapter) {
            checkedItems = (isInActionMode || getAdapter() == null)
                    ? null
                    : ((CheckableAdapter) getAdapter()).getCheckedPositions();
        }
        isListViewStateValid = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(LIST_POSITION_KEY, listPosition);
        outState.putInt(LIST_TOP_MARGIN_KEY, listTopMargin);
        if (checkedItems != null) {
            Integer[] checkedPositions = new Integer[checkedItems.size()];
            checkedItems.toArray(checkedPositions);
            outState.putIntArray(CHECKED_ITEMS_KEY, ArrayUtils.toPrimitive(checkedPositions));
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            listPosition = savedInstanceState.getInt(LIST_POSITION_KEY, 0);
            listTopMargin = savedInstanceState.getInt(LIST_TOP_MARGIN_KEY, 0);
            int[] checkedItemsArray = savedInstanceState.getIntArray(CHECKED_ITEMS_KEY);
            checkedItems = checkedItemsArray != null
                    ? Arrays.asList(ArrayUtils.toObject(checkedItemsArray))
                    : null;
        }
    }

    /* Raises when user tap on ListView item */
    protected void onItemClick(int position) {
    }

    @Override
    public void onDestroyView() {
        Adapter adapter = getAdapter();
        if (adapter instanceof ProviderAdapter) {
            ((ProviderAdapter) adapter).dispose();
        }
        super.onDestroyView();
        absListView = null;
    }

    /* Raises when ListView selection changes */
    protected void onSelectionChanged() {
        getActivity().supportInvalidateOptionsMenu();
    }

    /* Base class that implements ActionMode.Callback */
    protected abstract class SelectionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            isInActionMode = true;
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            isInActionMode = false;
            ((CheckableAdapter) getAdapter()).clearChecks();
        }
    }
}
