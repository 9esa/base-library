package org.zuzuk.tasks.local;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.zuzuk.database.DBUtils;
import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Request that initialize OrmLite iterator (it is usually long because of count() method)
 */
public class IteratorInitializationRequest<TItem> extends Task<IteratorInitializationRequest.Response> {

    public static class Response<TItem> {
        private final Integer count;
        private final CloseableIterator<TItem> iterator;

        public Response(Integer count, CloseableIterator<TItem> iterator) {
            this.count = count;
            this.iterator = iterator;
        }

        /* Returns count of query records */
        public Integer getCount() {
            return count;
        }

        /* Returns OrmLite iterator of query records */
        public CloseableIterator<TItem> getIterator() {
            return iterator;
        }
    }

    private final RuntimeExceptionDao<TItem, ?> dao;
    private QueryBuilder<TItem, ?> queryBuilder;
    private Where<TItem, ?> where;
    private final boolean isKnownCount;

    public IteratorInitializationRequest(QueryBuilder<TItem, ?> queryBuilder,
                                         RuntimeExceptionDao<TItem, ?> dao,
                                         boolean isKnownCount) {
        super(IteratorInitializationRequest.Response.class);
        this.queryBuilder = queryBuilder;
        this.dao = dao;
        this.isKnownCount = isKnownCount;
    }

    public IteratorInitializationRequest(Where<TItem, ?> where,
                                         RuntimeExceptionDao<TItem, ?> dao,
                                         boolean isKnownCount) {
        super(IteratorInitializationRequest.Response.class);
        this.where = where;
        this.dao = dao;
        this.isKnownCount = isKnownCount;
    }

    @Override
    public Response<TItem> execute() throws Exception {
        Integer count = isKnownCount ? ((int) (queryBuilder != null ? DBUtils.countOf(queryBuilder, dao) : DBUtils.countOf(where, dao))) : null;
        return new Response<>(count, queryBuilder != null ? queryBuilder.iterator() : where.iterator());
    }
}