package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents string setting
 */
public class StringSetting extends Setting<String> {

    public StringSetting(String name) {
        super(name);
    }

    /* Returns string value of setting */
    public String get(Context context) {
        CachedValue cachedValue = getCachedValue();
        if (cachedValue == null) {
            SharedPreferences preferences = getPreferences(context);
            String value = preferences.contains(getName())
                    ? preferences.getString(getName(), "")
                    : null;
            cachedValue = new CachedValue(value);
            setCachedValue(cachedValue);
        }
        return cachedValue.get();
    }

    /* Sets string value of setting */
    public void set(Context context, String value) {
        if (Utils.objectsEquals(value, get(context))) {
            return;
        }

        if (value == null) {
            getPreferences(context).edit().remove(getName()).commit();
        } else {
            getPreferences(context).edit().putString(getName(), value).apply();
        }
        setCachedValue(new CachedValue(value));
        raiseOnSettingChanged(context);
    }
}
