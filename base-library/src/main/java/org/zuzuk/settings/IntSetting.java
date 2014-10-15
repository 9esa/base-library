package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Lc;
import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents integer setting
 */
public class IntSetting extends Setting<Integer> {
    private final Integer defaultValue;
    private final ValueValidator<Integer> valueValidator;

    public IntSetting(String name) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = null;
    }

    public IntSetting(String name, int defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = null;
    }

    public IntSetting(String name, ValueValidator<Integer> valueValidator) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = valueValidator;
    }

    public IntSetting(String name, int defaultValue, ValueValidator<Integer> valueValidator) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = valueValidator;
    }

    /* Returns boolean value of setting */
    public Integer get(Context context) {
        synchronized (valueLocker) {
            CachedValue cachedValue = getCachedValue();
            if (cachedValue == null) {
                SharedPreferences preferences = getPreferences(context);
                Integer value = preferences.contains(getName())
                        ? preferences.getInt(getName(), 0)
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
    public boolean set(Context context, Integer value) {
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
                    getPreferences(context).edit().putInt(getName(), defaultValue).commit();
                } else {
                    getPreferences(context).edit().remove(getName()).commit();
                }
            } else {
                getPreferences(context).edit().putInt(getName(), value).commit();
            }
            setCachedValue(new CachedValue(value));
            raiseOnSettingChanged(context);
            return true;
        }
    }
}
