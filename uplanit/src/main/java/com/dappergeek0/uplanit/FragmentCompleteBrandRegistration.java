package com.dappergeek0.uplanit;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.dappergeek0.uplanit.datetime_and_placepickers.CreateAutoCompleteTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by DapperGeek0 on 2/3/2017.
 * File:
 * Usage:
 */

public class FragmentCompleteBrandRegistration extends Fragment implements View.OnClickListener  {

   // UI components declaration
   private ImageView brand_logo;
   private TextInputEditText editPhone, editDescription;
   AutoCompleteTextView editAddress;
   private Button button_submit;
   private View rootView;
   ProgressBar autoProgress;

   private String[] categories;

   // API Platform ContextWrapper class
   ContextWrapper contextWrapper;
   // SessionManager class
   SessionManager sessionManager;

   //
   CreateAutoCompleteTask mAutoTask;

   Boolean completeProfile = true;
   private String rating = "0";
   String accountType = Constants.brandString;

   // Interface listener
   FragmentLogin.OnLoginSuccessListener mListener;

   ArrayList<HashMap<String, String>> titlesArrayList = new ArrayList<>();//ArrayList for category titles
   ArrayList<HashMap<String, String>> idsArrayList = new ArrayList<>();//ArrayList for category IDs
   ArrayList<String> titlesArrayString = new ArrayList<>();
   Spinner spinner;

   private String category;
   private String catId;

   View updateFormView;
   View mProgressView;

   private String id, imgPath, encodedString;
   private int widthHeight = Constants.brandLogoDimensions;

   //storage permission code
   private static final int STORAGE_PERMISSION_CODE = 123;

   // Temporary null HashMap for brand server response
   HashMap<String, String> details = new HashMap<>();

   // Keep track of the login task to ensure we can cancel it if requested.
   private UploadMultipart mAuthTask = null;

   public FragmentCompleteBrandRegistration(){
      // Empty constructor required for subclasses
   }

   // Instantiate fragment new instance
   public static FragmentCompleteBrandRegistration newInstance(){
      return new FragmentCompleteBrandRegistration();
   }

   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
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
      //Initialize SessionManager class
      sessionManager = new SessionManager(getContext());

      // Initialize ContextWrapper class
      contextWrapper = new ContextWrapper(getContext());

      // Inflate the layout for this fragment
      rootView = inflater.inflate(R.layout.fragment_complete_brand_registration, container, false);

      id = sessionManager.getPreference(Constants.IdString);
      //Requesting storage permission
      requestStoragePermission();

      getActivity().setTitle(R.string.title_activity_brand_update_details);

      // Hide the FloatingActionButton
      FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
      fab.setVisibility(View.GONE);

      //Initializing views
      brand_logo = (ImageView) rootView.findViewById(R.id.brand_logo);
      editPhone = (TextInputEditText) rootView.findViewById(R.id.input_brand_phone);
      editAddress = (AutoCompleteTextView) rootView.findViewById(R.id.location);
      autoProgress = (ProgressBar) rootView.findViewById(R.id.place_autocomplete_progress);

      updateFormView = rootView.findViewById(R.id.update_form);
      mProgressView = rootView.findViewById(R.id.app_progress_bar);
      editDescription = (TextInputEditText) rootView.findViewById(R.id.input_description);
      button_submit = (Button) rootView.findViewById(R.id.button_submit);

      //Setting clickListener
      brand_logo.setOnClickListener(this);
      button_submit.setOnClickListener(this);

      spinner = (Spinner) rootView.findViewById(R.id.category_spinner);
      categories = getContext().getResources().getStringArray(R.array.categories);
      CreateSpinner();

