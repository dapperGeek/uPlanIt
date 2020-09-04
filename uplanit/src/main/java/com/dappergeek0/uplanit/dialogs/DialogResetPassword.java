package com.dappergeek0.uplanit.dialogs;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.StringHandler;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DialogResetPassword#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogResetPassword extends DialogFragment implements View.OnClickListener {
   // TODO: Rename parameter arguments, choose names that match
   // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
   private static final String ARG_PARAM1 = "param1";
   private static final String ARG_PARAM2 = "param2";

   // declare views
   private TextInputEditText emailInput;
   private Button dismissBtn, sendBtn, closeBtn;
   private ProgressBar mProgressView;
   private RelativeLayout sentLayout, inputForm;
   private View rootView;

   // declare classes
   private ResetPasswordTask resetPasswordTask;
   private StringHandler stringHandler;


   public DialogResetPassword() {
      // Required empty public constructor
   }

   /**
    * Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
//    * @param param1 Parameter 1.
//    * @param param2 Parameter 2.
    * @return A new instance of fragment DialogResetPassword.
    */
   // TODO: Rename and change types and number of parameters
   public static DialogResetPassword newInstance() {
//      Bundle args = new Bundle();
//      args.putString(ARG_PARAM1, param1);
//      args.putString(ARG_PARAM2, param2);
//      fragment.setArguments(args);
      return  new DialogResetPassword();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (getArguments() != null) {
//         mParam1 = getArguments().getString(ARG_PARAM1);
//         mParam2 = getArguments().getString(ARG_PARAM2);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      rootView = inflater.inflate(R.layout.dialog_reset_password_layout, container, false);

      // assign classes
      stringHandler = new StringHandler();

      // assign views
      mProgressView = (ProgressBar) rootView.findViewById(R.id.dialog_progress_bar);
      emailInput = (TextInputEditText) rootView.findViewById(R.id.email_in);
      dismissBtn = (Button) rootView.findViewById(R.id.dismiss_btn);
      sendBtn = (Button) rootView.findViewById(R.id.submit_btn);
      closeBtn = (Button) rootView.findViewById(R.id.close_btn);
      sentLayout = (RelativeLayout) rootView.findViewById(R.id.sent_layout);
      inputForm = (RelativeLayout) rootView.findViewById(R.id.input_form);

      // change text on send button
      sendBtn.setText(getResources().getString(R.string.action_submit));

      // hide the sent message layout
      sentLayout.setVisibility(View.GONE);

      // set the title
      getDialog().setTitle(getResources().getString(R.string.prompt_reset_password));

      // set onClick Listeners
      dismissBtn.setOnClickListener(this);
      sendBtn.setOnClickListener(this);
      closeBtn.setOnClickListener(this);

      return rootView;
   }

   @Override
   public void onClick(View v){

      switch (v.getId()){

         // handle click on the dismiss button
         case R.id.dismiss_btn:
            dismiss();
            break;

         // handle close button
         case R.id.close_btn:
            dismiss();
            break;

         // handle click on the send button
         case R.id.submit_btn:
            attemptSubmit();
            break;
      }
   }

   // method checks input then sends email to server
   public void attemptSubmit(){
      //
      if (resetPasswordTask != null){
         return;
      }

      // reset errors
      emailInput.setError(null);

      // store email value at the time of send button click
      String email = emailInput.getText().toString().trim();

      //
      boolean cancel = false;
      View focusView = null;

      if (TextUtils.isEmpty(email)){
         emailInput.setError(getString(R.string.error_field_required));
         focusView = emailInput;
         cancel = true;
      }

      if (!stringHandler.isEmailValid(email)){
         emailInput.setError(getString(R.string.error_invalid_email));
         focusView = emailInput;
         cancel = true;
      }

      if (cancel){
         // There was an error; don't attempt password update and focus
         // form field with an error.
         focusView.requestFocus();
      }
      else {

         // send the email for password generation
         resetPasswordTask = new ResetPasswordTask(email);
         resetPasswordTask.execute();
      }

   }

   // AsyncTask to send the email to server for password generation
   public class ResetPasswordTask extends AsyncTask<String, String, String >{

      private final String email;
      private JSONParser jsonParser = new JSONParser();

      public ResetPasswordTask(String email){
         this.email = email;
      }

      @Override
      protected void onPreExecute(){
         showProgress(true);
         inputForm.setVisibility(View.GONE);
      }

      @Override
      protected String doInBackground(String... args){

         try {

            HashMap<String , String> params = new HashMap<>();

            params.put(Constants.emailString, email);
            params.put(Constants.modeString, "resetEmail");

            JSONObject json = jsonParser.makeHttpRequest(Constants.pswdResetUrl, Constants.parserPost, params);

            if (json != null) {

            }
         }
         catch (Exception e){
            e.printStackTrace();
         }

         return null;
      }

      @Override
      protected void onPostExecute(String string){
         // hide the progress animation
         showProgress(false);
         sentLayout.setVisibility(View.VISIBLE);

         if (string != null){

         }
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
         sendBtn.setEnabled(!show);
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
         sendBtn.setEnabled(!show);
         mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
      }
   }
}
