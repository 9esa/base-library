package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Lc;
import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents boolean setting
 */
public class BooleanSetting extends Setting<Boolean> {
    private final Boolean defaultValue;
    private final ValueValidator<Boolean> valueValidator;

    public BooleanSetting(String name) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = null;
    }

    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = null;
    }

    public BooleanSetting(String name, ValueValidator<Boolean> valueValidator) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = valueValidator;
    }

    public BooleanSetting(String name, boolean defaultValue, ValueValidator<Boolean> valueValidator) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = valueValidator;
    }

    /* Returns boolean value of setting */
    public Boolean get(Context context) {
        synchronized (valueLocker) {
            CachedValue cachedValue = getCachedValue();
            if (cachedValue == null) {
                SharedPreferences preferences = getPreferences(context);
                Boolean value = preferences.contains(getName())
                        ? preferences.getBoolean(getName(), false)
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
    public boolean set(Context context, Boolean value) {
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
                    getPreferences(context).edit().putBoolean(getName(), defaultValue).commit();
                    value = defaultValue;
                } else {
                    getPreferences(context).edit().remove(getName()).commit();
                }
            } else {
                getPreferences(context).edit().putBoolean(getName(), value).commit();
            }
            setCachedValue(new CachedValue(value));
            raiseOnSettingChanged(context);
            return true;
        }
    }
}
