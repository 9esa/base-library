package org.zuzuk.dataproviding.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Helper class to simplify OrmLite integration into application
 */
public abstract class BaseOrmLiteHelper extends OrmLiteSqliteOpenHelper {

    /* Returns full column name for specific database class */
    public static String getTableColumnName(Class tableClass, String columnName) {
        DatabaseTable dbTable = (DatabaseTable) tableClass.getAnnotation(DatabaseTable.class);
        return dbTable.tableName() + "." + columnName;
    }

    private final HashMap<Class, RuntimeExceptionDao> daoMap = new HashMap<>();

    /* Returns classes that are part of database */
    protected abstract Class[] getTables();

    protected boolean shouldCreate() {
        return true;
    }

    public BaseOrmLiteHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public BaseOrmLiteHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion, File configFile) {
        super(context, databaseName, factory, databaseVersion, configFile);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        if (!shouldCreate()) {
            return;
        }

        try {
            for (Class table : getTables()) {
                TableUtils.createTableIfNotExists(connectionSource, table);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            for (Class table : getTables()) {
                TableUtils.dropTable(connectionSource, table, true);
            }
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Initializes helper */
    public void initialize() {
        for (Class table : getTables()) {
            daoMap.put(table, getRuntimeExceptionDao(table));
        }
    }

    @Override
    public void close() {
        super.close();
        daoMap.clear();
    }

    /* Returns data access object for special table */
    public <TDbTable, TId> RuntimeExceptionDao<TDbTable, TId> getDbTable(Class<TDbTable> clazz) {
        return daoMap.get(clazz);
    }

    /* Returns default access object */
    public RuntimeExceptionDao getDao() {
        return daoMap.get(getTables()[0]);
    }
}
