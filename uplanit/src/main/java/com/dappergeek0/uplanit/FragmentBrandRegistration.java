package com.dappergeek0.uplanit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created by DapperGeek0 on 2/4/2017.
 */

public class FragmentBrandRegistration extends Fragment implements LoaderCallbacks<Cursor>, View.OnClickListener {

   // Integer to identity READ_CONTACTS permission request.
   private static final int REQUEST_READ_CONTACTS = 0;

   // Keep track of the registration task to ensure we can cancel it if requested.
   private BrandRegisterTask mAuthTask = null;

   // UI references.
   private AutoCompleteTextView mEmailView;
   private TextInputEditText mUsernameView, mBrandNameView, mPasswordView;
   private View mProgressView;
   private View mLoginFormView;

   // Fragment creation interfaces
   Fragment newFragment;
   String fragmentTag;
   FragmentUserRegistration.ButtonToLoginListener bListener;
   FragmentLogin.OnLoginSuccessListener mListener;
   Boolean completeProfile = false;
   String accountType = Constants.brandString;

   // StringHandler Class
   StringHandler stringHandler = new StringHandler();

   // SessionManager class
   SessionManager sessionManager;

   // ContextWrapper class
   ContextWrapper contextWrapper;

   //FragmentUserRegistration activity class
   FragmentUserRegistration fragmentUserRegistration;

   // tmp hash map for single loginDetails
   HashMap<String, String> loginDetails = new HashMap<>();

   public FragmentBrandRegistration() {
      // Empty constructor required for fragment subclasses
   }

   public static FragmentBrandRegistration newInstance() {
      return new FragmentBrandRegistration();
   }

   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      try {
         bListener = (FragmentUserRegistration.ButtonToLoginListener) getActivity();
      } catch (ClassCastException e) {
         throw new ClassCastException(context.toString() + " must implement ButtonToLoginListener");
      }

