package com.hankarun.gevrek.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.fragments.CoursesFragment;
import com.hankarun.gevrek.fragments.EmailFragment;
import com.hankarun.gevrek.fragments.NewsGroupFragment;
import com.hankarun.gevrek.fragments.SettingsFragment;
import com.hankarun.gevrek.helpers.NNTPHelper;
import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.helpers.VolleyHelper;
import com.hankarun.gevrek.interfaces.AsyncResponse;
import com.hankarun.gevrek.interfaces.LoginDialogReturn;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;
import com.hankarun.gevrek.libs.VolleySingleton;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;


public class MainActivity extends AppCompatActivity implements AsyncResponse, LoginDialogReturn {

    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private VolleyHelper volleyHelper;
    public boolean checkUsername = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.mipmap.ic_action_search);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setHomeButtonEnabled(true);

        setupDrawerContent(nvDrawer);

        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.setDrawerListener(drawerToggle);
        TextView name = (TextView) findViewById(R.id.drawerHeaderUserName);
        if (name != null)
            name.setText(SharedPrefHelper.readPreferences(getApplicationContext(), StaticTexts.USER_REAL_NAME, ""));
        checkCreds();

        selectDrawerItem(nvDrawer.getMenu().getItem(0));

        if (SharedPrefHelper.readPreferences(this, StaticTexts.AVATAR_METHOD, "9").equals("9"))
            SharedPrefHelper.savePreferences(this, StaticTexts.AVATAR_METHOD, "0");
    }

    private void checkCreds() {
        //Hafızadan usename ve password al.
        String username = SharedPrefHelper.readPreferences(this, StaticTexts.SHARED_PREF_LOGINNAME, "");
        String password = SharedPrefHelper.readPreferences(this, StaticTexts.SHARED_PREF_PASSWORD, "");

        //Boş olup olmadıklarını kontrol et.

        if (username.equals("") || password.equals("")) {
            //Eğer boş ise dialog ekranını goster. ilk defa icin
            startLogin();
            //Bunun donuşunde http ile control et ve isim ile avatarı al
        } else {
            //Eğer boş değlse arka planda geçerlilikleri kontrol edilecek.
            testCredsWithNNTP(username, password);
        }

    }

    private void testCredsWithNNTP(String user, String password) {
        NNTPHelper helper = new NNTPHelper(user, password);
        helper.asyncResponse = this;
        helper.checkCreds();
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 9);
    }

    @Override
    public void onResponse(int feed) {
        switch (feed) {
            case StaticTexts.FAIL:
                startLogin();
                break;
            case StaticTexts.SUCCESS:
                loadusername();
                break;
        }
    }

    private void loadusername() {
        volleyHelper = new VolleyHelper(this);
        volleyHelper.postStringRequest(StaticTexts.REPLY_MESSAGE_GET, HttpPages.login_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Document doc = Jsoup.parse(response);
                    Element loginform = doc.getElementById("edit_auth");

                    Elements inputElements = loginform.select("tr");

                    TextView name = (TextView) findViewById(R.id.drawerHeaderUserName);
                    SharedPrefHelper.savePreferences(getApplicationContext(), StaticTexts.USER_REAL_NAME, inputElements.get(5).select("td").text());
                    name.setText(inputElements.get(5).select("td").text());

                    ImageView image = (ImageView) findViewById(R.id.drawerHeaderImage);

                    Log.d("image", inputElements.get(20).select("a").attr("abs:href"));

                    String username = SharedPrefHelper.readPreferences(getApplicationContext(), StaticTexts.SHARED_PREF_LOGINNAME, "");

                    TextView detail = (TextView) findViewById(R.id.draweDetailText);
                    detail.setText(username + "@ceng.metu.edu.tr");

                    Picasso.with(getApplicationContext())
                            .load("https://cow.ceng.metu.edu.tr/User/download_userPicture.php?username=" + username)
                                    //.placeholder(R.drawable.ic_file_big)
                                    //.error(R.drawable.ic_cloud_big)
                            .into(image);
                    //ImageLoader mImageLoader = VolleySingleton.getInstance().getImageLoader();
                    //image.setImageUrl(inputElements.get(20).select("a").attr("abs:href"),mImageLoader);
                }
            }
        });
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open, R.string.close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void selectDrawerItem(MenuItem menuItem) {

        Fragment fragment = null;
        String fragmentName = "";
        if (checkUsername) {
            checkUsername = false;
            checkCreds();
        }

        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = NewsGroupFragment.class;
                fragmentName = "News";
                break;
            case R.id.nav_second_fragment:
                fragmentClass = CoursesFragment.class;
                fragmentName = "Courses";
                break;
            case R.id.nav_third_fragment:
                fragmentClass = EmailFragment.class;
                fragmentName = "Email";
                break;
            case R.id.nav_settings:
                fragmentClass = SettingsFragment.class;
                fragmentName = "Settings";
                break;
            default:
                fragmentClass = NewsGroupFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container_body, fragment, fragmentName).commit();

        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            //Refresh fragment
            reloadNewsFragment();
        }
        /*if(requestCode==2) {
            //selectDrawerItem(nvDrawer.getMenu().getItem(1));
            //Show courses fragment
        }*/
    }

    private void reloadNewsFragment() {
        NewsGroupFragment tmp = (NewsGroupFragment) getSupportFragmentManager().findFragmentByTag("News");
        tmp.reload();
    }

    @Override
    public void dialogFinished() {
        loadusername();
        reloadNewsFragment();
    }

}
