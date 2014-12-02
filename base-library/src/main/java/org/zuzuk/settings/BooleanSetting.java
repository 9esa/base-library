package org.zuzuk.settings;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents boolean setting
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name) {
        super(name);
    }

    public BooleanSetting(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    public BooleanSetting(String name, ValueValidator<Boolean> valueValidator) {
        super(name, valueValidator);
    }

    public BooleanSetting(String name, Boolean defaultValue, ValueValidator<Boolean> valueValidator) {
        super(name, defaultValue, valueValidator);
    }

    @Override
    protected Boolean fromBytes(byte[] data) {
        return data != null ? data[0] == 1 : null;
    }

    @Override
    protected byte[] toBytes(Boolean value) {
        return value != null
                ? new byte[]{(byte) (value ? 1 : 0)}
                : null;
    }
}
