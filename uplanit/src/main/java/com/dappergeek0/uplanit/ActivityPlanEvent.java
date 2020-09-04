package com.dappergeek0.uplanit;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dappergeek0.uplanit.datetime_and_placepickers.CreateAutoCompleteTask;
import com.dappergeek0.uplanit.datetime_and_placepickers.DatePickerFragment;
import com.dappergeek0.uplanit.datetime_and_placepickers.TimePickerFragment;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

public class ActivityPlanEvent extends AppCompatActivity implements View.OnClickListener {

   //Initialize Views
   TextInputEditText eventName, editStartDate, editStartTime, editEndDate,
       editEndTime, editDetail;
   ImageView eventImage;
   AutoCompleteTextView editLocation;
   ProgressBar autoProgress;
   Button saveEvent;
   Button updateEvent;
   Toolbar toolbar;
   // DatabaseHandle class
   DatabaseHandler dbHandler;
   //
   ServerEventTask serverEventTask = null;
   // Event Sync Type : Indicates if to Create or Update
   String syncType;

   //
   Event event;

   //
   Integer extraEventID;
   //
   Bitmap bitmapImage;
   // Session
   SessionManager sessionManager;
   String eName, eStartDate, eStartTime, eEndDate, eEndTime, eLocation, eDetail, userID, accountType;
   Integer eventID;
   private String imgPath, encodedString;
   private int widthHeight = Constants.brandLogoDimensions;

   private CreateAutoCompleteTask mAutoTask = null;

