package com.hankarun.gevrek.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.hankarun.gevrek.MyApplication;
import com.hankarun.gevrek.NewsGroupIntentService;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.NNTPHelper;
import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.helpers.WaitDialogHelper;
import com.hankarun.gevrek.interfaces.AsyncResponse;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AsyncResponse {

    private Button mLoginButton;
    private EditText mUsername;
    private EditText mPassword;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);

        mLoginButton = (Button) findViewById(R.id.btn_login);

        mLoginButton.setOnClickListener(this);

        mUsername.setText(getUsername());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    private String getUsername(){
        return SharedPrefHelper.readPreferences(getApplicationContext(), StaticTexts.SHARED_PREF_LOGINNAME, "");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {
        if(validate()) {
            mDialog = new WaitDialogHelper(this);
            mDialog.show();
            new NNTPHelper(mUsername.getText().toString(), mPassword.getText().toString(),this).checkCreds();
        }
    }

    private void saveCreds(String _uname, String _pass){
        SharedPrefHelper.savePreferences(getApplicationContext(), StaticTexts.SHARED_PREF_LOGINNAME, _uname);
        SharedPrefHelper.savePreferences(getApplicationContext(), StaticTexts.SHARED_PREF_PASSWORD, _pass);
    }

    @Override
    public void onResponse(int feed) {
        switch (feed) {
            case StaticTexts.SUCCESS:
                //Bilgileri kaydedecek.
                saveCreds(mUsername.getText().toString(), mPassword.getText().toString());
                java.net.CookieManager msCookieManager = MyApplication.msCookieManager;
                msCookieManager.getCookieStore().removeAll();
                Intent mServiceIntent = new Intent(this, NewsGroupIntentService.class);
                mServiceIntent.setData(Uri.parse(HttpPages.courses_page));
                mServiceIntent.putExtra("type","0");
                startService(mServiceIntent);
                finish();
                //Kaybolacak
                mDialog.dismiss();
                break;
            case StaticTexts.FAIL:
                mDialog.dismiss();
                break;
        }

    }

    private boolean validate() {
        boolean valid = true;

        String email = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        if (email.isEmpty()) {
            mUsername.setError(getString(R.string.must_fill_error));
            valid = false;
        } else {
            mUsername.setError(null);
        }

        if (password.isEmpty() ) {
            mPassword.setError(getString(R.string.must_fill_error));
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }
}
