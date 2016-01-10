package com.example.livelyturtle.androidar;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

public class FullScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // fullscreen as in the bt200 technical info pdf - REMOVE BOTTOM BAR
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        //FLAG_SMARTFULLSCREEN is 0x80_00_00_00
        //winParams.flags |= WindowManager.LayoutParams.FLAG_SMARTFULLSCREEN;
        winParams.flags |= 0x80000000;
        win.setAttributes(winParams);

        // stackoverflow answer - REMOVE TOP BAR
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //set content view AFTER ABOVE sequence (to avoid crash)
        this.setContentView(new BasicView(this));


    }

}