   FragmentManager fragmentManager = getSupportFragmentManager();

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_plan_event);

      //
      sessionManager = new SessionManager(getApplicationContext());

      // Get the session ID and account type
      userID = sessionManager.getPreference(Constants.IdString);
      accountType = sessionManager.getPreference(Constants.accTypeString);
      // Get intent extra SyncType
      syncType = getIntent().getStringExtra(Constants.SYNC_TYPE);
      //Views
      eventName = (TextInputEditText) findViewById(R.id.event_name);
      eventImage = (ImageView) findViewById(R.id.event_image_select);
      editStartDate = (TextInputEditText) findViewById(R.id.edit_start);
      editStartTime = (TextInputEditText) findViewById(R.id.edit_start_time);
      editEndDate = (TextInputEditText) findViewById(R.id.edit_end);
      editEndTime = (TextInputEditText) findViewById(R.id.edit_end_time);
      editLocation = (AutoCompleteTextView) findViewById(R.id.event_location);
      autoProgress = (ProgressBar) findViewById(R.id.place_autocomplete_progress);
      editDetail = (TextInputEditText) findViewById(R.id.event_details);
      saveEvent = (Button) findViewById(R.id.save_event);
      updateEvent = (Button) findViewById(R.id.update_event);

      // Hide either of the save and update buttons depending syncType
      hideButton(syncType);

      // Screen title depending on sync type
      String title = syncType.equals(Constants.SYNC_CREATE) ? getResources().getString(R.string.title_activity_plan_event) : getResources().getString(R.string.update_event);

      // Set title
      setTitle(title);

      // Toolbar back navigation
      toolbar = (Toolbar) findViewById(R.id.toolbar);
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      // declare DatabaseHandler class
      dbHandler = new DatabaseHandler(getApplicationContext());

      // Update views with event details if edit option
      if (syncType.equals(Constants.SYNC_UPDATE)) {
         // Get event ID from intent extra
         extraEventID = getIntent().getIntExtra(Constants.IdString, 0);

         // get event
        event = dbHandler.getEvent(extraEventID);

         imgPath = event.getImage();
         Bitmap eImage = ImageEncoder.decodeSampledBitmapFromFile(imgPath, 100, 100);
         eventImage.setImageBitmap(eImage);

         eventName.setText(event.getName());
         editStartDate.setText(event.getStartDate());
         editStartTime.setText(event.getStartTime());
         editEndDate.setText(event.getEndDate());
         editEndTime.setText(event.getEndTime());
         editLocation.setText(event.getLocation());
         editDetail.setText(event.getDetail());
      }


      // OnClickListener for image selection
      eventImage.setOnClickListener(this);

      // Set onClick Listeners for date and time inputs
      editStartDate.setOnClickListener(this);
      //
      editStartTime.setOnClickListener(this);

      editEndDate.setOnClickListener(this);

      editEndTime.setOnClickListener(this);

      // Set onClickListener for save event button
      saveEvent.setOnClickListener(this);

      // Set onClickListener for update event button
      updateEvent.setOnClickListener(this);

      // Set the Google places api query class on the location input
      editLocation.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            String sString = s.toString();
            if (sString.length() > 2){
               mAutoTask = new CreateAutoCompleteTask(getApplicationContext(), sString, Constants.placesKey, editLocation, autoProgress);
               mAutoTask.execute();
            }
         }

         @Override
         public void afterTextChanged(Editable s) {
         }
      });
   }

   //back option menu
   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      switch (item.getItemId()){
         case android.R.id.home:
            finish();
            return true;

         default:
            return super.onOptionsItemSelected(item);
      }
   }

   // Method to hide either the save or update button depending on the syncType
   // Intent extra
   private void hideButton(String syncType) {
      if (syncType.equals(Constants.SYNC_CREATE)) {
         updateEvent.setVisibility(View.GONE);
      }
      if (syncType.equals(Constants.SYNC_UPDATE)) {
         saveEvent.setVisibility(View.GONE);
      }
   }

   // OnClick events sets date and time respectively for date and time input fields
   @Override
   public void onClick(View v){

      // Handle click event for edit start date
      if (v == editStartDate) {
         if (TextUtils.isEmpty(editStartTime.getText())) {
            showTimePickerDialog(R.id.edit_start_time);
         }
         showDatePickerDialog(R.id.edit_start);
      }

      // Handle click event for edit start time
      if (v == editStartTime) {
         showTimePickerDialog(R.id.edit_start_time);
      }

      // Handle click event for edit end date
      if (v == editEndDate) {
         if (TextUtils.isEmpty(editEndTime.getText())) {
            showTimePickerDialog(R.id.edit_end_time);
         }
         showDatePickerDialog(R.id.edit_end);
      }

      // Handle click event for edit end time
      if (v == editEndTime) {
         showTimePickerDialog(R.id.edit_end_time);
      }

      // Handle save event button onClick action
      if (v == saveEvent) {
         saveEvent(Constants.SYNC_CREATE);
      }

      // Handle save event button onClick action
      if (v == updateEvent) {
         saveEvent(Constants.SYNC_UPDATE);
      }

      // Handle Image selection
      if (v == eventImage) {
         showFileChooser();
      }
   }

   // Method to show image chooser
   private void showFileChooser() {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE_REQUEST);
   }

   /**
    * Handling the image chooser activity result
    * @param requestCode
    * @param resultCode
    * @param data
    */
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      try {
         // When an Image is picked
         if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == Constants.RESULT_OK
             && null != data) {
            // Get the Image from data
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            // Get the cursor
            Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imgPath = cursor.getString(columnIndex);
            cursor.close();
            /**
             * Process sampled down image using the same dimensions for width and height values
             */
            bitmapImage = decodeSampledBitmapFromFile(imgPath,widthHeight,widthHeight);

            /**
             * Set the Image in ImageView
             */
            eventImage.setImageBitmap(bitmapImage);

            //TODO: code to get image name when necessary ,if...
//                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

//            String[] splits = imgPath.split("/");
//            int i = splits.length -  1;
//             imageName = splits[i];
//            String[] img = imageName.split("\\.");
//            imageName = img[0] + timeStamp + "." + img[1] ;
//            Toast.makeText(getApplicationContext(),imageName,Toast.LENGTH_SHORT).show();
            // Save image to app memory
//            FileOutputStream fos = openFileOutput(imageName, Context.MODE_PRIVATE);
//            fos.write(imgPath.getBytes());
//            fos.close();
//            openFileInput(imageName).read();

         } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.warning_no_image_selected), Toast.LENGTH_SHORT).show();
         }
      } catch (Exception e) {
         Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_something), Toast.LENGTH_SHORT).show();
      }
   }

   // Task to convert image to string
   public void encodeImageToString() {
      //Bitmap to get image from gallery
      Bitmap bitmap;

      /* Process sampled down image using the same dimensions for width and
       height values */
      bitmap = decodeSampledBitmapFromFile(imgPath,widthHeight,widthHeight);

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      // Must compress the Image to reduce image size to make upload easy
      bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
      byte[] byte_arr = stream.toByteArray();
      // Encode Image to String
      encodedString = Base64.encodeToString(byte_arr, 0);
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

      // First decode with inJustDecodeBounds=true to check dimensions
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
    * @param options
    * @param reqWidth
    * @param reqHeight
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

   public void saveEvent(String syncMode) {
      if (serverEventTask != null) {
         return;
      }

      boolean cancel = false;
      View focusView = null;
      // Reset errors.

      eventName.setError(null);
      editStartDate.setError(null);
      editStartTime.setError(null);
      editEndDate.setError(null);
      editEndTime.setError(null);
      editLocation.setError(null);
      editDetail.setError(null);

      eName = eventName.getText().toString();
      eStartDate = editStartDate.getText().toString();
      eStartTime = editStartTime.getText().toString();
      eEndDate = editEndDate.getText().toString();
      eEndTime = editEndTime.getText().toString();
      eLocation = editLocation.getText().toString();
      eDetail = editDetail.getText().toString();

      // process image selected from gallery
      if (imgPath != null && !imgPath.isEmpty()) {
         // Convert image to String using Base64
         encodeImageToString();
      }

      // if image is not selected from gallery, show post notification
      else {
         Toast.makeText(
             getApplicationContext(),
             getResources().getString(R.string.warning_select_image),
             Toast.LENGTH_SHORT).show();
         cancel = true;
      }

      // Validate event name for improper input
      if (!StringHandler.isValidTextInput(eName)) {
         eventName.setError(getString(R.string.error_invalid_input_xters));
         focusView = eventName;
         cancel = true;
      }
      else if (TextUtils.isEmpty(eName)) {// Check if input is empty
         eventName.setError(getString(R.string.error_field_required));
         focusView = eventName;
         cancel = true;
      }

      else if (!TextUtils.isEmpty(eName)) {
         eName =  StringHandler.capWords(eName);
         // Check for duplicate event name
         eventID = dbHandler.getEventID(userID,eName);

         // Cancel submit if duplicate event name exists
         if (eventID != 0 && syncMode.equals(Constants.SYNC_CREATE)) {
            eventName.setError(getString(R.string.error_duplicate_event_name));
            focusView = eventName;
            cancel = true;
         }
      }

      // Check for empty event start date
      if (TextUtils.isEmpty(eStartDate)) {
         editStartDate.setError(getString(R.string.error_field_required));
         focusView = editStartDate;
         cancel = true;
      }

      // Check for empty event start time
      if (TextUtils.isEmpty(eStartTime)) {
         editStartTime.setError(getString(R.string.error_field_required));
         focusView = editStartTime;
         cancel = true;
      }

      // Check for empty event end date
      if (TextUtils.isEmpty(eEndDate)) {
         editEndDate.setError(getString(R.string.error_field_required));
         focusView = editEndDate;
         cancel = true;
      }

      // Check for empty event end time
      if (TextUtils.isEmpty(eEndTime)) {
         editEndTime.setError(getString(R.string.error_field_required));
         focusView = editEndTime;
         cancel = true;
      }

      // Check for empty event location
      if (TextUtils.isEmpty(eLocation)) {
         editLocation.setError(getString(R.string.error_field_required));
         focusView = editLocation;
         cancel = true;
      }

      // Check for improper xter in location
      else if (!StringHandler.isValidTextInput(eLocation)) {
         editLocation.setError(getString(R.string.error_invalid_input_xters));
         focusView = editLocation;
         cancel = true;
      }

      // Check for improper detail input
      if (!TextUtils.isEmpty(eDetail) && !StringHandler.isValidTextInput(eDetail)) {
         editDetail.setError(getString(R.string.error_invalid_input_xters));
         focusView = editDetail;
         cancel = true;
      }

      if (cancel) {
         // There was an error; don't attempt update and focus the first
         // form field with an error.
         try {
            if (focusView != null) {
               focusView.requestFocus();
            }
         }
         catch (NullPointerException e) {
            e.printStackTrace();
         }
      }
      else { // No error, proceed with create or update action
         event = new Event(eName, imgPath, eStartDate, eStartTime, eEndDate, eEndTime, eLocation, eDetail);
         // Handle the local DB create event action
         if (syncMode.equals(Constants.SYNC_CREATE)) {
            dbHandler.addEvent(event);
            saveEvent.setEnabled(false);
         }
         // Handle local DB update event action
         else if (syncMode.equals(Constants.SYNC_UPDATE)) {
            dbHandler.updateEvent(event, String.valueOf(extraEventID));
            updateEvent.setEnabled(false);
         }

         // Update title as event name
         setTitle(StringHandler.capWords(eName));

         // Handle server side create or update event action
         serverEventTask = new ServerEventTask(String.valueOf(eventID), eName, encodedString, eStartDate, eStartTime, eEndDate, eEndTime, eLocation, eDetail, syncMode);
         serverEventTask.execute((String) null);

         // show the event listing if event is just created
         if (syncMode.equals(Constants.SYNC_CREATE)) {
            Intent showEvents = new Intent(this, MainActivity.class);
            showEvents.putExtra(Constants.showEvents, Constants.showEvents);
            startActivity(showEvents);
         }
         finish();
      }
   }

   // Show Date Picker Dialog
   public void showDatePickerDialog(int id) {
      DialogFragment newFragment = new DatePickerFragment();
      Bundle args = new Bundle();
      args.putInt("view_id",id);
      newFragment.setArguments(args);
      newFragment.show(fragmentManager, "datePicker");
   }

   //Show TimePicker Dialog
public void showTimePickerDialog(int id) {
      DialogFragment newFragment = new TimePickerFragment();
      Bundle args = new Bundle();
      args.putInt("view_id", id);
      newFragment.setArguments(args);
      newFragment.show(fragmentManager, "timePicker");
   }

   public class ServerEventTask extends AsyncTask<String,String,HashMap<String,String>> {
      private final JSONParser jsonParser = new JSONParser();
      private final String eID;
      private final String eName;
      private final String encodedString;
      private final String eStartDate;
      private final String eStartTime;
      private final String eEndDate;
      private final String eEndTime;
      private final String eLocation;
      private final String eDetail;
      private final String eSync;

      public ServerEventTask(String eID, String eName, String encodedString, String eStartDate, String eStartTime, String eEndDate, String eEndTime, String eLocation, String eDetail, String eSync){
         this.eID = eID;
         this.eName = eName;
         this.encodedString = encodedString;
         this.eStartDate = eStartDate;
         this.eStartTime = eStartTime;
         this.eEndDate = eEndDate;
         this.eEndTime = eEndTime;
         this.eLocation = eLocation;
         this.eDetail = eDetail;
         this.eSync = eSync;
      }

      protected void onPreExecute() {
         String dMsg = eSync.equals(Constants.SYNC_CREATE) ? "Event saved" : "Event edited";
         Toast.makeText(getApplicationContext(), dMsg,Toast.LENGTH_SHORT).show();
      }

      protected HashMap<String ,String > doInBackground(String... args) {
         try {
            HashMap<String ,String> params = new HashMap<>();

            params.put(Constants.IdString, eID);
            params.put(Constants.ACCOUNT_ID, sessionManager.getPreference(Constants.IdString));
            params.put(Constants.accTypeString, sessionManager.getPreference(Constants.accTypeString));
            params.put(Constants.EVENT, eName);
            params.put(Constants.IMAGE, encodedString);
            params.put(Constants.START_DATE, eStartDate);
            params.put(Constants.START_TIME, eStartTime);
            params.put(Constants.END_DATE, eEndDate);
            params.put(Constants.END_TIME, eEndTime);
            params.put(Constants.locationString, eLocation);
            params.put(Constants.DETAIL, eDetail);
            params.put(Constants.SYNC_TYPE, eSync);

            JSONObject json = jsonParser.makeHttpRequest(Constants.eventSyncUrl, Constants.parserPost, params);

            if (json != null) {
               HashMap<String ,String > jsonHashMap = new HashMap<>();
               JSONObject status = json.getJSONObject(Constants.STATUS);
               String serverStats = status.getString(Constants.STATUS);
               jsonHashMap.put(Constants.STATUS,serverStats);
               return jsonHashMap;
            }

         }catch (Exception e){
            e.printStackTrace();
         }
         return null;
      }

      protected void onPostExecute(HashMap<String,String> jsonResult) {

         if (jsonResult != null){
            String status = jsonResult.get(Constants.STATUS);
            switch (status){
               case Constants.STATUS_OK:
//                  Toast.makeText(getApplicationContext(),"Sync complete",Toast.LENGTH_SHORT).show();
                  break;
               case Constants.STATUS_ERROR:
//                  Toast.makeText(getApplicationContext(),"Unable to sync",Toast.LENGTH_SHORT).show();
                  break;
            }
//            finish();
         }
//         else {
////            Toast.makeText(getApplicationContext(),"Unable to sync to server",Toast.LENGTH_SHORT).show();
////            finish();
//         }
      }
   }
}