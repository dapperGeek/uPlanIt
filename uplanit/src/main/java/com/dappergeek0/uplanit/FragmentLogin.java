package com.dappergeek0.uplanit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dappergeek0.uplanit.dialogs.DialogResetPassword;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

/* A login screen that offers login via username/password.
 */
public class FragmentLogin extends Fragment implements View.OnClickListener {

    /* Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    SessionManager sessionManager;
    Boolean completeProfile = true;
   String complete = String.valueOf(1);
   String logo;


   // classes
   StringHandler stringHandler = new StringHandler();
   ShowDialogFragmentClass showDialogFrag = new ShowDialogFragmentClass();

    Fragment newFragment;
    String fragmentTag;

    // UI views and components
    private TextInputEditText mUsernameView, mPasswordView;
    View mProgressView, mLoginView, rootView;
   FloatingActionButton fab;
    TextView buttonToRegister, passwordLink, termsLink, privacyLink;
    Button mUsernameSignInButton;
    LoginButton fbLoginBtn;

      // Facebook API CallbackManager
   CallbackManager callbackManager;

    // Interface listener
    private OnLoginSuccessListener mListener;

    // tmp hash map for single loginDetails
    HashMap<String, String> details = new HashMap<>();
    //temp HashMap for session details

    public FragmentLogin() {
        //Required empty constructor for fragment subclasses
    }

    public static FragmentLogin newInstance() {
        FragmentLogin fragmentLogin = new FragmentLogin();
        return fragmentLogin;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnLoginSuccessListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLoginSuccessListener");
        }

        try {
            bListener = (ButtonToRegisterListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ButtonToRegisterListener");
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
        rootView = inflater.inflate(R.layout.fragment_login, container, false);

       //
       callbackManager = CallbackManager.Factory.create();

       //Verify for a login session, if any , navigate away from the login screen
        sessionManager = new SessionManager(getContext());
        //Remember this is the FrameLayout area within your activity_main.xml
        FrameLayout contentFrameLayout = (FrameLayout) rootView.findViewById(R.id.content_frame);

       // assign views
       NavigationView nav_view = (NavigationView) rootView.findViewById(R.id.nav_view);
       fbLoginBtn = (LoginButton) rootView.findViewById(R.id.fb_login_button);

       mUsernameView = (TextInputEditText) rootView.findViewById(R.id.username);
       mPasswordView = (TextInputEditText) rootView.findViewById(R.id.password);
       //
       fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);

       buttonToRegister = (TextView) rootView.findViewById(R.id.button_toregister);
       passwordLink = (TextView) rootView.findViewById(R.id.pswd_help_link);
       termsLink = (TextView) rootView.findViewById(R.id.terms_link);
       privacyLink = (TextView) rootView.findViewById(R.id.privacy_link);
       mUsernameSignInButton = (Button) rootView.findViewById(R.id.sign_in_button);
        // Login input fields area
        mLoginView = rootView.findViewById(R.id.login_form);
        // Progress indicator
        mProgressView = rootView.findViewById(R.id.app_progress_bar);

        // Hide the FloatingActionButton
        fab.setVisibility(View.GONE);

        // Set up the login form.
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

       // Set Facebook login permissions
       fbLoginBtn.setReadPermissions("public_profile","email");
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
//                          String[] picture = url.split("\\?");

                          fbLoginBtn.setVisibility(View.GONE);

                          mAuthTask = new UserLoginTask(Constants.FB_LOGIN, name, email, pixUrl);
                          mAuthTask.execute();
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

       /*
       set the AppBar title
        */
        getActivity().setTitle(getString(R.string.title_activity_user_login));

       // Set onClickListener for buttons
        mUsernameSignInButton.setOnClickListener(this);
        buttonToRegister.setOnClickListener(this);
        passwordLink.setOnClickListener(this);
        termsLink.setOnClickListener(this);
        privacyLink.setOnClickListener(this);

