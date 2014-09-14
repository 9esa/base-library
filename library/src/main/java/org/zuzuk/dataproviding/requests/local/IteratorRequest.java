package org.zuzuk.dataproviding.requests.local;

import com.j256.ormlite.dao.CloseableIterator;

import org.zuzuk.dataproviding.requests.base.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Common request for batch of data from OrmLite iterator
 */
public class IteratorRequest<TItem> implements Task<List> {
    private final CloseableIterator<TItem> iterator;
    private final int currentPosition;
    private final int offset;
    private final int limit;

    public IteratorRequest(CloseableIterator<TItem> iterator, int currentPosition, int offset, int limit) {
        this.iterator = iterator;
        this.currentPosition = currentPosition;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public List<TItem> execute() throws Exception {
        TItem item = iterator.moveRelative(offset - currentPosition);
        if (item == null) {
            return new ArrayList<>(0);
        }

        List<TItem> result = new ArrayList<>(limit);
        while (result.size() < limit) {
            result.add(item);
            if (iterator.hasNext()) {
                iterator.moveToNext();
                item = iterator.current();
            } else {
                break;
            }
        }
        return result;
    }
}
