package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents integer setting
 */
public class IntSetting extends Setting<Integer> {

    public IntSetting(String name) {
        super(name);
    }

    /* Returns integer value of setting */
    public Integer get(Context context) {
        CachedValue cachedValue = getCachedValue();
        if (cachedValue == null) {
            SharedPreferences preferences = getPreferences(context);
            Integer value = preferences.contains(getName())
                    ? preferences.getInt(getName(), 0)
                    : null;
            cachedValue = new CachedValue(value);
            setCachedValue(cachedValue);
        }
        return cachedValue.get();
    }

    /* Sets integer value of setting */
    public void set(Context context, Integer value) {
        if (Utils.objectsEquals(value, get(context))) {
            return;
        }

        if (value == null) {
            getPreferences(context).edit().remove(getName()).commit();
        } else {
            getPreferences(context).edit().putInt(getName(), value).apply();
        }
        setCachedValue(new CachedValue(value));
        raiseOnSettingChanged(context);
    }
}
