package org.zuzuk.settings;

import android.content.Context;
import android.content.SharedPreferences;

import org.zuzuk.utils.Lc;
import org.zuzuk.utils.Utils;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents string setting
 */
public class StringSetting extends Setting<String> {
    private final String defaultValue;
    private final ValueValidator<String> valueValidator;

    public StringSetting(String name) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = null;
    }

    public StringSetting(String name, String defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = null;
    }

    public StringSetting(String name, ValueValidator<String> valueValidator) {
        super(name);
        this.defaultValue = null;
        this.valueValidator = valueValidator;
    }

    public StringSetting(String name, String defaultValue, ValueValidator<String> valueValidator) {
        super(name);
        this.defaultValue = defaultValue;
        this.valueValidator = valueValidator;
    }

    /* Returns boolean value of setting */
    public String get(Context context) {
        synchronized (valueLocker) {
            CachedValue cachedValue = getCachedValue();
            if (cachedValue == null) {
                SharedPreferences preferences = getPreferences(context);
                String value = preferences.contains(getName())
                        ? preferences.getString(getName(), "")
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
    public boolean set(Context context, String value) {
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
                    getPreferences(context).edit().putString(getName(), defaultValue).commit();
                    value = defaultValue;
                } else {
                    getPreferences(context).edit().remove(getName()).commit();
                }
            } else {
                getPreferences(context).edit().putString(getName(), value).commit();
            }
            setCachedValue(new CachedValue(value));
            raiseOnSettingChanged(context);
            return true;
        }
    }
}