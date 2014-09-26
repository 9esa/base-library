package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents long setting
 */
public class LongSetting extends Setting<Long> {

    public LongSetting(String name) {
        super(name);
    }

    /* Returns long value of setting */
    public Long get(Context context) {
        CachedValue cachedValue = getCachedValue();
        if (cachedValue == null) {
            SharedPreferences preferences = getPreferences(context);
            Long value = preferences.contains(getName())
                    ? preferences.getLong(getName(), 0)
                    : null;
            cachedValue = new CachedValue(value);
            setCachedValue(cachedValue);
        }
        return cachedValue.get();
    }

    /* Sets long value of setting */
    public void set(Context context, Long value) {
        if (Utils.objectsEquals(value, get(context))) {
            return;
        }

        if (value == null) {
            getPreferences(context).edit().remove(getName()).commit();
        } else {
            getPreferences(context).edit().putLong(getName(), value).apply();
        }
        setCachedValue(new CachedValue(value));
        raiseOnSettingChanged(context);
    }
}
