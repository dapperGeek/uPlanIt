package com.dappergeek0.uplanit;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dappergeek0.uplanit.dialogs.FragmentChangePassword;

import java.util.Objects;

public class FragmentUserDashboard extends Fragment implements View.OnClickListener{

   /**
     * Session manager class
     */
    SessionManager sessionManager;

    // Declare Views
    TextView welcomeText, dashEmail, dashFirstName, dashLastName, dashUsername;
    ImageView editPswdImg;
    RelativeLayout settingView, nameView, usernameView;
    View rootView;

   //
   String acctName, firstName, lastName, username, loginMode, title;
   String[] nameSplit;
    public FragmentUserDashboard(){
        // Empty constructor required for fragment subclasses
    }

    // New instance of fragment
    public static FragmentUserDashboard newInstance() {
        return new FragmentUserDashboard();
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
         rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
            sessionManager = new SessionManager(getContext());

            // Views assignments
            welcomeText = (TextView) rootView.findViewById(R.id.welcome_text);
            dashEmail = (TextView) rootView.findViewById(R.id.display_email);
            dashFirstName = (TextView) rootView.findViewById(R.id.display_fname);
            dashLastName = (TextView) rootView.findViewById(R.id.display_lname);
            dashUsername = (TextView) rootView.findViewById(R.id.display_username);
            editPswdImg = (ImageView) rootView.findViewById(R.id.edit_pswd_img);
            settingView = (RelativeLayout) rootView.findViewById(R.id.settings_view);
            nameView = (RelativeLayout) rootView.findViewById(R.id.name_view);
            usernameView = (RelativeLayout) rootView.findViewById(R.id.username_view);

            // set user email to textview
            dashEmail.setText(sessionManager.getPreference(SessionManager.emailKey));

            setHasOptionsMenu(true);

            showDetails();

            //Set the appbar title
            getActivity().setTitle(StringHandler.capFirst(title));

            //
            welcomeText.setText(getString(R.string.welcome_text) + " " + StringHandler.capFirst(title));

            // Set onClickListener for the edit password icon
            editPswdImg.setOnClickListener(this);

//       Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);

            return rootView;
    }

   public void showDetails(){

      loginMode = sessionManager.getPreference(SessionManager.loginMode);

      try {
         acctName = sessionManager.getPreference(SessionManager.nameKey);
         nameSplit = acctName.split("\\s");
         firstName = nameSplit[0];
         lastName = nameSplit[nameSplit.length - 1];
      }
      catch (Exception e){
         e.printStackTrace();
      }
      switch (loginMode){

         // Handle details displayed if login via native app login
         case Constants.APP_LOGIN:
            // show the username view
            showUsername();
            // display name if set
            if (!acctName.equals("")){
               showNames();
            }
            else {nameView.setVisibility(View.GONE);}
            break;

         // Handle details to display when login is via Facebook
         case Constants.FB_LOGIN:
            title = firstName;
            showNames();
            settingView.setVisibility(View.GONE);
            usernameView.setVisibility(View.GONE);
            break;
      }
   }

   void showNames(){

      dashFirstName.setText(firstName);
      dashLastName.setText(lastName);
   }

   void showUsername(){
      title = sessionManager.getPreference(SessionManager.usernameKey);
      dashUsername.setText(title);
   }

   @Override
   public void onClick(View view){

      if (view == editPswdImg){
         showPasswordDialog();
      }
   }

   public void showPasswordDialog(){

      // DialogFragment.show() will take care of adding the fragment
      // in a transaction.  We also want to remove any currently showing
      // dialog, so make our own transaction and take care of that here.
      FragmentTransaction ft = getFragmentManager().beginTransaction();
      Fragment prev = getFragmentManager().findFragmentByTag(Constants.changePswdFragTag);
      if (prev != null) {
         ft.remove(prev);
      }
      ft.addToBackStack(null);

      // Create and show the dialog.
      DialogFragment newFragment = FragmentChangePassword.newInstance();
      newFragment.show(ft, Constants.changePswdFragTag);
   }

   @Override
   public void onCreateOptionsMenu(
       Menu menu, MenuInflater inflater){
//      inflater.inflate(R.menu.menu_user_dashboard, menu);
   }
}

