package org.zuzuk.settings;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents string setting
 */
public class StringSetting extends Setting<String> {

    public StringSetting(String name) {
        super(name);
    }

    public StringSetting(String name, String defaultValue) {
        super(name, defaultValue);
    }

    public StringSetting(String name, ValueValidator<String> valueValidator) {
        super(name, valueValidator);
    }

    public StringSetting(String name, String defaultValue, ValueValidator<String> valueValidator) {
        super(name, defaultValue, valueValidator);
    }

    @Override
    protected String fromBytes(byte[] data) {
        return data != null ? new String(data) : null;
    }

    @Override
    protected byte[] toBytes(String value) {
        return value != null ? value.getBytes() : null;
    }
}