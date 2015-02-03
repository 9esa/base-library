package org.zuzuk.tasks.remote.mycache;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class MyCacheDbEntry {
    @DatabaseField(id = true, canBeNull = false)
    private String key;

    @DatabaseField
    private long lastModified;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] data;

    public String getKey() {
        return key;
    }

    public long getLastModified() {
        return lastModified;
    }

    public byte[] getData() {
        return data;
    }

    private MyCacheDbEntry() {
    }

    public MyCacheDbEntry(Object key, byte[] data) {
        this.key = key.toString();
        this.lastModified = System.currentTimeMillis();
        this.data = data;
    }

}
