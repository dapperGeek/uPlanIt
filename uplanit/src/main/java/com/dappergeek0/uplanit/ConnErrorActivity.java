package com.dappergeek0.uplanit;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ConnErrorActivity extends MainActivity implements View.OnClickListener {

    Button buttonConnRetry;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remember this is the FrameLayout area within your activity_main.xml
        FrameLayout contentFrameLayout = (FrameLayout) findViewById(R.id.content_frame);
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        getLayoutInflater().inflate(R.layout.activity_conn_error, contentFrameLayout);

//        buttonConnRetry = (Button) findViewById(R.id.button_conn_retry);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setVisibility(View.GONE);
        buttonConnRetry.setOnClickListener(this);
        }

    private long mLastPress = 0;
    int TOAST_DURATION = 5000;
    Toast onBackPressedToast;

    @Override
    public void onBackPressed(){

        if (!CheckNetwork.isInternetAvailable(ConnErrorActivity.this)){
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastPress > TOAST_DURATION) {
                onBackPressedToast = Toast.makeText(this, R.string.prompt_to_exit, Toast.LENGTH_SHORT); onBackPressedToast.show();
                mLastPress = currentTime;
            }
            else {
                if (onBackPressedToast != null) {
                    onBackPressedToast.cancel();
                    //Difference with previous answer. Prevent continuing showing toast after application exit.
                    onBackPressedToast = null;
                    finish();
                }
                super.onBackPressed();
            }
        }
        else {
            toFeatured();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonConnRetry) {
            toFeatured();
        }
    }

    public void toFeatured(){
        Intent featured = new Intent(this, FragmentBrandsListing.class);
        featured.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        featured.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        featured.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(featured);
    }
}
