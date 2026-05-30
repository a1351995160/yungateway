package com.wps.yundoc.common.api;

import java.util.ArrayList;
import java.util.List;

/**
 * One-page result window fetched with one extra row to detect whether more data exists.
 *
 * @author wps
 */
public final class PageWindow<T> {

    private final List<T> items;
    private final boolean hasMore;

    private PageWindow(List<T> items, boolean hasMore) {
        this.items = items;
        this.hasMore = hasMore;
    }

    public static <T> PageWindow<T> fromFetched(List<T> fetched, int pageSize) {
        int itemCount = Math.min(fetched.size(), pageSize);
        return new PageWindow<>(
                new ArrayList<>(fetched.subList(0, itemCount)),
                fetched.size() > pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public boolean hasMore() {
        return hasMore;
    }
}
