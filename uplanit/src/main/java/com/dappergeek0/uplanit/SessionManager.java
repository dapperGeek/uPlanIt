package com.dappergeek0.uplanit;

/**
 * Created by DapperGeek0 on 12/26/2016.
 * File: SessionManager Class
 * Usage: Handles all session
 */
import java.util.HashMap;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // All Shared Preferences Keys
    private static final String isLoginKey = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String nameKey = "name";

    //Brand Complete profile status key
    public static final String completeProfileKey = "completeProfile";

    // User name (make variable public to access from outside)
    public static final String usernameKey = "username";

    // User name (make variable public to access from outside)
    public static final String emailKey = "email";

    // User name (make variable public to access from outside)
    public static final String passHashKey = "passHash";

    // Valid Key
    public static final String keyValidKey = "validKey";

 // Logo Key
    public static final String logoKey = "logo";


 // fbPicture Key
    public static final String fbPicture = "fbPicture";

 // Logo Key
    public static final String categoryIdKey = "categoryId";

 // Phone Key
    public static final String phoneKey = "phone";

 // address Key
    public static final String addressKey = "address";

 // description Key
    public static final String descKey = "description";

 // description Key
    public static final String ratingKey = "rating";

 // description Key
    public static final String vPhoneKey = "vPhone";

 // description Key
    public static final String vLocationKey = "vLocation";

   // premium Key
    public static final String premiumKey = "premium";

 // gallery Key
    public static final String KEY_GALLERY = "gallery";

 // favorites Key
    public static final String favsKey = "favs";

    // Account ID
    public static final String IdKey = "id";

    // Account type
    public static final String accountType = "accountType";
    public static final String loginMode = "loginMode";

    // Account type
    public static final String KEY_HISTORY = "noHistory";

    //    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(Constants.myPrefsName, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(
                    String id,
                    String name,
                    String username,
                    String email,
                    String passHash,
                    String validKey,
                    String accountType,
                    Boolean completeProfile,
                    String loginMode){
        //saving user id in prefs
        editor.putBoolean(isLoginKey,true);
        editor.putBoolean(completeProfileKey,completeProfile);
        editor.putString(IdKey,id);
        editor.putString(nameKey, name.trim());
        editor.putString(usernameKey, username.trim());
        editor.putString(emailKey, email.trim());
        editor.putString(passHashKey, passHash.trim());
        editor.putString(keyValidKey, validKey.trim());
        editor.putString(SessionManager.accountType, accountType.trim());
        editor.putString(SessionManager.loginMode, loginMode.trim());
        editor.apply();
    }

   // update brand info on complete registration
   public void completeBrandInfo(
                     String categoryId,
                     String logo,
                     String address,
                     String mobile,
                     String description,
                     String rating,
                     String vPhone,
                     String vLocation){
            editor.putString(categoryIdKey, categoryId);
            editor.putString(logoKey, logo);
            editor.putString(phoneKey, mobile);
            editor.putString(addressKey, address);
            editor.putString(descKey, description);
            editor.putString(ratingKey, rating);
            editor.putString(vLocationKey, vLocation);
            editor.putString(vPhoneKey, vPhone);
            editor.apply();
   }

   // add featured status to brand login info
   public void premiumStatus(Integer premium){
         editor.putInt(premiumKey, premium);
         editor.apply();
   }

   // editing brand info
   public void updateBrandInfo(String name,
                               String phone,
                               String address,
                               String description){
      editor.putString(nameKey, name);
      editor.putString(phoneKey, phone);
      editor.putString(addressKey, address);
      editor.putString(descKey, description);
      editor.apply();
   }

   // set facebook profile picture url
   public void setFbPicture(String fbPix){
      editor.putString(fbPicture, fbPix);
      editor.apply();
   }

   // editing brand info
   public void updateLogo(String logo){
      editor.putString(logoKey, logo);
      editor.apply();
   }

    public void setBooleanPreference(String keyString, Boolean booleanValue){
        editor.putBoolean(keyString,booleanValue);
        editor.apply();
    }

   public void setPreference(String keyString, String value){
      editor.putString(keyString, value);
      editor.apply();
   }

   public void setPreference(String keyString, Integer value){
      editor.putInt(keyString, value);
      editor.apply();
   }

    //Get data value from sharedPreferences
    public String getPreference(String keyString){
        return pref.getString(keyString,null);
    }

    //Get data value from sharedPreferences
    public Integer getIntPreference(String keyString){
        return pref.getInt(keyString,0);
    }

   /**
    * remove an item from the sharedPreferences
    * @param keyString
    */
   public void removePreference(String keyString){
        editor.remove(keyString);
        editor.apply();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, FragmentLogin.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<>();
        // Session name
        user.put(nameKey, pref.getString(nameKey, null));

        // Session username
        user.put(usernameKey, pref.getString(usernameKey, null));

        // valid key
        user.put(keyValidKey, pref.getString(keyValidKey, null));

        // account ID
        user.put(IdKey, pref.getString(IdKey, null));

        // account type
        user.put(accountType, pref.getString(accountType, null));

        // return user
        return user;
    }

    // Clear session details
    public void logoutUser(){

        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

       logoutFbUser();

       // Make toast indicating log out
       Toast.makeText(_context,Constants.loggedOut,Toast.LENGTH_SHORT).show();
    }

   public void logoutFbUser(){
      FacebookSdk.sdkInitialize(_context);
      LoginManager.getInstance().logOut();
   }


    public void logoutFbUser(Button button){
      FacebookSdk.sdkInitialize(_context);
      LoginManager.getInstance().logOut();
      button.setVisibility(View.VISIBLE);
   }
    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(isLoginKey, false);
    }

    /**
     * Quick check for complete profile
     * **/
    // Get Login State
    public boolean isProfileComplete(){
        return pref.getBoolean(completeProfileKey, false);
    }
}