package com.hankarun.gevrek.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.hankarun.gevrek.R;
import com.hankarun.gevrek.helpers.NNTPHelper;
import com.hankarun.gevrek.helpers.SharedPrefHelper;
import com.hankarun.gevrek.interfaces.AsyncResponse;
import com.hankarun.gevrek.libs.StaticTexts;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AsyncResponse {

    Button mLoginButton;
    EditText mUsername;
    EditText mPassword;
    Dialog mDialog;

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

    private void setDialog(){
        mDialog = new Dialog(this);


        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDialog.setContentView(R.layout.custom_dialog);
        mDialog.setCancelable(false);

        final Window window = mDialog.getWindow();
        //window.setLayout(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    public void onClick(View v) {
        //Bekleme ekranı eklenecek

        //nntp ile kontrol edilecek.
        if(validate()) {
            setDialog();
            mDialog.show();
            NNTPHelper helper = new NNTPHelper(mUsername.getText().toString(), mPassword.getText().toString());
            helper.asyncResponse = this;
            helper.checkCreds();
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

                finish();
                //Kaybolacak
                mDialog.dismiss();
                break;
            case StaticTexts.FAIL:
                mDialog.dismiss();
                break;
        }

    }

    public boolean validate() {
        boolean valid = true;

        String email = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        if (email.isEmpty()) {
            mUsername.setError("Must Fill.");
            valid = false;
        } else {
            mUsername.setError(null);
        }

        if (password.isEmpty() ) {
            mPassword.setError("Must Fill.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }
}
