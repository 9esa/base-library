package org.zuzuk.settings;

import java.nio.ByteBuffer;

/**
 * Created by Gavriil Sitnikov on 09/2014.
 * Class that represents long setting
 */
public class LongSetting extends Setting<Long> {

    public LongSetting(String name) {
        super(name);
    }

    public LongSetting(String name, Long defaultValue) {
        super(name, defaultValue);
    }

    public LongSetting(String name, ValueValidator<Long> valueValidator) {
        super(name, valueValidator);
    }

    public LongSetting(String name, Long defaultValue, ValueValidator<Long> valueValidator) {
        super(name, defaultValue, valueValidator);
    }

    @Override
    protected Long fromBytes(byte[] data) {
        return data != null ? ByteBuffer.wrap(data).getLong() : null;
    }

    @Override
    protected byte[] toBytes(Long value) {
        return value != null
                ? ByteBuffer.allocate(Long.SIZE / 8).putLong(value).array()
                : null;
    }
}