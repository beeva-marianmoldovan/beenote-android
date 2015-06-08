package com.beeva.beenote.viewutils;

/**
 * Created by marianclaudiu on 21/05/15.
 */
public interface OnRecyclerItemClick<T> {
    public void onItemClick(int position, T t);
    public void onItemLongClick(int position, T t);
}
