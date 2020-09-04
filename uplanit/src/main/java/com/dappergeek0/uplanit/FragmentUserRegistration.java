package com.dappergeek0.uplanit;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.CursorLoader;
import android.content.Intent;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;


/**
 * Created by DapperGeek0 on 2/3/2017.
 */

public class FragmentUserRegistration extends Fragment implements LoaderCallbacks<Cursor>, View.OnClickListener{
   // Id to identity READ_CONTACTS permission request.
   private static final int REQUEST_READ_CONTACTS = 0;

   // Keep track of the login task to ensure we can cancel it if requested.
   private UserRegisterTask mAuthTask = null;

   // UI references.
   private AutoCompleteTextView mEmailView;
   private TextInputEditText mUsernameView, mPasswordView;
   private View mProgressView;
   private View mLoginFormView;
   private LoginButton fbLoginBtn;
   Button  mNewSignUpButton;
   TextView toLoginText, toBrandText, termsLink, privacyLink;
   private View rootView;

   //
   CallbackManager callbackManager;

   //
   SessionManager sessionManager;

   // Interface listener
   private FragmentLogin.OnLoginSuccessListener mListener;

   // Fragment creation interfaces
   Fragment newFragment;
   String fragmentTag;
   ButtonToLoginListener bListener;

   ContextWrapper contextWrapper;

//   StringHandler class
   StringHandler stringHandler = new StringHandler();

   // tmp hash map for single loginDetails
   HashMap<String, String> contact = new HashMap<>();

   public FragmentUserRegistration() {
      // Empty constructor required for fragment subclasses
   }

   public static FragmentUserRegistration newInstance() {
      return new FragmentUserRegistration();
   }

   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      try {
         bListener = (ButtonToLoginListener) getActivity();
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
      // Inflate the layout for this fragment
      rootView = inflater.inflate(R.layout.fragment_user_registration, container, false);
      //
      sessionManager = new SessionManager(getContext());

      //
      callbackManager = CallbackManager.Factory.create();

//      setupActionBar();
      // Set up the login form.
      mEmailView = (AutoCompleteTextView) rootView.findViewById(R.id.email);
      populateAutoComplete();

      mUsernameView = (TextInputEditText) rootView.findViewById(R.id.username);
      mPasswordView = (TextInputEditText) rootView.findViewById(R.id.password);
      toLoginText = (TextView) rootView.findViewById(R.id.to_login);
      toBrandText = (TextView) rootView.findViewById(R.id.to_brand_register);
      termsLink = (TextView) rootView.findViewById(R.id.terms_link);
      privacyLink = (TextView) rootView.findViewById(R.id.privacy_link);
      mNewSignUpButton = (Button) rootView.findViewById(R.id.new_sign_up_button);
      fbLoginBtn = (LoginButton) rootView.findViewById(R.id.fb_login_button);

      // onEditorListener
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

      // Hide the FloatingActionButton
      FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
      fab.setVisibility(View.GONE);

      // SetOnclickListeners on screen buttons
      toLoginText.setOnClickListener(this);
      toBrandText.setOnClickListener(this);
      termsLink.setOnClickListener(this);
      privacyLink.setOnClickListener(this);
      mNewSignUpButton.setOnClickListener(this);

      fbLoginBtn.setReadPermissions("email");
      // If using in a fragment
      fbLoginBtn.setFragment(this);

      // FBLogin callback
      fbLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
         @Override
         public void onSuccess(LoginResult loginResult) {
            GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {

                   @Override
                   public void onCompleted(JSONObject object, GraphResponse response) {
                      Log.v("Main", response.toString());

                      //
                      try {
                         String name = object.getString(Constants.nameString);
                         String email = object.getString(Constants.emailString);
                         String pixUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");

                         fbLoginBtn.setVisibility(View.GONE);

                         mAuthTask = new UserRegisterTask(Constants.FB_LOGIN, name, email, pixUrl);
                         mAuthTask.execute((String) null);
                      }
                      catch (Exception e){
                         e.printStackTrace();
                      }
                   }
                });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday,picture.type(normal)");
            request.setParameters(parameters);
            request.executeAsync();
         }

