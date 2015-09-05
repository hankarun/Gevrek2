package com.hankarun.gevrek.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.hankarun.gevrek.R;
import com.hankarun.gevrek.activities.CourseActivity;
import com.hankarun.gevrek.libs.CourseItem;

import java.util.ArrayList;

public class AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.ViewHolder> {

    private ArrayList<CourseItem> mDataset;
    private Context context;
    private int lastPosition = -1;
    private Activity activity;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtHeader;
        public TextView txtFooter;
        public CardView card;
        //public RelativeLayout card;

        public ViewHolder(View v) {
            super(v);
            txtHeader = (TextView) v.findViewById(R.id.courseShortName);
            txtFooter = (TextView) v.findViewById(R.id.courseLongName);
            card = (CardView) v.findViewById(R.id.card_view);
        }
    }

    public void add(int position, CourseItem item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(String item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterRecycler(ArrayList<CourseItem> myDataset, Context _context, Activity _activity) {
        mDataset = myDataset;
        context = _context;
        activity = _activity;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final String name = mDataset.get(position).shorName;
        final String lname = mDataset.get(position).longName;
        final String html = mDataset.get(position).hmtl;
        holder.txtHeader.setText(mDataset.get(position).longName);

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, CourseActivity.class);
                intent.putExtra("name",name);
                intent.putExtra("lname",lname);
                intent.putExtra("html",html);
                activity.startActivityForResult(intent,2);
                activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        holder.txtFooter.setText(name);
        setAnimation(holder.card, position);


    }

    @Override
    public AdapterRecycler.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_row, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}
