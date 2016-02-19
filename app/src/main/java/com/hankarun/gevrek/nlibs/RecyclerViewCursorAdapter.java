package com.hankarun.gevrek.nlibs;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private Cursor cursor;

    public void swapCursor(final Cursor cursor) {
        this.cursor = cursor;
        fillSections();
        this.notifyDataSetChanged();
    }

    public abstract void fillSections();

    @Override
    public int getItemCount() {
        return this.cursor != null
                ? this.cursor.getCount()
                : 0;
    }

    public Cursor getItem(final int position) {
        if (this.cursor != null && !this.cursor.isClosed()) {
            this.cursor.moveToPosition(position);
        }

        return this.cursor;
    }

    public Cursor getCursor() {
        return this.cursor;
    }

    @Override
    public final void onBindViewHolder(final VH holder, final int position) {
        final Cursor cursor = this.getItem(position);
        this.onBindViewHolder(holder, cursor, position);
    }

    public abstract void onBindViewHolder(final VH holder, final Cursor cursor, final int position);
}
