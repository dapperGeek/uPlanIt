package com.dappergeek0.uplanit.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.DatabaseHandler;
import com.dappergeek0.uplanit.FavoritesHandlerClass;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.SessionManager;
import com.dappergeek0.uplanit.StringHandler;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class BrandDetailsFragment extends Fragment implements View.OnClickListener{

   String  id;
   Context context;
   String brandId, brandName, phoneNumber, category,
         categoryId;
   int vPhone = 0;
   int vLocation = 0;
   int rating = 0;
   String[] categories;
   private TypedArray ratingStars;

   // SessionManager class
   SessionManager sessionManager;

   // FavoritesHandler class
   FavoritesHandlerClass favsHandlerClass;

   DatabaseHandler databaseHandler;

   Boolean favorite = false;

   // Views declared
   ImageView brandLogoView, ratingView;
   TextView displayCategory, displayMobile, displayAddress,
            displayDescription, verifiedStatus;
   private ImageView verifyPhone, verifyLocation;
   private RelativeLayout mobileLayout;
   private FloatingActionMenu menuPhoneNumber;
   private FloatingActionButton fabCall, fabSms, fabAdd, fabCopy;
   View rootView;

   private List<FloatingActionMenu> menus = new ArrayList<>();
   private Handler mUiHandler = new Handler();

   /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARGUMENT_EXTRAS = "extra_string";
    private String stringExtra;

    public BrandDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Declaration of new instance for BrandDetailsFragment
     */
    public static BrandDetailsFragment newInstance(int page, String title, String extra_string) {
        BrandDetailsFragment fragment = new BrandDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, page);
        args.putString(ARGUMENT_EXTRAS,extra_string);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_brand_details, container, false);

       context = getContext();
       sessionManager = new SessionManager(context);
       id = sessionManager.getPreference(SessionManager.IdKey);
       favsHandlerClass = new FavoritesHandlerClass(context);
       databaseHandler = new DatabaseHandler(context);

        //  Define layout elements
        brandLogoView = (ImageView) rootView.findViewById(R.id.brand_logo);
        ratingView = (ImageView) rootView.findViewById(R.id.rating_stars);
        verifyPhone = (ImageView) rootView.findViewById(R.id.verify_phone);
        verifyLocation = (ImageView) rootView.findViewById(R.id.verify_location);
        displayCategory = (TextView) rootView.findViewById(R.id.display_category);
        displayMobile = (TextView) rootView.findViewById(R.id.display_mobile);
        displayAddress = (TextView) rootView.findViewById(R.id.display_address);
        verifiedStatus = (TextView) rootView.findViewById(R.id.verified_status);
        displayDescription = (TextView) rootView.findViewById(R.id.display_description);
        mobileLayout = (RelativeLayout) rootView.findViewById(R.id.mobile_layout);

       // hide the phone number layout
       mobileLayout.setVisibility(View.GONE);

        //Get data from the intent EXTRA_MESSAGE
        stringExtra = getArguments().getString(ARGUMENT_EXTRAS);
        stringExtra = stringExtra.replaceAll("[{}]","");
        String[] keyValuePairs = stringExtra.split(",") ;
        Map<String,String> map = new HashMap<>();
        for(String pair : keyValuePairs){
            try {
                String[] entry = pair.split("=");
                map.put(entry[0].trim(),entry[1].trim());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

       // brand details
       brandId = map.get(Constants.IdString);
       brandName = map.get(Constants.brandNameString);
       categoryId = map.get(Constants.categoryId);

       try {
          vPhone = Integer.parseInt(map.get(Constants.vPhone));
          vLocation = Integer.parseInt(map.get(Constants.vLocation));
          rating = Integer.parseInt(map.get(Constants.ratingString));
       }catch (NumberFormatException e) {
          e.printStackTrace();
       }


       // assign resource arrays
       categories = getResources().getStringArray(R.array.categories);
       ratingStars = getActivity().getResources().obtainTypedArray(R.array.rating_stars);

       //set brand rating
       try {
          int ratingIcon = ratingStars.getResourceId(rating,-1);
          ratingView.setImageResource(ratingIcon);
       }catch (NumberFormatException e){
          e.printStackTrace();
       }

       // get the brand category
       category = StringHandler.brandCategory(categoryId, categories);

       // logo
        String brandLogo = Constants.LogoPath(map.get(Constants.logoString));
        Picasso.with(getContext())
            .load(brandLogo)
            .placeholder(R.drawable.ic_no_logo)
            .transform(new CropCircleTransformation())
            .into(brandLogoView);

       // set verifications
       setVerifications();

       //
       displayCategory.setText(category, null);
       phoneNumber = map.get(Constants.phoneString);
        displayMobile.setText(map.get(Constants.phoneString),null);
        displayMobile.setTypeface(null, Typeface.BOLD);
        displayAddress.setText(StringHandler.revertComma(map.get(Constants.addressString)),null);
        displayDescription.setText(StringHandler.revertComma(map.get(Constants.descString)),null);

       // set onClick Listeners
       displayMobile.setOnClickListener(this);

        // show options menu
        setHasOptionsMenu(true);
//       getActivity().invalidateOptionsMenu();

        return rootView;
    }

   @Override
   public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      menuPhoneNumber = (FloatingActionMenu) view.findViewById(R.id.menu_phone_number);

      fabCall = (FloatingActionButton) view.findViewById(R.id.fab_call);
      fabSms = (FloatingActionButton) view.findViewById(R.id.fab_sms);
      fabAdd = (FloatingActionButton) view.findViewById(R.id.fab_add);
      fabCopy = (FloatingActionButton) view.findViewById(R.id.fab_copy);

      menuPhoneNumber.setClosedOnTouchOutside(true);

      menuPhoneNumber.hideMenuButton(false);
   }


   @Override
   public void onActivityCreated(@Nullable Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      menus.add(menuPhoneNumber);

      fabCall.setOnClickListener(clickListener);
      fabCopy.setOnClickListener(clickListener);
      fabSms.setOnClickListener(clickListener);
      fabAdd.setOnClickListener(clickListener);

      int delay = 400;
      for (final FloatingActionMenu menu : menus) {
         mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               menu.showMenuButton(true);
            }
         }, delay);
         delay += 150;
      }

      menuPhoneNumber.setOnMenuButtonClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            // toggle phone menu icon
            menuPhoneNumber.toggle(true);
         }
      });

   }

   private View.OnClickListener clickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
         switch (v.getId()) {

            // call phone number option
            case R.id.fab_call:
               dialPhoneNumber(phoneNumber);
               break;

            // send sms option
            case R.id.fab_sms:
               sendSms(phoneNumber);
               break;

            // case copy number option
            case R.id.fab_copy:

               copyNumber(phoneNumber);
               break;

            // add new contact option
            case R.id.fab_add:

               // Creates a new Intent to insert a contact
               Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
               // Sets the MIME type to match the Contacts Provider
               intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
               // Inserts a phone number
               intent.putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber);
               // Sends the Intent
               startActivity(intent);
               break;
         }
      }
   };

    @Override
    public void onCreateOptionsMenu(
        Menu menu, MenuInflater menuInflater){
        menuInflater.inflate(R.menu.menu_activity_brand, menu);

       favorite = favsHandlerClass.isFavorite(brandId);

       MenuItem favorited = menu.findItem(R.id.remove_fav);
       MenuItem notFav = menu.findItem(R.id.add_fav);

       favorited.setVisible(favorite);
       notFav.setVisible(!favorite);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

       switch (item.getItemId()){

          //
          case R.id.add_fav:
             // add favorite to SQLite db
             favsHandlerClass.addFavorite(brandId);

             favorite = true;
             String toast = "Favourite added";
             Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
             getActivity().invalidateOptionsMenu();

             // if user is logged in, update server-side favorite
             if (sessionManager.isLoggedIn()){
                new serverFavoriteTask(brandId).execute();
             }
             return true;

          //
          case R.id.remove_fav:
             Toast.makeText(getContext(), "Favourite removed", Toast.LENGTH_SHORT).show();
             favsHandlerClass.removeFavorite(brandId);
             favorite = false;
             getActivity().invalidateOptionsMenu();
             return true;

          //
          case R.id.share_brand:
             shareBrand();
             return true;

          default:
             return super.onOptionsItemSelected(item);
       }
    }

   // share a brand
   public void shareBrand(){
      String shareMessage = StringHandler.shareBrandMessage(brandName, category);

      Intent shareBrand =  new Intent();
      shareBrand.setAction(Intent.ACTION_SEND);
      shareBrand.putExtra(Intent.EXTRA_TEXT, shareMessage);
      shareBrand.setType("text/plain");
      startActivity(shareBrand);
   }

   public class serverFavoriteTask extends AsyncTask<String, String , String > {

      private final String brandId;
      private final String type;
      private JSONParser jsonParser = new JSONParser();

      public serverFavoriteTask(String brandId){
         this.brandId = brandId;
         this.type = sessionManager.getPreference(SessionManager.accountType);

      }

      @Override
      protected String doInBackground(String ... args){

         try {
            HashMap<String , String> params = new HashMap<>();

            params.put(Constants.modeString, Constants.modeAdd);
            params.put(Constants.IdString, id);
            params.put(Constants.brandIdString, brandId);
            params.put(Constants.accTypeString, type);

            JSONObject json = jsonParser.makeHttpRequest(Constants.favoritesUrl, Constants.parserPost, params);

            if (json != null){
               JSONObject jResponse = json.getJSONObject(Constants.jsonResponse);
               String status = jResponse.getString(Constants.statusKey);

               if (status.equals(Constants.statusOk)){

               }
               return status;
            }
         }
         catch (Exception e){
            e.printStackTrace();
         }
         return null;
      }
//      @Override
//      protected void onPostExecute(String status){
////         String toast = null;
//
//         if (status != null){
//
//            switch (status){
//
//               //
//               case Constants.statusOk:
////                  toast = "synced";
//                  break;
//
//               //
//               case Constants.statusError:
////                  toast = "error adding favorite";
//                  break;
//            }
//         }
//         else {
////            toast = "unable to add favorite";
//         }
//
////         Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
//      }
   }

   // onclick actions
   @Override
   public void onClick(View v){

      switch (v.getId()){

         // handle click on the mobile number
         case R.id.display_mobile:
            dialPhoneNumber(phoneNumber);
            break;
      }
   }

   // intent to dial phone number
   public void dialPhoneNumber(String phoneNumber) {

      Intent intent = new Intent(Intent.ACTION_DIAL);
      intent.setData(Uri.parse("tel:" + phoneNumber));
      if (intent.resolveActivity(getContext().getPackageManager()) != null) {
         startActivity(intent);
      }
   }

   public void sendSms(String phoneNumber) {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null)));
   }

   public void copyNumber(String phoneNumber) {

      String copied = getResources().getString(R.string.prompt_phone_copied);
      // Gets a handle to the clipboard service.
      ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

      // Creates a new text clip to put on the clipboard
      ClipData clip = ClipData.newPlainText("simple text", phoneNumber);
      // Set the clipboard's primary clip.
      clipboard.setPrimaryClip(clip);
      Toast.makeText(getContext(), copied, Toast.LENGTH_SHORT).show();
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
