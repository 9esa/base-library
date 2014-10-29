package org.zuzuk.ui.adapters;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gavriil Sitnikov on 03/10/2014.
 * Base adapter for ViewPager
 */
public abstract class BasePagerAdapter extends PagerAdapter {
    private final List<View> viewCache = new ArrayList<>();

    /**
     * Create view for item here. If view is using in several adapters then create special
     * class that extends view
     */
    protected abstract View newView(ViewGroup container);

    /* Fills view with item data */
    protected abstract void bindView(View view, int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemPage;
        if (viewCache.isEmpty()) {
            itemPage = newView(container);
        } else {
            itemPage = viewCache.get(0);
            viewCache.remove(0);
        }
        container.addView(itemPage);

        bindView(itemPage, position);

        return itemPage;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        viewCache.add((View) object);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