         @Override
         public void onCancel() {

         }

         @Override
         public void onError(FacebookException error) {

         }
      });

      /**
       * set the AppBar title
       */
      getActivity().setTitle(getString(R.string.title_activity_register));

      mLoginFormView = rootView.findViewById(R.id.login_form);
      mProgressView = rootView.findViewById(R.id.app_progress_bar);

      // Instantiate ContextWrapper class
      contextWrapper = new ContextWrapper(getContext());

      return rootView;
   }

   @Override
   public void onClick(View v){

      // ButtonToLogin onClick event
      if (v == toLoginText) {
         // Interface to open login fragment
         newFragment = new FragmentLogin();
         fragmentTag = Constants.loginFragTag;
         bListener.ButtonToLogin(newFragment, fragmentTag);
      }
      // ButtonToBrand onClick event
      if (v == toBrandText) {
         // Interface to open brand registration fragment
         newFragment = new FragmentBrandRegistration();
         fragmentTag = Constants.BRAND_REGISTRATION_FRAG_TAG;
         bListener.ButtonToLogin(newFragment, fragmentTag);
      }

      // newSignUpButton onClick event
      if (v == mNewSignUpButton) {
         attemptLogin();
      }

      // terms link action
      if (v == termsLink) {
         OpenWebLinks.openWebPage(getContext(),
                         Constants.termsUrl,
                         getResources().getString(R.string.usage_terms));
      }

      // privacy policy link action
      if (v == privacyLink) {
         OpenWebLinks.openWebPage(getContext(),
                         Constants.privacyUrl,
                         getResources().getString(R.string.privacy_string));
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      callbackManager.onActivityResult(requestCode, resultCode, data);
   }

   // Interface to show login screen on button click
   public interface ButtonToLoginListener {
      void ButtonToLogin(Fragment fragment, String fragmentTag);
   }

   // Populate email field with autoComplete suggestions from phone details
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
      if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
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
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
      String email = mEmailView.getText().toString().trim();
      String password = mPasswordView.getText().toString().trim();

      boolean cancel = false;
      View focusView = null;

      // Check for a valid username entry.
      if (TextUtils.isEmpty(username)) {
         mUsernameView.setError(getString(R.string.error_field_required));
         focusView = mUsernameView;
         cancel = true;
      }else if (!stringHandler.isUsernameValid(username)) {
         mUsernameView.setError(getString(R.string.error_invalid_username));
         focusView = mUsernameView;
         cancel = true;
      }

      // Check for a valid password, if the user entered one.
      if (TextUtils.isEmpty(password)) {
         mPasswordView.setError(getString(R.string.error_field_required));
         focusView = mPasswordView;
         cancel = true;
      }

      // Check for a valid password, if the user entered one.
      if (!stringHandler.isPasswordValid(password)) {
         mPasswordView.setError(getString(R.string.error_invalid_password));
         focusView = mPasswordView;
         cancel = true;
      }

      // Check for a valid email address.
      if (TextUtils.isEmpty(email)) {
         mEmailView.setError(getString(R.string.error_field_required));
         focusView = mEmailView;
         cancel = true;
      } else if (!stringHandler.isEmailValid(email)) {
         mEmailView.setError(getString(R.string.error_invalid_email));
         focusView = mEmailView;
         cancel = true;
      }

      if (cancel) {
         // There was an error; don't attempt login and focus the first
         // form field with an error.
         focusView.requestFocus();
      } else {

         // SHA1 hash the password
        String passHash = StringHandler.sha1Hash(password);

         // Initiate the user registration task
         mAuthTask = new UserRegisterTask(Constants.APP_LOGIN, username, email, passHash);
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
         mProgressView.setPadding(10,10,10,10);mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
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
   public class UserRegisterTask extends AsyncTask<String, String, HashMap<String, String>> {
      JSONParser jsonParser = new JSONParser();

//      private final String mEmail;
//      private final String mUsername;
//      private final String mPassword;

      private final String param1;
      private final String param2;
      private final String param3;
      private final String mLoginMode;

      UserRegisterTask(String mLoginMode, String param1, String param2, String param3) {
         this.mLoginMode = mLoginMode;
         this.param1 = param1;
         this.param2 = param2;
         this.param3 = param3;
      }

      @Override
      protected void onPreExecute(){
         // Show a progress spinner, and kick off a background task to
         // perform the user login attempt.
         showProgress(true);
      }

      @Override
      protected HashMap<String, String> doInBackground(String... args) {
         // get authentication against a network service.

         try {
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.LOGIN_MODE, mLoginMode);

            // parameters for native app login
            if (mLoginMode.equals(Constants.APP_LOGIN)){
               params.put(Constants.usernameString, param1);
               params.put(Constants.emailString, param2);
               params.put(Constants.passHashString, param3);
            }

            // post parameters for FB login
            else if (mLoginMode.equals(Constants.FB_LOGIN)){
               params.put(Constants.nameString, param1);
               params.put(Constants.emailString, param2);
            }

            JSONObject json = jsonParser.makeHttpRequest(
                Constants.userRegistrationUrl, Constants.parserPost, params);

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
                  String accountName = acc.getString(Constants.nameString);
                  String accountType = acc.getString(Constants.accTypeString);
                  String email = acc.getString(Constants.emailString);
                  String username = acc.getString(Constants.usernameString);
                  String passHash = acc.getString(Constants.passHashString);
                  String validKey = acc.getString(Constants.validKeyString);

                  /**
                   * Add general account details to HashMap
                   */
                  contact.put(Constants.IdString,id);
                  contact.put(Constants.usernameString, username);
                  contact.put(Constants.nameString,accountName);
                  contact.put(Constants.accTypeString,accountType);

                  /**
                   * save session data in SharedPreferences and login account
                   */
                  sessionManager.createLoginSession(
                      id,
                      accountName,
                      username,
                      email,
                      passHash,
                      validKey,
                      accountType,
                      true,
                      mLoginMode);

                  // set profile picture if FB login == true
                  if (mLoginMode.equals(Constants.FB_LOGIN)) {
                     sessionManager.setFbPicture(param3);
                  }

                  contact.put(Constants.IdString,id);
               }
               contact.put("auth_key",auth_key);
               return contact;
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
            //show error if both username & password have already been used
            if (showAuthKey.equals("BOTH_TAKEN")){
               mUsernameView.setError(getString(R.string.error_username_taken));
               mEmailView.setError(getString(R.string.error_email_taken));
               mEmailView.requestFocus();
               mUsernameView.requestFocus();
            }
            //show error if username has already been used
            if (showAuthKey.equals("USERNAME_TAKEN")){
               mUsernameView.setError(getString(R.string.error_username_taken));
               mUsernameView.requestFocus();
            }
            //show error if email has already been used
            if(showAuthKey.equals("EMAIL_TAKEN")){
               mEmailView.setError(getString(R.string.error_email_taken));
               mEmailView.requestFocus();
            }
            if(showAuthKey.equals("REGISTRATION_OK")){
               String displayName = details.get(Constants.nameString) != null
                   ? details.get(Constants.nameString)
                   : details.get(Constants.usernameString);

               String display = "Welcome "+ displayName + "\n";
               display += getString(R.string.registration_successful);

               mListener.OnLoginSuccess(Constants.patronString, details.toString(), true);
               /**
                * make toast indicating successful login
                */
               Toast.makeText(getContext(), display, Toast.LENGTH_SHORT).show();
            }
         }
         else {
            Toast.makeText(getContext(),getString(R.string.error_creating_account),
                Toast.LENGTH_SHORT).show();

            sessionManager.logoutFbUser(fbLoginBtn);
         }
      }

      @Override
      protected void onCancelled() {
         mAuthTask = null;
         showProgress(false);
      }
   }
}