      // Set the Google places api query class on the location input
      editAddress.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            String sString = s.toString();
            if (sString.length() > 2){
               mAutoTask = new CreateAutoCompleteTask(getContext(), sString, Constants.placesKey, editAddress, autoProgress);
               mAutoTask.execute();
            }
         }

         @Override
         public void afterTextChanged(Editable s) {
         }
      });
      return rootView;
   }

   // Handle buttons OnClick events
   @Override
   public void onClick(View v) {
      //action for click on the image chooser
      if (v == brand_logo) {
         showFileChooser();
      }
      //action for submit button click
      if (v == button_submit) {
         attemptUpload();
      }
   }

   //Requesting permission
   private void requestStoragePermission() {
      if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
         return;

      if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
         //If the user has denied the permission previously your code will come to this block
         //Here you can explain why you need this permission
         //Explain here why you need this permission
         Toast.makeText(getContext(),"Permission needed to select logo", Toast.LENGTH_SHORT).show();
      }
      //And finally ask for the permission
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
         ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
      }
   }

   //This method will be called when the user will tap on allow or deny
   @Override
   public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

      //Checking the request code of our request
      if (requestCode == STORAGE_PERMISSION_CODE) {

         //If permission is granted
         if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Displaying a toast
            Toast.makeText(getContext(), "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
         } else {
            //Displaying another toast if permission is not granted
            Toast.makeText(getContext(), "Oops you just denied the permission", Toast.LENGTH_LONG).show();
         }
      }
   }

   /**
    * Attempts to update brand logo image and data .
    * If there are form errors (invalid email, missing fields, etc.), the
    * errors are presented and no actual login attempt is made.
    */
   private void attemptUpload() {
      if (mAuthTask != null) {
         return;
      }

      boolean cancel = false;
      View focusView = null;

      // Reset errors.
      editPhone.setError(null);
      editAddress.setError(null);
      editDescription.setError(null);

      // When Image is selected from Gallery
      if (imgPath != null && !imgPath.isEmpty()) {
         // Convert image to String using Base64
         encodeImageToString();
      }

      // When Image is not selected from Gallery
      else {
         Toast.makeText(
             getContext(),
             "You must select image from gallery before you try to upload",
             Toast.LENGTH_SHORT).show();
         cancel = true;
      }
      // Store values at the time of the login attempt.
      String phone = editPhone.getText().toString().trim();
      String address = editAddress.getText().toString().trim();
      String description = editDescription.getText().toString().trim();

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

      // Check for valid address input
      if (!StringHandler.isValidTextInput(address)){
         editAddress.setError(getString(R.string.error_invalid_input_xters));
         focusView = editAddress;
         cancel = true;
      }

      // Check for valid description input
      if (!StringHandler.isValidTextInput(description)){
         editDescription.setError(getString(R.string.error_invalid_input_xters));
         focusView = editDescription;
         cancel = true;
      }

      if (cancel) {
         // There was an error; don't attempt update and focus the first
         // form field with an error.
            if (focusView != null) {
               focusView.requestFocus();
            }
      } else {
         showProgress(true);
         mAuthTask = new UploadMultipart(Constants.modeRegistration,encodedString,phone,address,description,catId);
         mAuthTask.execute((String) null);
      }
   }

   // Shows the progress UI and hides the login form.
   @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
   public void showProgress(final boolean show) {
      // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
      // for very easy animations. If available, use these APIs to fade-in
      // the progress spinner.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
         int shortAnimTime = getActivity().getResources().getInteger(android.R.integer.config_shortAnimTime);

         updateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
         updateFormView.animate().setDuration(shortAnimTime).alpha(
             show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
               updateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
         });

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
         updateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
      }
   }

   // Task to convert image to string
   public void encodeImageToString() {
      //Bitmap to get image from gallery
      Bitmap bitmap;

      // Process sampled down image using the same dimensions
      // for width and height values
      bitmap = decodeSampledBitmapFromFile(imgPath, widthHeight, widthHeight);

      if (bitmap != null) { // verify bitmap is not null
         ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
           // Must compress the Image to reduce image size to make upload easy
           bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
           byte[] byte_arr = stream.toByteArray();

           // Encode Image to String
           encodedString = Base64.encodeToString(byte_arr, 0);
        }
        catch (NullPointerException e) {
           e.printStackTrace();
        }
      }
      else { // show warning toast if bitmap is null
         Toast.makeText(getContext(), getString(R.string.warning_select_different_image), Toast.LENGTH_SHORT).show();
      }
   }

   /**
    * Method to show image chooser
    */
   private void showFileChooser() {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE_REQUEST);
   }

   /**
    * Handling the image chooser activity result
    * @param requestCode request code
    * @param resultCode result code
    * @param data data from image select activity
    */
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      try {// When an Image is picked

         if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == Constants.RESULT_OK
             && null != data) {

            // Get the image from data
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            // Get the cursor
            Cursor cursor = contextWrapper.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);

            // assert that cursor is not null
            if (cursor != null) {

               // Move to first row
               cursor.moveToFirst();

               int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
               imgPath = cursor.getString(columnIndex);
               cursor.close();
            }

            // Process sampled down image using the same dimensions for width and height values
            Bitmap bitmapImage = decodeSampledBitmapFromFile(imgPath,widthHeight,widthHeight);

            //Set the Image into ImageView
            if (bitmapImage != null) { // verify bitmap is not null
               brand_logo.setImageBitmap(bitmapImage);
            }
            else { // show warning toast if bitmap is null

               Toast.makeText(getContext(), getString(R.string.warning_select_different_image), Toast.LENGTH_SHORT).show();
            }
         }
         else { // show warning if no image is selected
            Toast.makeText(getContext(), "You haven't selected an image",
                Toast.LENGTH_SHORT).show();
         }
      } catch (Exception e) {
         Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT)
             .show();
      }
   }
   /**
    * Decode and sample down a bitmap from a file to the requested width and height.
    *
    * @param filename The full path of the file to decode
    * @param reqWidth The requested width of the resulting bitmap
    * @param reqHeight The requested height of the resulting bitmap
    * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
    *         that are equal to or greater than the requested width and height
    */
   public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                    int reqWidth, int reqHeight) {

      // First decode with inJustDecodeBounds = true to check dimensions
      final BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(filename, options);

      // Calculate inSampleSize
      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

      // Decode bitmap with inSampleSize set
      options.inJustDecodeBounds = false;
      return BitmapFactory.decodeFile(filename, options);
   }

   /**
    * calculate the sample size of image to load
    * @param options options
    * @param reqWidth width
    * @param reqHeight height
    * @return inSampleSize
    */
   public static int calculateInSampleSize(
       BitmapFactory.Options options, int reqWidth, int reqHeight) {
      // Raw height and width of image
      final int height = options.outHeight;
      final int width = options.outWidth;
      int inSampleSize = 1;

      if (height > reqHeight || width > reqWidth) {

         final int halfHeight = height / 2;
         final int halfWidth = width / 2;

         // Calculate the largest inSampleSize value that is a power of 2 and keeps both
         // height and width larger than the requested height and width.
         while ((halfHeight / inSampleSize) >= reqHeight
             && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2;
         }
      }

      return inSampleSize;
   }

   //method to get the file path from uri
   public String getPath(Uri uri) {
      Cursor cursor = contextWrapper.getContentResolver().query(uri, null, null, null, null);
      cursor.moveToFirst();
      String document_id = cursor.getString(0);
      document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
      cursor.close();

      cursor = contextWrapper.getContentResolver().query(
          android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
      cursor.moveToFirst();
      String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
      cursor.close();

      return path;
   }

   // Represents an asynchronous login/registration task used to authenticate
    // the user.
   public class UploadMultipart extends AsyncTask<String, String, HashMap<String, String>> {
      JSONParser jsonParser = new JSONParser();

      private final String uMode;
      private final String uImage;
      private final String uPhone;
      private final String uAddress;
      private final String uDescription;
      private final String uCategoryId;


      UploadMultipart(String mode,String image,String phone,String address,String description,String categoryId){
         uMode = mode;
         uImage = image;
         uPhone = phone;
         uAddress = address;
         uDescription = description;
         uCategoryId = categoryId;
      }

      @Override
      protected HashMap<String, String> doInBackground(String... args) {
         try {
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.modeString, uMode);
            params.put(Constants.IdString, id);
            params.put(Constants.categoryId, uCategoryId);
            params.put(Constants.IMAGE, uImage);
            params.put(Constants.phoneString, uPhone);
            params.put(Constants.addressString, uAddress);
            params.put(Constants.descString, uDescription);

            JSONObject json = jsonParser.makeHttpRequest(
                Constants.brandUpdateUrl, Constants.parserPost, params);

            if (json != null) {

               //Get the authentication status or error message
               JSONObject serverAuth = json.getJSONObject(Constants.statusKey);
               String statusKey = serverAuth.getString(Constants.statusKey);

               //If authentication is OK get user account data
               if(statusKey.equals(Constants.statusOk)){

                  //Get data from the user account
                  JSONObject result = json.getJSONObject(Constants.jsonResponse);
                  JSONArray accounts = result.getJSONArray(Constants.jsonAccounts);
                  JSONObject acc = accounts.getJSONObject(0);

                  // get logo url from JSON
                  String logo = acc.getString("file");
                  String vPhone = "0";
                  String vLocation = "0";

                  // add logo url and data to details HashMap
                  details.put(Constants.IdString, id);
                  details.put(Constants.logoString, logo);
                  details.put(Constants.phoneString, uPhone);
                  details.put(Constants.addressString, uAddress);
                  details.put(Constants.descString, uDescription);
                  details.put(Constants.ratingString, rating);

                  //Indicate profile complete status
                  sessionManager.setBooleanPreference(Constants.completeProfileString,completeProfile);

                  // Add brand specific session info to sharedPrefs
                  sessionManager.completeBrandInfo(uCategoryId, logo, uAddress, uPhone, uDescription, rating, vPhone, vLocation);

               }
               details.put(Constants.statusKey,statusKey);
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

         // Take appropriate actions if details HashMap is not null
         if(details != null){
            String statusKey = details.get(Constants.statusKey);

            //action depending on result status
            switch (statusKey){

               //shows error message when upload error occurs
               case Constants.statusError:
                  Toast.makeText(getContext(), getString(R.string.error_processing), Toast.LENGTH_SHORT).show();
                  break;

               // Proceeds to next activity on successful upload and details update
               case Constants.statusOk:

                  // Interface initialization
                  mListener.OnLoginSuccess(accountType, details.toString(), completeProfile);
                  break;
            }
         }
         else{

            // Make toast indicating connection error
            Toast.makeText(getContext(),
                getString(R.string.error_dataConnection),
                Toast.LENGTH_SHORT).show();
         }
      }
   }

   // Spinner to show all app categories
   public void CreateSpinner(){

      for (String str : categories) {
         //HashMap to save category titles
         HashMap<String, String> titleHashMap = new HashMap<>();
         //HashMap to store category IDs
         HashMap<String, String> idHashMap = new HashMap<>();
         String[] splits = str.split("=");
         String cId = splits[0];
         String cTitle = splits[1];
         titleHashMap.put(Constants.categoryString, cTitle);
         idHashMap.put(Constants.IdString, cId);
         titlesArrayList.add(titleHashMap);
         idsArrayList.add(idHashMap);
         titlesArrayString.add(cTitle);
      }

      // Create an ArrayAdapter using the string array and a default spinner layout
      ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.spinner_style,titlesArrayString);
      // Specify the layout to use when the list of choices appears
      adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
      // Apply the adapter to the spinner
      spinner.setAdapter(adapter);

      spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
         @Override
         public void onItemSelected(AdapterView<?> parent, View view,
                                    int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            category = titlesArrayList.get(pos).get(Constants.categoryString);
            catId = idsArrayList.get(pos).get(Constants.IdString);
         }

         public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
         }
      });
   }
}