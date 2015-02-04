package org.zuzuk.settings;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Gavriil Sitnikov on 02/12/2014.
 * Inner class to store settings inside database
 */
@DatabaseTable
class SettingDatabaseModel {
    @DatabaseField(id = true, canBeNull = false)
    private String name;
    @DatabaseField(dataType = DataType.BYTE_ARRAY, canBeNull = false)
    private byte[] data;

    /* Returns name of setting */
    public String getName() {
        return name;
    }

    /* Returns byte array data of setting value */
    public byte[] getData() {
        return data;
    }

    private SettingDatabaseModel() {
    }

    public SettingDatabaseModel(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }
}
