package com.example.newdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Jn on 10/01/16.
 */
public class HomeActivity extends Activity {

    private Button mBeginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);
        setContentView(R.layout.home);



        initComponents();
    }

    private void initComponents(){
        mBeginButton = (Button) findViewById(R.id.beginButton);
        mBeginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                letsBegin();
            }
        });
    }

    private void letsBegin(){
        Toast.makeText(this, "booting sequence..", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "setting up camera..", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(HomeActivity.this, MainActivity.class));
    }



}
