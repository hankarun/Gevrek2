package com.hankarun.gevrek.activities;

import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.hankarun.gevrek.R;
import com.hankarun.gevrek.fragments.MessagesFragment;
import com.hankarun.gevrek.fragments.ReadMessageFragment;

import java.util.ArrayList;

public class ReadMessageActivity extends AppCompatActivity implements View.OnClickListener{
    private Toolbar toolbar;
    public int link;
    public ArrayList<CharSequence> link_list;
    public ArrayList<CharSequence> header_list;
    public String groupname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);

        Bundle bundle = getIntent().getExtras();
        toolbar = (Toolbar) findViewById(R.id.toolbar12);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(bundle.getString("groupname"));

        groupname = bundle.getString("groupname");
        link = bundle.getInt("message");
        link_list = bundle.getCharSequenceArrayList("list");
        header_list = bundle.getCharSequenceArrayList("headers");

    }

    float x1,x2;
    float y1, y2;

    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                x1 = touchevent.getX();
                y1 = touchevent.getY();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = touchevent.getX();
                y2 = touchevent.getY();

                if (x1 < x2)
                {
                    goLeft();
                }

                if (x1 > x2)
                {
                    goRight();
                }

                break;
            }
        }

    return super.onTouchEvent(touchevent);
    }

    private void goLeft(){
        ReadMessageFragment readFrag = (ReadMessageFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment22);
        if (link > 0){
            link = link - 1;
            readFrag.loadMessage(link);
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.you_reached_first_message, Toast.LENGTH_SHORT).show();
        }
    }

    private void goRight(){
        ReadMessageFragment readFrag = (ReadMessageFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment22);
        if (link < link_list.size() - 1){
            link = link + 1;
            readFrag.loadMessage(link);
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.you_reached_last_message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewLeft:
                goLeft();
                break;
            case R.id.imageViewRight:
                goRight();
                break;
        }
    }

}
