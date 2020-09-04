package com.dappergeek0.uplanit.fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dappergeek0.uplanit.ActivityVerificationsManager;
import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.MainActivity;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.SessionManager;
import com.dappergeek0.uplanit.StringHandler;
import com.dappergeek0.uplanit.dialogs.FragmentChangePassword;
import com.dappergeek0.uplanit.dialogs.FragmentUpdateBrandInfo;
import com.dappergeek0.uplanit.dialogs.FragmentUpdateLogo;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class BrandDashDetailsFragment extends Fragment{

    /*
     * The fragment argument representing the section number for this
     * fragment.
     */
    private String brandLogo, phone, address, category,
            categoryId, description;

    private View rootView;

    // classes
    SessionManager sessionManager;

   String[] categories;
   private TypedArray ratingStars;

    // UI views
   ImageView connErrorImage, brandLogoView, ratingView;
   private ImageView verifyPhone, verifyLocation;
   private TextView verifiedStatus;
   TextView displayCategory, displayMobile, displayAddress, displayDescription, connErrorText;
   private FloatingActionMenu menuPhoneNumber;

   ScrollView detailsView;
   int vPhone = 0;
   int vLocation = 0;
   int rating = 0;

    public BrandDashDetailsFragment() {
        // Required empty public constructor
    }

    /*
     * Declaration of new instance for BrandDetailsFragment
     */
    public static BrandDashDetailsFragment newInstance() {
        return new BrandDashDetailsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate UI layout
        rootView = inflater.inflate(R.layout.fragment_brand_details, container, false);

        sessionManager = new SessionManager(getContext());

        // Setup layout views
        connErrorImage = (ImageView) rootView.findViewById(R.id.conn_error_image);
        connErrorText = (TextView) rootView.findViewById(R.id.conn_error_text);
        detailsView = (ScrollView) rootView.findViewById(R.id.details_view);

        setHasOptionsMenu(true);//Set fragment options menu

        // Define layout elements
       displayCategory = (TextView) rootView.findViewById(R.id.display_category);
       brandLogoView = (ImageView) rootView.findViewById(R.id.brand_logo);
       ratingView = (ImageView) rootView.findViewById(R.id.rating_stars);
        displayMobile = (TextView) rootView.findViewById(R.id.display_mobile);
        displayAddress = (TextView) rootView.findViewById(R.id.display_address);
        displayDescription = (TextView) rootView.findViewById(R.id.display_description);
       menuPhoneNumber = (FloatingActionMenu) rootView.findViewById(R.id.menu_phone_number);
       verifyPhone = (ImageView) rootView.findViewById(R.id.verify_phone);
       verifyLocation = (ImageView) rootView.findViewById(R.id.verify_location);
       verifiedStatus = (TextView) rootView.findViewById(R.id.verified_status);

       brandLogo = Constants.LogoPath(sessionManager.getPreference(Constants.logoString));
       categoryId = sessionManager.getPreference(Constants.categoryId);
       phone = sessionManager.getPreference(Constants.phoneString);
       address = sessionManager.getPreference(Constants.addressString);
       description = sessionManager.getPreference(Constants.descString);

       // hide floating phone menu button
       menuPhoneNumber.setVisibility(View.GONE);

       try {
          vPhone = Integer.parseInt(sessionManager.getPreference(SessionManager.vPhoneKey));
          vLocation = Integer.parseInt(sessionManager.getPreference(SessionManager.vLocationKey));
          rating = Integer.parseInt(sessionManager.getPreference(SessionManager.ratingKey));
       }catch (NumberFormatException e) {
          e.printStackTrace();
       }

       // assign resource arrays
       categories = getResources().getStringArray(R.array.categories);
       ratingStars = getActivity().getResources().obtainTypedArray(R.array.rating_stars);

       //set brand rating
       int ratingIcon = ratingStars.getResourceId(rating,-1);
       ratingView.setImageResource(ratingIcon);

       // set verifications
      setVerifications();

       // get the brand category
       category = StringHandler.brandCategory(categoryId, categories);

       Picasso.with(getContext())
           .load(brandLogo)
           .placeholder(R.drawable.ic_no_logo)
           .transform(new CropCircleTransformation())
           .into(brandLogoView);

       displayCategory.setText(category, null);
       displayMobile.setText(phone);
       displayMobile.setTypeface(null, Typeface.BOLD);
       displayAddress.setText(address);
       displayDescription.setText(description);

        return rootView;//Show view
    }

    /*
     * Create ActionBar Menu for Fragment
     */
    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_brand_dash_details, menu);

       // menu items
       MenuItem manageVerifications = menu.findItem(R.id.v_manager);

       // show the verification option item if brand is not verified
       if (vLocation == 1 && vPhone == 1) {
          manageVerifications.setVisible(false);
       }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){

            case R.id.brand_logout:
                // Clearing all data from Shared Preferences
                sessionManager.logoutUser();
               Intent main = new Intent(getContext(), MainActivity.class);
               startActivity(main);
               return true;

               // Edit password click action
            case R.id.edit_password:
                showDialogFragment(FragmentChangePassword.newInstance(), Constants.changePswdFragTag);
               return true;

            // edit contact details click action
            case R.id.edit_contact:
               showDialogFragment(FragmentUpdateBrandInfo.newInstance(), Constants.updateBrandInfoTag);
               return true;

            // edit logo click option
           case R.id.edit_dp:
              showDialogFragment(new FragmentUpdateLogo(), Constants.updateBrandLogoTag);
              return true;

           // manage verifications option
           case R.id.v_manager:

              // open verifications manager activity
              Intent vManager = new Intent(getContext(), ActivityVerificationsManager.class);
              vManager.putExtra(Constants.vLocation, vLocation);
              vManager.putExtra(Constants.vPhone, vPhone);
              startActivity(vManager);

           default:
              return super.onOptionsItemSelected(item);
        }
    }

    public void showDialogFragment(DialogFragment dialogFragment, String fragmentTag ){
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
        dialogFragment.show(ft, fragmentTag);
    }

   public void setVerifications() {

      // show brand verifications
      if (vPhone == 1){
         verifyPhone.setImageResource(R.drawable.ic_phone_verified);
      }

      if (vLocation == 1){
         verifyLocation.setImageResource(R.drawable.ic_location_verified);
      }

      // if both phone and location has been verified
      if (vLocation == 1 && vPhone == 1){
         verifiedStatus.setText(R.string.brand_verified);
      }

      // if only phone has been verified
      if(vPhone == 1 && vLocation != 1) {
         verifiedStatus.setText(R.string.phone_verified);
      }

      // if only location has been
      else if(vLocation == 1 && vPhone != 1) {
         verifiedStatus.setText(R.string.location_verified);
      }
   }
}