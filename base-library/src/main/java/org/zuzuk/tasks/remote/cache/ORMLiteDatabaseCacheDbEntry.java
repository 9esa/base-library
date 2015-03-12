package org.zuzuk.tasks.remote.cache;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ORMLiteDatabaseCacheDbEntry {

    public static final String CLAZZ_COLUMN = "CLAZZ_COLUMN";

    @DatabaseField(id = true, canBeNull = false)
    private String key;

    @DatabaseField(index = true, canBeNull = false, columnName = CLAZZ_COLUMN)
    private String clazz;

    @DatabaseField(canBeNull = false)
    private long lastModified;

    @DatabaseField(dataType = DataType.BYTE_ARRAY, canBeNull = true)
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

    public ORMLiteDatabaseCacheDbEntry(Object key, Class clazz, byte[] data) {
        this.key = key.toString();
        this.clazz = clazz.getName();
        this.lastModified = System.currentTimeMillis();
        this.data = data;
    }

}
