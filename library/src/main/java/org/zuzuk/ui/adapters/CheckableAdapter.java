package org.zuzuk.ui.adapters;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Adapter that supports item checking
 */
public interface CheckableAdapter {

    /**
     * Sets is item checked.
     * int index - position of item
     * boolean isChecked - is item checked
     * boolean isSingleCheck - should it be only one checked item or not
     */
    void setItemChecked(int index, boolean isChecked, boolean isSingleCheck);

    /* Returns is item checked by position */
    boolean isItemChecked(int index);

    /* Returns list of checked items */
    List<Integer> getCheckedPositions();

    /* Clears checked items */
    void clearChecks();
}