      try {
         mListener = (FragmentLogin.OnLoginSuccessListener) getActivity();
      } catch (ClassCastException e) {
         throw new ClassCastException(context.toString() + " must implement OnLoginSuccessListener");
      }
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Nullable
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {

      View rootView = inflater.inflate(R.layout.fragment_brand_registration, container, false);

      //Initialize SessionManager class
      sessionManager = new SessionManager(getContext());

      // Initialize ContextWrapper class
      contextWrapper = new ContextWrapper(getContext());

//      setupActionBar();
      // Set up the login form.
      mEmailView = (AutoCompleteTextView) rootView.findViewById(R.id.email);
      populateAutoComplete();

      // set the AppBar title
      getActivity().setTitle(getString(R.string.title_register_brand));

      // Create instance of register activity
      fragmentUserRegistration = new FragmentUserRegistration();

      // assign views
      TextView toRegistration = (TextView) rootView.findViewById(R.id.to_user_registration);
      TextView toLogin = (TextView) rootView.findViewById(R.id.text_to_login);
      TextView tLink = (TextView) rootView.findViewById(R.id.terms_link);
      TextView pLink = (TextView) rootView.findViewById(R.id.privacy_link);
      mUsernameView = (TextInputEditText) rootView.findViewById(R.id.username);
      mBrandNameView = (TextInputEditText) rootView.findViewById(R.id.brand_name);
      mPasswordView = (TextInputEditText) rootView.findViewById(R.id.password);
      Button mNewSignUpButton = (Button) rootView.findViewById(R.id.new_sign_up_button);

      mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
            if (id == R.id.login || id == EditorInfo.IME_NULL) {
               attemptLogin();
               return true;
            }
            return false;
         }
      });

      // set on Click Listeners
      toRegistration.setOnClickListener(this);
      toLogin.setOnClickListener(this);
      tLink.setOnClickListener(this);
      pLink.setOnClickListener(this);
      mNewSignUpButton.setOnClickListener(this);

      // Hide the FloatingActionButton
      FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
      fab.setVisibility(View.GONE);

      mLoginFormView = rootView.findViewById(R.id.login_form);
      mProgressView = rootView.findViewById(R.id.app_progress_bar);
      return rootView;
   }

   @Override
   public void onClick(View v) {

      switch (v.getId()) {

         // handle click on to registration textView
         case R.id.to_user_registration:
            // Interface to open registration fragment
            newFragment = new FragmentUserRegistration();
            fragmentTag = Constants.patronRegistrationFragTag;
            bListener.ButtonToLogin(newFragment, fragmentTag);
            break;

         // handle to login click
         case R.id.text_to_login:
            // Interface to open login fragment
            newFragment = new FragmentLogin();
            fragmentTag = Constants.loginFragTag;
            bListener.ButtonToLogin(newFragment, fragmentTag);
            break;

         // terms of use click
         case R.id.terms_link:
            OpenWebLinks.openWebPage(getContext(),
                            Constants.termsUrl,
                            getResources().getString(R.string.usage_terms));
            break;

         // terms of use click
         case R.id.privacy_link:
            OpenWebLinks.openWebPage(getContext(),
                            Constants.privacyUrl,
                            getResources().getString(R.string.privacy_string));
            break;

         // handle form submit
         case R.id.new_sign_up_button:
            attemptLogin();
            break;
      }
   }

   private void populateAutoComplete() {
      if (!mayRequestContacts()) {
         return;
      }
      getActivity().getLoaderManager().initLoader(0, null, this);
   }

   private boolean mayRequestContacts() {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
         return true;
      }
      if (contextWrapper.checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
         return true;
      }
      if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
         Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
             .setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v) {
                   requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                }
             });
      } else {
         requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
      }
      return false;
   }

   /**
    * Callback received when a permissions request has been completed.
    */
   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
      if (requestCode == REQUEST_READ_CONTACTS) {
         if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            populateAutoComplete();
         }
      }
   }

   /**
    * Set up the {@link android.app.ActionBar}, if the API is available.
    */
   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   private void setupActionBar() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         // Show the Up button in the action bar.
         //  getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      }
   }

   /**
    * Attempts to sign in or register the account specified by the login form.
    * If there are form errors (invalid email, missing fields, etc.), the
    * errors are presented and no actual login attempt is made.
    */
   private void attemptLogin() {
      if (mAuthTask != null) {
         return;
      }

      // Reset errors.
      mEmailView.setError(null);
      mPasswordView.setError(null);

      // Store values at the time of the login attempt.
      String username = mUsernameView.getText().toString().trim();
      String brand_name = mBrandNameView.getText().toString().trim();
      String email = mEmailView.getText().toString().trim();
      String password = mPasswordView.getText().toString().trim();

      boolean cancel = false;
      View focusView = null;

      // Check for a valid password, if the user entered one.
      if (!stringHandler.isPasswordValid(password)) {
         mPasswordView.setError(getString(R.string.error_invalid_password));
         focusView = mPasswordView;
         cancel = true;
      }

      // Check for a valid brand name entry.
      if (TextUtils.isEmpty(brand_name)) {
         mBrandNameView.setError(getString(R.string.error_field_required));
         focusView = mBrandNameView;
         cancel = true;
      }

      if (!StringHandler.isBrandName(brand_name)) {
         mBrandNameView.setError(getString(R.string.error_invalid_brandName));
         focusView = mBrandNameView;
         cancel = true;
      }

      // Check for a empty username entry.
      if (TextUtils.isEmpty(username)) {
         mUsernameView.setError(getString(R.string.error_field_required));
         focusView = mUsernameView;
         cancel = true;
      }

      //  Check for valid username
      if (!stringHandler.isUsernameValid(username)){
         mUsernameView.setError(getString(R.string.error_invalid_username));
         focusView = mUsernameView;
         cancel = true;
      }

      // Check for a valid email address.
      if (TextUtils.isEmpty(email)) {
         mEmailView.setError(getString(R.string.error_field_required));
         focusView = mEmailView;
         cancel = true;
      }
      else if (!stringHandler.isEmailValid(email)) {
         mEmailView.setError(getString(R.string.error_invalid_email));
         focusView = mEmailView;
         cancel = true;
      }

      if (cancel) {
         // There was an error; don't attempt login and focus the first
         // form field with an error.
         focusView.requestFocus();
      } else {
         // Show a progress spinner, and kick off a background task to
         // perform the user login attempt.
         showProgress(true);

         // SHA1 hash the password
         String passHash = stringHandler.sha1Hash(password);

         // Initiate the user registration task
         mAuthTask = new BrandRegisterTask(brand_name, username, email, passHash);
         mAuthTask.execute((String) null);
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

         mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
         mLoginFormView.animate().setDuration(shortAnimTime).alpha(
             show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
               mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
         });

         mProgressView.setBackgroundColor(getResources().getColor(R.color.colorBackground));
         mProgressView.setPadding(10,10,10,10);
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
         mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
         mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
      }
   }

   @Override
   public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
      return new CursorLoader(getContext(),
          // Retrieve data rows for the device user's 'profile' loginDetails.
          Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
              ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

          // Select only email addresses.
          ContactsContract.Contacts.Data.MIMETYPE +
              " = ?", new String[]{ContactsContract.CommonDataKinds.Email
          .CONTENT_ITEM_TYPE},

          // Show primary email addresses first. Note that there won't be
          // a primary email address if the user hasn't specified one.
          ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
   }

   @Override
   public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
      List<String> emails = new ArrayList<>();
      cursor.moveToFirst();
      while (!cursor.isAfterLast()) {
         emails.add(cursor.getString(ProfileQuery.ADDRESS));
         cursor.moveToNext();
      }
      addEmailsToAutoComplete(emails);
   }

   @Override
   public void onLoaderReset(Loader<Cursor> cursorLoader) {

   }

   private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
      //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
      ArrayAdapter<String> adapter =
          new ArrayAdapter<>(getContext(),
              android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

      mEmailView.setAdapter(adapter);
   }

   private interface ProfileQuery {
      String[] PROJECTION = {
          ContactsContract.CommonDataKinds.Email.ADDRESS,
          ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
      };

      int ADDRESS = 0;
      int IS_PRIMARY = 1;
   }

   /**
    * Represents an asynchronous login/registration task used to authenticate
    * the user.
    */
   public class BrandRegisterTask extends AsyncTask<String, String, HashMap<String, String>> {
      JSONParser jsonParser = new JSONParser();

      private final String mBrandName;
      private final String mEmail;
      private final String mUsername;
      private final String mPassHash;

      BrandRegisterTask(String brandName, String username, String email, String passHash) {
         mBrandName = brandName;
         mUsername = username;
         mEmail = email;
         mPassHash = passHash;
      }

      @Override
      protected HashMap<String, String> doInBackground(String... args) {
         // get authentication against a network service.

         try {
            HashMap<String, String> params = new HashMap<>();
            params.put("brand_name", mBrandName);
            params.put("username", mUsername);
            params.put("email", mEmail);
            params.put("password", mPassHash);

            JSONObject json = jsonParser.makeHttpRequest(
                Constants.brandRegistrationUrl, "POST", params);

            if (json != null) {
               //Get the authentication status or error message
               JSONObject serverAuth = json.getJSONObject("serverAuth");
               String auth_key = serverAuth.getString("auth_key");
               //If authentication is OK get user account data
               if(auth_key.equals("REGISTRATION_OK")){
                 //Get data from the user account
                  JSONObject result = json.getJSONObject("result");
                  JSONArray accounts = result.getJSONArray("accounts");
                  JSONObject acc = accounts.getJSONObject(0);
                  /**
                   * get id and other session data from JSON
                   */
                  String id = acc.getString(Constants.IdString);
                  String name = acc.getString(Constants.nameString);
                  String validKey = acc.getString(Constants.validKeyString);
                  String accountType = acc.getString(Constants.accTypeString);

                  /**
                   * save session data in SharedPreferences and login account
                   */
                  sessionManager.createLoginSession(
                                     id,
                                     name,
                                     mUsername,
                                     mEmail,
                                     mPassHash,
                                     validKey,
                                     accountType,
                                     completeProfile,
                                     Constants.APP_LOGIN);
                  /**
                   * add details to brand details array
                   */
                  loginDetails.put(Constants.IdString,id);
                  loginDetails.put(Constants.usernameString,mUsername);
                  loginDetails.put(Constants.nameString,name);
                  loginDetails.put(Constants.accTypeString,accountType);
               }
               loginDetails.put("auth_key",auth_key);
               return loginDetails;
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(HashMap<String, String> details) {
         mAuthTask = null;
         showProgress(false);

         if (details != null) {
            String showAuthKey = details.get("auth_key");
            /**
             * Take appropriate action depending on the authentication key from server
             */
            switch (showAuthKey){
               /**
                * shows error when both email and username are taken
                */
               case "BOTH_TAKEN":
                  mUsernameView.setError(getString(R.string.error_username_taken));
                  mEmailView.setError(getString(R.string.error_email_taken));
                  mUsernameView.requestFocus();
                  break;
               /**
                * shows error when the username is taken
                */
               case "USERNAME_TAKEN":
                  mUsernameView.setError(getString(R.string.error_username_taken));
                  mUsernameView.requestFocus();
                  break;
               /**
                * shows error for email taken
                */
               case "EMAIL_TAKEN":
                  mEmailView.setError(getString(R.string.error_email_taken));
                  mEmailView.requestFocus();
                  break;
               /**
                * if registration is ok, proceed to next activity
                */
               case "REGISTRATION_OK":
                  String display = "Welcome "+mBrandName+"\n";
                  display += getString(R.string.brand_registration_continues);
                  /**
                   * Show Toast for successful registration
                   */
                  Toast.makeText(getContext(),
                      display,Toast.LENGTH_SHORT).show();

                  // Interface initialization
                  mListener.OnLoginSuccess(accountType, details.toString(), completeProfile);
            }
         }
         else {
            Toast.makeText(getContext(), getString(R.string.error_creating_account), Toast.LENGTH_SHORT).show();
         }
      }

      @Override
      protected void onCancelled() {
         mAuthTask = null;
         showProgress(false);
      }
   }

//   // Container Activity must implement this interface
//   public interface OnLoginSuccessListener {
//      void OnLoginSuccess(String accountType, String extrasString, Boolean completeProfile);
//   }
}