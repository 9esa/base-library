package org.zuzuk.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Base class that represents setting
 */
public abstract class Setting<T> {
    protected final static String SETTINGS_GROUP_NAME = "SETTINGS_GROUP";

    private final String name;
    private SharedPreferences preferences;
    private CachedValue cachedValue;

    /* Raises when value changes */
    public void raiseOnSettingChanged(Context context){
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(getName()));
    }

    /* Returns name of setting */
    public String getName() {
        return name;
    }

    /* Returns cached value of setting */
    protected CachedValue getCachedValue() {
        return cachedValue;
    }

    /* Sets string value of setting */
    protected void setCachedValue(CachedValue cachedValue) {
        this.cachedValue = cachedValue;
    }

    protected SharedPreferences getPreferences(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(SETTINGS_GROUP_NAME, Context.MODE_PRIVATE);
        }
        return preferences;
    }

    protected Setting(String name) {
        this.name = name;
    }

    /* helper class to store cached value */
    protected class CachedValue {
        private final T cachedValue;

        public T get() {
            return cachedValue;
        }

        public CachedValue(T cachedValue) {
            this.cachedValue = cachedValue;
        }
    }
}
