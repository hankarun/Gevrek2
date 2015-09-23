package com.hankarun.gevrek.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
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

    private EditText mSubject;
    private EditText mBody;
    private CheckBox mAddQuoteCheck;
    private VolleyHelper volleyHelper;
    private Activity activity;
    private String quote;
    private String newsgroups;
    private String newsgroup;
    private String references;
    private int mDialogType;
    private String from;
    public Dialog mDialog;

    public LoginDialogReturn answer;

    public PostDialogHelper(Context context, final Activity a) {
        super(context);
        Context context1 = context;
        dialogHelper = new WaitDialogHelper(context);
        dialogHelper.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    a.finish();
                }
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        setContentView(R.layout.reply_dialog);


        Button mSendButton = (Button) findViewById(R.id.postButton);
        mSendButton.setOnClickListener(this);

        Button mCancelButton = (Button) findViewById(R.id.cancelButton);
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

    private final WaitDialogHelper dialogHelper;

    public void dialogShow(Bundle b, Activity activity){
        this.activity = activity;
        mDialogType = b.getInt("type");
        //Get type of the dialog
        getReplyPage(b.getString("link"));

        dialogHelper.show();
    }

    private void getReplyPage(String link){
        volleyHelper = new VolleyHelper(activity);
        volleyHelper.postStringRequest(StaticTexts.REPLY_MESSAGE_GET, link, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(dialogHelper!=null) {
                    dialogHelper.dismiss();
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
                            if (s.attr("name").equals("cc"))
                                from = s.attr("value");
                        }
                    }
                }

            }
        });
    }

    private void postMessage() {
        dismiss();
        dialogHelper.show();
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
                answer.dialogFinished();
                dialogHelper.dismiss();
                //Gonderildi dismiss yap ve sayfayı yenile
            }
        });
    }

}