        return rootView;
    }

   //
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)  {
      super.onActivityResult(requestCode, resultCode, data);
      callbackManager.onActivityResult(requestCode, resultCode, data);
   }

   //
   @Override
   public void onClick(View v){

      // switch the view id
      switch (v.getId()){

         // handle click on sign in button
         case R.id.sign_in_button:
            attemptLogin();
            break;

         // handle click on button to register screen
         case R.id.button_toregister:
            newFragment = new FragmentBrandRegistration();
            fragmentTag = Constants.patronRegistrationFragTag;

            bListener.ButtonToRegister(newFragment, fragmentTag);
            break;

         // handle click on the terms link text
         case R.id.terms_link:
            OpenWebLinks.openWebPage(getContext(),
                            Constants.termsUrl,
                            getResources().getString(R.string.usage_terms));
            break;

         // handle click on the privacy link text
         case R.id.privacy_link:
            OpenWebLinks.openWebPage(getContext(),
                Constants.privacyUrl,
                getResources().getString(R.string.privacy_string));
            break;

         // hand click on the password help
         case R.id.pswd_help_link:
            showDialog(DialogResetPassword.newInstance(), Constants.resetPswdFragTag);
            break;

      }
   }

   public void showDialog(DialogFragment fragment, String fragmentTag){

      // DialogFragment.show() will take care of adding the fragment
      // in a transaction.  We also want to remove any currently showing
      // dialog, so make our own transaction and take care of that here.
      FragmentTransaction ft = getFragmentManager().beginTransaction();
      Fragment prev = getFragmentManager().findFragmentByTag(fragmentTag);
      if (prev != null) {
         ft.remove(prev);
      }
      ft.addToBackStack(null);

      // Create and show the dialog.
      DialogFragment newFragment = fragment;
      newFragment.show(ft, fragmentTag);
   }

    /*
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);
        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!stringHandler.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

       if (!stringHandler.isUsernameValid(username)){
          mUsernameView.setError(getString(R.string.error_invalid_username));
          focusView = mUsernameView;
          cancel = true;
       }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

           // SHA1 hash the password
           String passHash = stringHandler.sha1Hash(password);

           // Initiate login task
            mAuthTask = new UserLoginTask(Constants.APP_LOGIN, username, passHash, null);
            mAuthTask.execute((String) null);
        }
    }

    /* Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, String, HashMap<String, String>> {

        JSONParser jsonParser = new JSONParser();

        private final String param1;
        private final String param2;
        private final String param3;
        private final String mLoginMode;
       String picture;

        UserLoginTask(String loginMode,String param1, String param2, String param3) {
           this.mLoginMode = loginMode;
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
                  params.put(Constants.passHashString, param2);
                  picture = "No pix";
               }

               // post parameters for FB login
               else {
                  params.put(Constants.nameString, param1);
                  params.put(Constants.emailString, param2);
                  picture = param3;
               }

                JSONObject json = jsonParser.makeHttpRequest(Constants.loginUrl, Constants.parserPost, params);

                if (json != null) {

                  //Get the authentication status or error message
                 JSONObject serverAuth = json.getJSONObject(Constants.serverAuthKey);
                 String auth_key = serverAuth.getString(Constants.authenticationKey);

                 //If authentication is OK get user account data
                 if(auth_key.equals(Constants.authenticationOk)){

                     //Get data from the user account
                     JSONObject result = json.getJSONObject(Constants.jsonResult);
                     JSONArray accounts = result.getJSONArray(Constants.jsonAccounts);
                     JSONObject acc = accounts.getJSONObject(0);

                     /* get username and other session data from JSON
                      */
                     String accountType = acc.getString(Constants.accTypeString);
                     String id = acc.getString(Constants.IdString);
                     String email = acc.getString(Constants.emailString);
                     String accountName = acc.getString(Constants.nameString);
                     String username = acc.getString(Constants.usernameString);
                     String passHash = acc.getString(Constants.passHashString);
                     String validKey = acc.getString(Constants.validKeyString);
                     /* Add general account details to HashMap
                      */
                     details.put(Constants.IdString,id);
                     details.put(Constants.usernameString, username);
                     details.put(Constants.nameString,accountName);
                     details.put(Constants.accTypeString,accountType);
                     details.put(Constants.pictureString, picture);

                     /* Add brand specific data to HashMap if login account is a brand account
                      */
                     if (accountType.equals(Constants.brandString)){

                        // Get brand specific details
                         logo = acc.getString(Constants.logoString);
                         String categoryId = acc.getString(Constants.categoryIdColumnString);
                        String rating = acc.getString(Constants.ratingString);
                        String vPhone = acc.getString(Constants.vPhone);
                        String vLocation = acc.getString(Constants.vLocation);
                        String phone = acc.getString(Constants.phoneString);
                        String address = acc.getString(Constants.addressString);
                        String description = acc.getString(Constants.descString);
                        complete = acc.getString(Constants.completeString);
                        Integer premium = acc.getInt(Constants.premiumString);

                        details.put(Constants.logoString,logo);
                        details.put(Constants.categoryId,categoryId);
                        details.put(Constants.ratingString,rating);

                        // Add brand specific session info to sharedPrefs
                        sessionManager.completeBrandInfo(categoryId,logo,address,phone,description,rating,vPhone,vLocation);

                        sessionManager.premiumStatus(premium);

                     }
                    details.put(Constants.completeProfileString,complete);
                     /* Save session data in SharedPreferences
                      */
                     completeProfile = Integer.parseInt(complete) != 0;

                    // create login session
                    sessionManager.createLoginSession(
                        id,
                        accountName,
                        username,
                        email,
                        passHash,
                        validKey,
                        accountType,
                        completeProfile,
                        mLoginMode);

                    // set profile picture if FB login == true
                    if (mLoginMode.equals(Constants.FB_LOGIN)) {
                       sessionManager.setFbPicture(param3);
                    }
                 }
                 details.put(Constants.authenticationKey, auth_key);
                 return details;
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
                String showAuthKey = details.get(Constants.authenticationKey);
                /* Take appropriate action depending on the authentication key from server
                 */
                switch (showAuthKey){
                    /* shows error for invalid username entry
                     */
                    case "INVALID_USER":
                        mUsernameView.setError(getString(R.string.error_invalid_username));
                        mUsernameView.requestFocus();
                        break;
                    /* shows error for incorrect password
                     */
                    case "INVALID_PASS":
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                        break;
                    /* Proceeds with login if username and password are correct
                     */
                    case Constants.authenticationOk:
                        // Get account type
                        String accountType = details.get(Constants.accTypeString);

                       String displayName = details.get(Constants.nameString) != null
                           ? details.get(Constants.nameString)
                           : details.get(Constants.usernameString);
                       String picture = details.get("picture");

                       // Toast indicating successful login
                       String display = "Welcome "+ displayName + "\n";
                       display += getString(R.string.successful_login) ;
                       display += !completeProfile ? "\n Complete registration to continue" : "";

                       Toast.makeText(getContext(),
                           display,Toast.LENGTH_LONG).show();

                        // Implement successful login interface
                        mListener.OnLoginSuccess(accountType, details.toString(), completeProfile);

                       // show the FloatingActionButton
                       fab.setVisibility(View.VISIBLE);
                }
            }
            else {
                /* Make toast indicating connection error
                 */
                Toast.makeText(getContext(),
                        getString(R.string.error_dataConnection),
                        Toast.LENGTH_SHORT).show();

               sessionManager.logoutFbUser(fbLoginBtn);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    // Container Activity must implement this interface
    public interface OnLoginSuccessListener {
        void OnLoginSuccess(String accountType, String extrasString, Boolean completeProfile);
    }

    ButtonToRegisterListener bListener;

    public interface ButtonToRegisterListener {
        void ButtonToRegister(Fragment fragment, String fragmentTag);
    }

    /* Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime);

            fbLoginBtn.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            fbLoginBtn.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}