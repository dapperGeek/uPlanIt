package com.dappergeek0.uplanit.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.SessionManager;
import com.dappergeek0.uplanit.StringHandler;

import org.json.JSONObject;

import java.util.HashMap;

/**
 */
public class FragmentChangePassword extends DialogFragment implements View.OnClickListener {

   // Views
   TextInputEditText currentPswdIn, newPswdIn, retypePswdIn;
   Button dismissBtn, sendButton;
   ProgressBar mProgressView;
   View dialogView;

   // SessionManager class
   SessionManager sessionManager;
   StringHandler stringHandler = new StringHandler();
   String passHash;

   UpdatePasswordTask updatePasswordTask;

   public FragmentChangePassword() {
      // Required empty public constructor
   }

   /**
    * Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
    * @return A new instance of fragment FragmentChangePassword.
    */
   public static FragmentChangePassword newInstance() {
      return new FragmentChangePassword();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (getArguments() != null) {
         //
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      dialogView = inflater.inflate(R.layout.fragment_change_password, container, false);

      sessionManager = new SessionManager(getContext());
      passHash = sessionManager.getPreference(SessionManager.passHashKey);

      // Declare Views
      mProgressView = (ProgressBar) dialogView.findViewById(R.id.dialog_progress_bar);
      currentPswdIn = (TextInputEditText) dialogView.findViewById(R.id.current_pswd_in);
      newPswdIn = (TextInputEditText) dialogView.findViewById(R.id.new_pswd_in);
      retypePswdIn = (TextInputEditText) dialogView.findViewById(R.id.retype_pswd_in);
      dismissBtn = (Button) dialogView.findViewById(R.id.dismiss_btn);
      sendButton = (Button) dialogView.findViewById(R.id.submit_btn);

      dismissBtn.setOnClickListener(this);
      sendButton.setOnClickListener(this);

      // set dialog title
      getDialog().setTitle(getString(R.string.change_pswd_text));

      return dialogView;
   }

   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
   }

   @Override
   public void onClick(View view){

      if (view == dismissBtn){
         dismiss();
      }

      if (view == sendButton){
         attemptUpdate();
      }
   }

   /** Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid username, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
       */
   private void attemptUpdate() {
      if (updatePasswordTask != null) {
         return;
      }

      // Reset errors.
      currentPswdIn.setError(null);
      newPswdIn.setError(null);
      retypePswdIn.setError(null);

      // Store values at the time of the login attempt.
      String currentPswd = currentPswdIn.getText().toString().trim();
      String newPswd = newPswdIn.getText().toString().trim();
      String retypedPswd = retypePswdIn.getText().toString().trim();

      // Hash password entries
      String currentHash = StringHandler.sha1Hash(currentPswd);
      String newHash = StringHandler.sha1Hash(newPswd);

      boolean cancel = false;
      View focusView = null;

      // Compare new and retyped passwords
      if (!newPswd.equals(retypedPswd)){
         retypePswdIn.setError(getString(R.string.error_unmatched_passwords));
         focusView = retypePswdIn;
         cancel = true;
      }

      // Compare the hashes of the currentPswd input with the actual session password
      if (!passHash.equals(currentHash)){
         currentPswdIn.setError(getString(R.string.error_incorrect_password));
         focusView = currentPswdIn;
         cancel = true;
      }

      // Check if new and password is valid
      if (!stringHandler.isPasswordValid(newPswd)){
         newPswdIn.setError(getString(R.string.error_invalid_password));
         focusView = newPswdIn;
         cancel = true;
      }

      // Check if retype password field is valid
      if (!stringHandler.isPasswordValid(retypedPswd)){
         retypePswdIn.setError(getString(R.string.error_invalid_password));
         focusView = retypePswdIn;
         cancel = true;
      }

      // Check if new and session password are same
      if (currentHash.equals(newHash)){
         newPswdIn.setError(getString(R.string.error_duplicate_password));
         focusView = newPswdIn;
         cancel = true;
      }

      //Check if the new password field is empty
      if (TextUtils.isEmpty(newPswd)){
         newPswdIn.setError(getString(R.string.error_field_required));
         focusView = newPswdIn;
         cancel = true;
      }

      //Check if  retype password field is empty
      if (TextUtils.isEmpty(retypedPswd)){
         retypePswdIn.setError(getString(R.string.error_field_required));
         focusView = retypePswdIn;
         cancel = true;
      }

      // Check for a valid password, if the user entered one.
      if (TextUtils.isEmpty(currentPswd)) {
         currentPswdIn.setError(getString(R.string.error_field_required));
         focusView = currentPswdIn;
         cancel = true;
      }

      if (cancel) {
         // There was an error; don't attempt password update and focus
         // form field with an error.
         focusView.requestFocus();
      } else {
         // Show a progress spinner, and kick off a background task to
         // perform the password update.
         showProgress(true);

         // Initiate password update task
         updatePasswordTask = new UpdatePasswordTask(sessionManager.getPreference(SessionManager.IdKey), newHash, sessionManager.getPreference(SessionManager.accountType));
         updatePasswordTask.execute((String) null);
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
         int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

         dismissBtn.setEnabled(!show);
         sendButton.setEnabled(!show);
         mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
         mProgressView.animate().setDuration(shortAnimTime).alpha(
             show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
               mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
         });
      } else {
         // The ViewPropertyAnimator APIs are not available, so simply show
         // and hide the relevant UI components.
         dismissBtn.setEnabled(!show);
         sendButton.setEnabled(!show);
         mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
      }
   }

   /**
    * AsyncTask to change the password locally and on server
    */
   public class UpdatePasswordTask extends AsyncTask<String , String , String >{

      //
      JSONParser jsonParser = new JSONParser();
      private final String mId;
      private final String mPassHash;
      private final String mType;

      public UpdatePasswordTask(String id, String passHash, String mType){
         this.mId = id;
         this.mPassHash = passHash;
         this.mType = mType;
      }

      @Override
      protected String doInBackground(String ... args){

         // get authentication against a network service.
         try {
            HashMap<String, String> params = new HashMap<>();
            params.put(SessionManager.IdKey, mId);
            params.put(SessionManager.passHashKey, mPassHash);
            params.put(SessionManager.accountType, mType);

            // Send parameters to server for response
            JSONObject json = jsonParser.makeHttpRequest(Constants.updatePswdUrl, Constants.parserPost, params);

            if (json != null) {

               String statusKey = json.getString(Constants.statusKey);

               if (statusKey.equals(Constants.statusOk))
               {
                  // Update hash in sharedPreferences
                  sessionManager.setPreference(SessionManager.passHashKey, mPassHash);
               }
               return statusKey;
            }
         }catch (Exception e){
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(String status){
         updatePasswordTask = null;
         showProgress(false);

         // Toast message
         String toast = null;

         if (status != null){
            switch (status){

               // Handle event for successful update
               case Constants.statusOk:
                  // Toast message
                  toast = getResources().getString(R.string.password_update_ok);

                  // close dialog
                  dismiss();
                  break;

               // Handle failed password update
               case Constants.statusError:
                  toast = getResources().getString(R.string.error_password_update);
                  break;
            }
         }

         else {
            // Data connection error when response is null
            toast = getResources().getString(R.string.error_dataConnection);
         }

         Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
      }
   }
}
