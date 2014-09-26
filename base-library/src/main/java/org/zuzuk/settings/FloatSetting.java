package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents float setting
 */
public class FloatSetting extends Setting<Float> {

    public FloatSetting(String name) {
        super(name);
    }

    /* Returns float value of setting */
    public Float get(Context context) {
        CachedValue cachedValue = getCachedValue();
        if (cachedValue == null) {
            SharedPreferences preferences = getPreferences(context);
            Float value = preferences.contains(getName())
                    ? preferences.getFloat(getName(), 0f)
                    : null;
            cachedValue = new CachedValue(value);
            setCachedValue(cachedValue);
        }
        return cachedValue.get();
    }

    /* Sets float value of setting */
    public void set(Context context, Float value) {
        if (Utils.objectsEquals(value, get(context))) {
            return;
        }

        if (value == null) {
            getPreferences(context).edit().remove(getName()).commit();
        } else {
            getPreferences(context).edit().putFloat(getName(), value).apply();
        }
        setCachedValue(new CachedValue(value));
        raiseOnSettingChanged(context);
    }
}
