package com.hankarun.gevrek.helpers;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

public class WaitDialogHelper extends Dialog implements Dialog.OnKeyListener{
    Context context;
    public WaitDialogHelper(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public boolean onKey(DialogInterface arg0, int keyCode,
                         KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Cancel everything and return.
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

    }

    private void setDialog(){

    }
}
