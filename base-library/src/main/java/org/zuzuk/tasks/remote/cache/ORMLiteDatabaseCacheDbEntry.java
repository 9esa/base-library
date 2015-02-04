package org.zuzuk.tasks.remote.cache;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ORMLiteDatabaseCacheDbEntry {

    @DatabaseField(id = true, canBeNull = false)
    private String key;

    @DatabaseField(canBeNull = false)
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

    private ORMLiteDatabaseCacheDbEntry() {
    }

    public ORMLiteDatabaseCacheDbEntry(Object key, byte[] data) {
        this.key = key.toString();
        this.lastModified = System.currentTimeMillis();
        this.data = data;
    }

}
