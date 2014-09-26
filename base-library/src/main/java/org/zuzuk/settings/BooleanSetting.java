package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents boolean setting
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name) {
        super(name);
    }

    /* Returns boolean value of setting */
    public Boolean get(Context context) {
        CachedValue cachedValue = getCachedValue();
        if (cachedValue == null) {
            SharedPreferences preferences = getPreferences(context);
            Boolean value = preferences.contains(getName())
                    ? preferences.getBoolean(getName(), false)
                    : null;
            cachedValue = new CachedValue(value);
            setCachedValue(cachedValue);
        }
        return cachedValue.get();
    }

    /* Sets boolean value of setting */
    public void set(Context context, Boolean value) {
        if (Utils.objectsEquals(value, get(context))) {
            return;
        }

        if (value == null) {
            getPreferences(context).edit().remove(getName()).commit();
        } else {
            getPreferences(context).edit().putBoolean(getName(), value).apply();
        }
        setCachedValue(new CachedValue(value));
        raiseOnSettingChanged(context);
    }
}
