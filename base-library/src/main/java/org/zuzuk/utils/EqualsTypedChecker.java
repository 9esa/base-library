package org.zuzuk.utils;

/**
 * Created by Gavriil Sitnikov on 11/10/2014.
 * Equals two objects by data (e.g. id or set of fields)
 */
public interface EqualsTypedChecker<T> {

    /* Return result of object comparing */
    boolean areEqual(T thisObject, T thatObject);
}
