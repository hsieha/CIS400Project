package com.example.livelyturtle.androidar;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

public class BlackScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_screen);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(new BasicView(this));

    }

}
