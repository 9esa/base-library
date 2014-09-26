package org.zuzuk.settings;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Object that checks value validation
 */
public interface ValueChecker<T> {

    /* Checks value validation */
    public boolean isValid(T value);
}
