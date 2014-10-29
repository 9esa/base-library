package org.zuzuk.ui.fragments;

import android.os.Bundle;

/**
 * Created by Gavriil Sitnikov on 13/10/2014.
 * Fragment that lives with activity till the end. Just attaching/detaching
 */
public interface StaticFragment {

    /* Apply some arguments to change state of fragment */
    void applyArguments(Bundle args);
}
