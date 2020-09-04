package com.dappergeek0.uplanit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

/**
 * Created by DapperGeek0 on 3/3/2017.
 */

public class ProgressAnimation {

   Context _context;
   View mProgressView;
   TextView mProgressString;

   public ProgressAnimation(Context context){
      this._context = context;
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
            int shortAnimTime = _context.getResources().getInteger(android.R.integer.config_shortAnimTime);

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
