package com.hankarun.gevrek.nlibs;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hankarun.gevrek.R;
import com.hankarun.gevrek.database.NewsGroupTable;

import java.util.LinkedHashMap;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewsGroupListCursorAdapter extends RecyclerViewCursorAdapter<NewsGroupListCursorAdapter.GroupViewHolder>
        implements View.OnClickListener {

    private final LayoutInflater layoutInflater;
    private OnItemClickListener onItemClickListener;

    private LinkedHashMap<String, Integer> mMapIndex;


    public NewsGroupListCursorAdapter(final Context context) {
        super();
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void fillSections() {
        mMapIndex = new LinkedHashMap<>();

        for (int x = 0; x < getItemCount(); x++) {
            Cursor cursor = getItem(x);
            String group = cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_GROUP));
            if (!mMapIndex.containsKey(group)) {
                mMapIndex.put(group, x);
            }
        }

        Set<String> sectionLetters = mMapIndex.keySet();
    }


    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = this.layoutInflater.inflate(R.layout.newsgroup_listitem, parent, false);
        view.setOnClickListener(this);

        return new GroupViewHolder(view);
    }

    @Override
    public void onClick(final View view) {
        if (this.onItemClickListener != null) {
            final RecyclerView recyclerView = (RecyclerView) view.getParent();
            final int position = recyclerView.getChildLayoutPosition(view);
            if (position != RecyclerView.NO_POSITION) {
                final Cursor cursor = this.getItem(position);
                this.onItemClickListener.onItemClicked(cursor);
            }
        }
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, final Cursor cursor, final int position) {
        final String section_name = cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_GROUP));
        //holder.bindData(cursor,true);
        holder.bindData(cursor, mMapIndex.get(section_name) == position);
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.section_name)
        TextView mName;
        @Bind(R.id.section_title)
        TextView mSectionTitle;

        public GroupViewHolder(final View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bindData(final Cursor cursor, boolean bshowSection) {
            final String name = cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_NAME));
            final String color = cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_COLOR));
            final String count = cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_COUNT));

            final String section_name = cursor.getString(cursor.getColumnIndex(NewsGroupTable.NEWSGROUP_GROUP));

            this.mName.setText(Html.fromHtml(name + " <font color=\"" + color + "\">" + count + "</font>"));
            this.mSectionTitle.setText(section_name);
            this.mSectionTitle.setVisibility(bshowSection ? View.VISIBLE : View.GONE);
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(Cursor cursor);
    }
}
