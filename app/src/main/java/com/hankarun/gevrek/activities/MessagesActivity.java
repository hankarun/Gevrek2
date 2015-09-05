package com.hankarun.gevrek.activities;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.hankarun.gevrek.fragments.MessagesFragment;
import com.hankarun.gevrek.R;


public class MessagesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MessagesFragment articleFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        articleFrag = (MessagesFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);


        Intent iin = getIntent();
        Bundle b = iin.getExtras();
        articleFrag.link(b.getString("link"),b.getString("name"));
        getSupportActionBar().setTitle(b.getString("name"));
    }


    @Override
    public void onBackPressed() {
        // finish() is called in super: we only override this method to be able to override the transition
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out);
    }


    float x1,x2;
    @Override
    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {
                x1 = touchevent.getX();
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                x2 = touchevent.getX();

                if (x1 < x2)
                {
                    onBackPressed();
                }

                break;
            }
        }

        return super.onTouchEvent(touchevent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1) {
            //Refresh fragment
            articleFrag.loadPages();

        }
        if(requestCode==2) {
            //selectDrawerItem(nvDrawer.getMenu().getItem(1));
            //Show courses fragment

        }
    }
}
