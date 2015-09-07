package com.hankarun.gevrek.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.android.volley.Response;
import com.hankarun.gevrek.R;
import com.hankarun.gevrek.interfaces.LoginDialogReturn;
import com.hankarun.gevrek.libs.HttpPages;
import com.hankarun.gevrek.libs.StaticTexts;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PostDialogHelper extends Dialog implements
        android.view.View.OnClickListener {

    Button mSendButton;
    Button mCancelButton;
    EditText mSubject;
    EditText mBody;
    CheckBox mAddQuoteCheck;
    Context context;
    VolleyHelper volleyHelper;
    Activity activity;
    String quote;
    String newsgroups;
    String newsgroup;
    String references;
    int mDialogType;
    String from;
    public Dialog mDialog;

    public LoginDialogReturn answer;

    public PostDialogHelper(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        setContentView(R.layout.reply_dialog);


        mSendButton = (Button) findViewById(R.id.postButton);
        mSendButton.setOnClickListener(this);

        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(this);

        //Add check box on click listener.
        mAddQuoteCheck = (CheckBox) findViewById(R.id.quoteCheckBox);
        mAddQuoteCheck.setOnClickListener(this);

        //Define edit texts.
        mSubject = (EditText) findViewById(R.id.subjectText);
        mBody = (EditText) findViewById(R.id.bodyText);

        if(mDialogType == StaticTexts.REPLY_MESSAGE_DIALOG){
            mSubject.setEnabled(false);
        }else{
            mAddQuoteCheck.setVisibility(View.GONE);
        }

    }

    private void setDialog(){
        mDialog = new Dialog(context);


        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mDialog.setContentView(R.layout.custom_dialog);
        mDialog.setCancelable(false);

        final Window window = mDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        //window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.postButton:
                if(!mBody.getText().toString().isEmpty())
                    postMessage();
                //Burada toas gösterilecek.
                break;
            case R.id.cancelButton:
                dismiss();
                break;
            case R.id.quoteCheckBox:
                if(!mAddQuoteCheck.isChecked()){
                    mBody.setText(mBody.getText().toString().replace(quote, ""));
                }else{
                    mBody.append(quote);
                }
                break;
        }
    }

    public void dialogShow(Bundle b, Activity activity){
        this.activity = activity;
        mDialogType = b.getInt("type");
        //Get type of the dialog
        getReplyPage(b.getString("link"));
        setDialog();
        mDialog.show();
    }

    private void getReplyPage(String link){
        volleyHelper = new VolleyHelper(activity);
        volleyHelper.postStringRequest(StaticTexts.REPLY_MESSAGE_GET, link, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mDialog.dismiss();
                show();
                if (response != null) {
                    Document doc = Jsoup.parse(response);
                    Elements inputs = doc.select("input");
                    for (Element s : inputs) {
                        if (s.attr("name").equals("subject"))
                            mSubject.setText(s.attr("value"));
                        if (s.attr("name").equals("hide"))
                            quote = s.attr("value");
                        if (s.attr("name").equals("newsgroups"))
                            newsgroups = s.attr("value");
                        if (s.attr("name").equals("group"))
                            newsgroup = s.attr("value");
                        if (s.attr("name").equals("references"))
                            references = s.attr("value");
                        if(s.attr("name").equals("cc"))
                            from = s.attr("value");
                    }
                }

            }
        });
    }

    private void postMessage() {
        dismiss();
        mDialog.show();
        volleyHelper = new VolleyHelper(activity);
        volleyHelper.params.put("body", mBody.getText().toString());
        volleyHelper.params.put("references", references);
        volleyHelper.params.put("group", newsgroup);
        volleyHelper.params.put("newsgroups",newsgroups);
        volleyHelper.params.put("subject",mSubject.getText().toString());
        volleyHelper.params.put("cc",from);
        volleyHelper.params.put("type","post");
        volleyHelper.postStringRequest(StaticTexts.REPLY_MESSAGE_GET, HttpPages.post_page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mDialog.dismiss();
                answer.dialogFinished();
                //Gonderildi dismiss yap ve sayfayı yenile
            }
        });
    }

}
