package com.dappergeek0.uplanit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewActivity extends AppCompatActivity {

   ProgressBar mProgressView;
   TextView mProgressString;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_web_view);
      WebView webView;
      Toolbar toolbar;
      String openUrl = null;
      String title = null;

      try {
         openUrl = getIntent().getStringExtra(Constants.openUrl);
         title = getIntent().getStringExtra(Constants.urlTitle);
      }
      catch (NullPointerException e) {
         e.printStackTrace();
      }

      // set screen title
      setTitle(title);

      // assign views
      webView = (WebView) findViewById(R.id.activity_web_view);
      mProgressView = (ProgressBar) findViewById(R.id.app_progress_bar);
      mProgressString = (TextView) findViewById(R.id.progress_loading_string);

      webView.getSettings().setJavaScriptEnabled(true);
      webView.loadUrl(openUrl);

      toolbar = (Toolbar) findViewById(R.id.toolbar);
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      webView.setWebViewClient(new WebViewClient(){

         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
            showProgress(true);
         }

         @Override
         public void onPageFinished(WebView view, String url) {
            showProgress(false);
         }

         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url){
            view.loadUrl(url);
            return true;
         }
      });
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      switch (item.getItemId()){

         // handle clicking on the back button
         case android.R.id.home:
            finish();
            return true;

         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * Shows the progress UI and hides the login form.
    */
   @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
   public void showProgress(final boolean show) {
      // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
      // for very easy animations. If available, use these APIs to fade-in
      // the progress spinner.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
         try {
            int shortAnimTime = getApplicationContext().getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
               @Override
               public void onAnimationEnd(Animator animation) {
                  mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                  mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
               }
            });
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         // The ViewPropertyAnimator APIs are not available, so simply show
         // and hide the relevant UI components.
         mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
         mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
      }
   }
}
