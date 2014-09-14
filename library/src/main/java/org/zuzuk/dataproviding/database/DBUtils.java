package org.zuzuk.dataproviding.database;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Some utils for OrmLite library
 */
public class DBUtils {

    /* Returns valid count of simple query records even if statement includes grouping */
    public static<T> long countOf(QueryBuilder<T, ?> queryBuilder, RuntimeExceptionDao<T, ?> dao) throws SQLException {
        return dao.queryRawValue("SELECT COUNT(*) FROM (" + queryBuilder.prepare().getStatement() + ")");
    }

    /* Returns valid count of where query records even if statement includes grouping */
    public static<T> long countOf(Where<T, ?> where, RuntimeExceptionDao<T, ?> dao) throws SQLException {
        return dao.queryRawValue("SELECT COUNT(*) FROM (" + where.prepare().getStatement() + ")");
    }
}
