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
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.SessionManager;
import com.dappergeek0.uplanit.StringHandler;
import com.dappergeek0.uplanit.datetime_and_placepickers.CreateAutoCompleteTask;

import org.json.JSONObject;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentUpdateBrandInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUpdateBrandInfo extends DialogFragment implements View.OnClickListener {

   private String id;
   private String mParam2;

//   Views Declarations
   private View rootView;
   private TextInputEditText editBrandName, editPhone, editDescription;
   private AutoCompleteTextView editAddress;
   private Button button_submit, dismissBtn, sendButton;
   private ProgressBar autoProgress;
   private RelativeLayout spinnerView;
   private View updateFormView;
   private ProgressBar progressBar;
   private RelativeLayout logoView;

   private SessionManager sessionManager;

   private EditBrandTask editBrandTask = null;

   public ReloadBrandDetailsListener bListener;


   public FragmentUpdateBrandInfo() {
      // Required empty public constructor
   }

   /**
    * Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
    * @return A new instance of fragment FragmentUpdateBrandInfo.
    */
   // TODO: Rename and change types and number of parameters
   public static FragmentUpdateBrandInfo newInstance() {
      return new FragmentUpdateBrandInfo();
   }

   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      try {
         bListener = (ReloadBrandDetailsListener) getActivity();
      } catch (ClassCastException e) {
         throw new ClassCastException(context.toString() + " must implement ReloadBrandDetailsListener");
      }
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
//      if (getArguments() != null) {
//      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      rootView = inflater.inflate(R.layout.fragment_update_brand_info, container, false);

      sessionManager = new SessionManager(getContext());
      id = sessionManager.getPreference(Constants.IdString);

      //Initializing views
      logoView = (RelativeLayout) rootView.findViewById(R.id.brand_logo_view);
      spinnerView = (RelativeLayout) rootView.findViewById(R.id.spinner_view);
      editBrandName = (TextInputEditText) rootView.findViewById(R.id.input_brand_name);
      editPhone = (TextInputEditText) rootView.findViewById(R.id.input_brand_phone);
      editAddress = (AutoCompleteTextView) rootView.findViewById(R.id.location);
      editDescription = (TextInputEditText) rootView.findViewById(R.id.input_description);
      autoProgress = (ProgressBar) rootView.findViewById(R.id.place_autocomplete_progress);
      updateFormView = rootView.findViewById(R.id.update_form);
      progressBar = (ProgressBar) rootView.findViewById(R.id.dialog_progress_bar);
      button_submit = (Button) rootView.findViewById(R.id.button_submit);
      dismissBtn = (Button) rootView.findViewById(R.id.dismiss_btn);
      sendButton = (Button) rootView.findViewById(R.id.submit_btn);

      // show saved brand details in text input fields
      editBrandName.setText(sessionManager.getPreference(SessionManager.nameKey));
      editPhone.setText(sessionManager.getPreference(SessionManager.phoneKey));
      editAddress.setText(sessionManager.getPreference(SessionManager.addressKey));
      editDescription.setText(sessionManager.getPreference(SessionManager.descKey));

      // hide the logo upload and categories spinner
      logoView.setVisibility(View.GONE);
      spinnerView.setVisibility(View.GONE);

      // set buttons onClickListener
      dismissBtn.setOnClickListener(this);
      sendButton.setOnClickListener(this);

      // Set the Google places api query class on the location input
      editAddress.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            String sString = s.toString();
            if (sString.length() > 2){
               new CreateAutoCompleteTask(getContext(), sString, Constants.placesKey, editAddress, autoProgress).execute();
            }
         }

         @Override
         public void afterTextChanged(Editable s) {
         }
      });

      //Setting clickListener
      button_submit.setVisibility(View.GONE);

      // set dialog title
      getDialog().setTitle(getString(R.string.title_edit_brand));

      return rootView;
   }

   @Override
   public void onClick(View v){
      if (v == dismissBtn){
         dismiss();
      }

      if (v == sendButton){
         attemptEdit();
      }
   }

   /**
    * Attempts to update brand logo image and data .
    * If there are form errors (invalid email, missing fields, etc.), the
    * errors are presented and no actual login attempt is made.
    */
   private void attemptEdit() {
      if (editBrandTask != null) {
         return;
      }

      boolean cancel = false;
      View focusView = null;

      // Reset errors.
      editBrandName.setError(null);
      editPhone.setError(null);
      editAddress.setError(null);
      editDescription.setError(null);

      // Store values at the time of the login attempt.
      String name = editBrandName.getText().toString().trim();
      String phone = editPhone.getText().toString().trim();
      String address = editAddress.getText().toString().trim();
      String description = editDescription.getText().toString().trim();

      // validate brand name input
      if (TextUtils.isEmpty(name)) {
         editBrandName.setError(getString(R.string.error_field_required));
         focusView = editBrandName;
         cancel = true;
      }
      else if (!StringHandler.isBrandName(name)){
         editBrandName.setError(getString(R.string.error_invalid_brandName));
         focusView = editBrandName;
         cancel = true;
      }

       // check for valid and empty phone input
      if (TextUtils.isEmpty(phone)) {
         editPhone.setError(getString(R.string.error_field_required));
         focusView = editPhone;
         cancel = true;
      }
      else if (!StringHandler.isPhoneValid(phone)){
         editPhone.setError(getString(R.string.error_field_phone));
         focusView = editPhone;
         cancel = true;
      }

      // Validate address input
      if (TextUtils.isEmpty(address)) {
         editAddress.setError(getString(R.string.error_field_required));
         focusView = editAddress;
         cancel = true;
      }
      else if (!StringHandler.isValidTextInput(address)){
         editAddress.setError(getString(R.string.error_invalid_input_xters));
         focusView = editAddress;
         cancel = true;
      }

      // Validate description input
      if (TextUtils.isEmpty(description)) {
         editDescription.setError(getString(R.string.error_field_required));
         focusView = editDescription;
         cancel = true;
      }
      else if (!StringHandler.isValidTextInput(description)){
         editDescription.setError(getString(R.string.error_invalid_input_xters));
         focusView = editDescription;
         cancel = true;
      }

      if (cancel) {
         // There was an error; don't attempt update and focus the first
         // form field with an error.
         focusView.requestFocus();
      }
      else {
         editBrandTask = new EditBrandTask(
                               Constants.modeUpdate,
                               name,
                               phone,
                               address,
                               description);
         editBrandTask.execute((String) null);
      }
   }

   public class EditBrandTask extends AsyncTask<String, String, String>{

      JSONParser jsonParser = new JSONParser();

      private final String bId;
      private final String bMode;
      private final String bName;
      private final String bPhone;
      private final String bLocation;
      private final String bDesc;

      EditBrandTask(String mode,
                    String name,
                    String phone,
                    String location,
                    String description){
         this.bId = id;
         this.bMode = mode;
         this.bName = name;
         this.bPhone = phone;
         this.bLocation = location;
         this.bDesc = description;
      }

      @Override
      protected void onPreExecute(){
         // show progress animation
         showProgress(true);
      }

      @Override
      protected String doInBackground(String ... args){

         try {
            HashMap<String, String> params = new HashMap<>();

            params.put(Constants.IdString, bId);
            params.put(Constants.modeString, bMode);
            params.put(Constants.nameString, bName);
            params.put(Constants.phoneString, bPhone);
            params.put(Constants.locationString, bLocation);
            params.put(Constants.descString, bDesc);

            JSONObject jsonObject = jsonParser.makeHttpRequest(Constants.brandUpdateUrl, Constants.parserPost, params);

            if (jsonObject != null){

               // Get the status key from JSON response
               JSONObject json = jsonObject.getJSONObject(Constants.statusKey);
               String statusKey = json.getString(Constants.statusKey);

               // operations if statusKey is OK
               if (statusKey.equals(Constants.statusOk)){

                  // update brand details in shared preferences
                  sessionManager.updateBrandInfo(bName, bPhone, bLocation, bDesc);
               }

               return statusKey;
            }
         }
         catch (Exception e){
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(String status){
         // remove progress animation
         showProgress(false);
         String toast = null;

         if (status != null){

            switch (status){

               case Constants.statusOk:
                  // Toast message
                  toast = getResources().getString(R.string.info_update_ok);

                  //
                  bListener.ReloadBrandDetails();

                  // close dialog
                  dismiss();
                  break;

               //
               case Constants.statusError:
                  toast = getResources().getString(R.string.error_processing);
                  break;
            }
         }
         else {

            // Make toast indicating connection error
            toast = getResources().getString(R.string.error_dataConnection);
         }

         // Toast to indicate status
         Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
      }
   }

   public interface ReloadBrandDetailsListener {
      void ReloadBrandDetails();
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
         progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
         progressBar.animate().setDuration(shortAnimTime).alpha(
             show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
               progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
         });
      } else {
         // The ViewPropertyAnimator APIs are not available, so simply show
         // and hide the relevant UI components.
         dismissBtn.setEnabled(!show);
         sendButton.setEnabled(!show);
         progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
      }
   }
}
