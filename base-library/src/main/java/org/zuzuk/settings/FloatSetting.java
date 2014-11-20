package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Lc;
import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents float setting
 */
public class FloatSetting extends Setting<Float> {
    private final Float defaultValue;
    private final ValueValidator<Float> valueValidator;

    public FloatSetting(String name) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = null;
    }

    public FloatSetting(String name, float defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = null;
    }

    public FloatSetting(String name, ValueValidator<Float> valueValidator) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = valueValidator;
    }

    public FloatSetting(String name, float defaultValue, ValueValidator<Float> valueValidator) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = valueValidator;
    }

    /* Returns boolean value of setting */
    public Float get(Context context) {
        synchronized (valueLocker) {
            CachedValue cachedValue = getCachedValue();
            if (cachedValue == null) {
                SharedPreferences preferences = getPreferences(context);
                Float value = preferences.contains(getName())
                        ? preferences.getFloat(getName(), 0)
                        : null;

                if (value == null && defaultValue != null) {
                    value = defaultValue;
                }

                cachedValue = new CachedValue(value);
                setCachedValue(cachedValue);
            }
            return cachedValue.get();
        }
    }

    /* Sets boolean value of setting */
    public boolean set(Context context, Float value) {
        synchronized (valueLocker) {
            if (Utils.objectsEquals(value, get(context))) {
                return true;
            }

            if (valueValidator != null && !valueValidator.isValid(value)) {
                Lc.e("Setting " + getName() + " tried to set with invalid value: "
                        + (value != null ? value : "null"));
                return false;
            }

            if (value == null) {
                if (defaultValue != null) {
                    getPreferences(context).edit().putFloat(getName(), defaultValue).commit();
                    value = defaultValue;
                } else {
                    getPreferences(context).edit().remove(getName()).commit();
                }
            } else {
                getPreferences(context).edit().putFloat(getName(), value).commit();
            }
            setCachedValue(new CachedValue(value));
            raiseOnSettingChanged(context);
            return true;
        }
    }
}