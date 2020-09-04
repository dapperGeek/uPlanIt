package com.dappergeek0.uplanit;

import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;

public class ActivityVerificationsManager extends AppCompatActivity implements View.OnClickListener {

   // classes
   SessionManager sessionManager;
   SendCodeTask sendCodeTask = new SendCodeTask();
   ConfirmCodeTask confirmCodeTask;
   SendAddressTask sendAddressTask;

   // Views initialization
   private RelativeLayout sendLayout, verifyLayout, phoneLayout, locationLayout;
   private TextView opStatus, op2Status, showNumber, lStatus;
   private Button sendCode, confirmCode, submitAddress;
   private ProgressBar phoneProgress, codeProgress, addressProgress;
   private TextInputEditText inputCode, inputAddress;
   private Toolbar toolbar;

   // brand properties initialization
   private String userId, phoneNumber, vCode, vAddress, opMessage;

   // brand phone & location verification statuses
   private int vPhone, vLocation;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_verifications_manager);

      sessionManager = new SessionManager(getApplicationContext());

      // assign brand properties
      userId = sessionManager.getPreference(SessionManager.IdKey);
      phoneNumber = sessionManager.getPreference(SessionManager.phoneKey);

      // assign views
      sendLayout = (RelativeLayout) findViewById(R.id.send_layout);
      verifyLayout = (RelativeLayout) findViewById(R.id.verify_layout);
      phoneLayout = (RelativeLayout) findViewById(R.id.phone_layout);
      locationLayout = (RelativeLayout) findViewById(R.id.location_layout);
      opStatus = (TextView) findViewById(R.id.op_status);
      op2Status = (TextView) findViewById(R.id.op_2_status);
      inputCode = (TextInputEditText) findViewById(R.id.input_code);
      inputAddress = (TextInputEditText) findViewById(R.id.input_address);
      showNumber = (TextView) findViewById(R.id.show_number);
      lStatus = (TextView) findViewById(R.id.l_status);
      sendCode = (Button) findViewById(R.id.send_code);
      confirmCode = (Button) findViewById(R.id.confirm_code);
      submitAddress = (Button) findViewById(R.id.submit_address);
      phoneProgress = (ProgressBar) findViewById(R.id.v_phone_progress);
      codeProgress = (ProgressBar) findViewById(R.id.v_code_progress);
      addressProgress = (ProgressBar) findViewById(R.id.v_loc_progress);
      toolbar = (Toolbar) findViewById(R.id.toolbar);

      // display current phone number
      showNumber.setText(phoneNumber);

      // get verification statuses
      vPhone = getIntent().getIntExtra(Constants.vPhone, 0);
      vLocation = getIntent().getIntExtra(Constants.vLocation, 0);

      // hide views depending on verification statuses
      phoneLayout.setVisibility(vPhone == 1 ? View.GONE : View.VISIBLE);
      locationLayout.setVisibility(vLocation == 1 ? View.GONE : View.VISIBLE);

      // set onClickListeners
      sendCode.setOnClickListener(this);
      confirmCode.setOnClickListener(this);
      submitAddress.setOnClickListener(this);

      // set title
      setTitle(getResources().getString(R.string.manage_verifications));

      // Toolbar back navigation
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   // onClick operations
   @Override
   public void onClick(View v){
      //
      int id = v.getId();

      switch (id) {

         // initiates server side script to send code sms to phone
         case R.id.send_code:
            sendCodeTask.execute();
            break;

         // initiates sending the confirmation code for server side verification
         case R.id.confirm_code:
            setConfirmCode();
            break;

         // initiates sending address for address verification request
         case R.id.submit_address:
            setConfirmAddress();
            break;
      }
   }

   // back menu option
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

   // function initializes send verification code to server
   // and sanitizes input
   private void setConfirmCode(){
      if (confirmCodeTask != null) {
         return;
      }

      boolean cancel = false;
      View focusView = null;
      // Reset errors.

      inputCode.setError(null);

      vCode = inputCode.getText().toString();

      // check code input if empty
      if (TextUtils.isEmpty(vCode)) {
         inputCode.setError(getString(R.string.required_field_text));
         focusView = inputCode;
         cancel = true;
      }
      // check code input for invalid xters
      else if (!StringHandler.isValidTextInput(vCode)) {
         inputCode.setError(getString(R.string.error_invalid_input_xters));
         focusView = inputCode;
         cancel = true;
      }

      // if an error occurs, show error and discontinue
      if (cancel) {
         focusView.requestFocus();
      }

      // if no errors, proceed with sending code to server for verification
      else {
         confirmCodeTask = new ConfirmCodeTask(vCode);
         confirmCodeTask.execute();
      }

   }

   // function initializes send verification address request to server
   // and sanitizes input
   private void setConfirmAddress(){
      if (sendAddressTask != null) {
         return;
      }

      boolean cancel = false;
      View focusView = null;
      // Reset errors.

      inputAddress.setError(null);

      vAddress = inputAddress.getText().toString();

      // check code input if empty
      if (TextUtils.isEmpty(vAddress)) {
         inputAddress.setError(getString(R.string.required_field_text));
         focusView = inputAddress;
         cancel = true;
      }
      // check code input for invalid xters
      else if (!StringHandler.isValidTextInput(vAddress)) {
         inputAddress.setError(getString(R.string.error_invalid_input_xters));
         focusView = inputAddress;
         cancel = true;
      }

      // if and error occurs, set focus on field
      if (cancel) {
         focusView.requestFocus();
      }
      // if no errors, proceed with sending code to server for verification
      else {
         sendAddressTask = new SendAddressTask(vAddress);
         sendAddressTask.execute();
      }

   }

   // AsyncTask to send activate server side sms code send to phone number
   public class SendCodeTask extends AsyncTask<String , String , String > {
      private final JSONParser jsonParser = new JSONParser();

      public SendCodeTask(){

      }

      protected void onPreExecute(){
         showNumber.setVisibility(View.GONE);
         sendCode.setVisibility(View.GONE);
         phoneProgress.setVisibility(View.VISIBLE);
      }

      protected String doInBackground(String... args) {

         try {
            HashMap<String ,String> params = new HashMap<>();
            params.put(SessionManager.IdKey, userId);
            params.put(Constants.accTypeString, Constants.brandString);
            params.put(Constants.phoneString, phoneNumber);
            params.put(Constants.modeString, Constants.modeSend);

            JSONObject json = jsonParser.makeHttpRequest(Constants.verificationsUrl, Constants.parserPost, params);

            if (json != null) {
               JSONObject status = json.getJSONObject(Constants.jsonResponse);
               return status.getString(Constants.statusKey);
            }

         }catch (Exception e) {
            e.printStackTrace();
         }

         return null;
      }

      protected void onPostExecute(String statusKey) {

         if (statusKey != null) {

            switch (statusKey) {

               // if the status is OK
               case Constants.statusOk:
                  sendLayout.setVisibility(View.GONE);
                  verifyLayout.setVisibility(View.VISIBLE);
                  break;

               // if service is not yet available
               case Constants.statusUnavailable:
                  phoneProgress.setVisibility(View.GONE);
                  opStatus.setVisibility(View.VISIBLE);
                  opMessage = getResources().getString(R.string.verificationPending);
                  opStatus.setText(opMessage);
                  break;

               // if error processing request
               case Constants.statusError:
                  phoneProgress.setVisibility(View.GONE);
                  opStatus.setVisibility(View.VISIBLE);
                  opMessage = getResources().getString(R.string.error_processing);
                  opStatus.setText(opMessage);
                  break;
            }
         }
      }
   }

   // AsyncTask to send code confirmation
   public class ConfirmCodeTask extends AsyncTask<String , String , String > {

      private final JSONParser jsonParser = new JSONParser();
      private final String vCode;

      public ConfirmCodeTask(String vCode) {
         this.vCode = vCode;
      }

      protected void onPreExecute(){
         inputCode.setVisibility(View.GONE);
         confirmCode.setVisibility(View.GONE);
         codeProgress.setVisibility(View.VISIBLE);
      }

      protected String doInBackground(String... args) {

         try {
            HashMap<String ,String> params = new HashMap<>();
            params.put(SessionManager.IdKey, userId);
            params.put(Constants.accTypeString, Constants.brandString);
            params.put(Constants.phoneString, vCode);
            params.put(Constants.modeString, Constants.vPhone);

            JSONObject json = jsonParser.makeHttpRequest(Constants.verificationsUrl, Constants.parserPost, params);

            if (json != null) {
               JSONObject status = json.getJSONObject(Constants.jsonResponse);
               return status.getString(Constants.statusKey);
            }

         }catch (Exception e) {
            e.printStackTrace();
         }

         return null;
      }

      protected void onPostExecute(String statusKey) {

         if (statusKey != null) {

            switch (statusKey) {
               // if the status is OK
               case Constants.statusOk:

                  // hide and show views
                  codeProgress.setVisibility(View.GONE);
                  verifyLayout.setVisibility(View.VISIBLE);

                  // set success message
                  opMessage = getResources().getString(R.string.phone_verified);
                  op2Status.setText(opMessage);
                  break;

               // if the status is OK
               case Constants.statusError:

                  // hide and show views
                  codeProgress.setVisibility(View.GONE);
                  opMessage = getResources().getString(R.string.error_processing);

                  // set toast message
                  Toast.makeText(getApplicationContext(), opMessage, Toast.LENGTH_SHORT).show();
                  break;
            }
         }
      }
   }

   // AsyncTask to send office address verification request
   public class SendAddressTask extends AsyncTask<String , String , String > {

      private final JSONParser jsonParser = new JSONParser();
      private final String address;

      public SendAddressTask(String address) {
         this.address = address;
      }

      protected void onPreExecute(){
         inputAddress.setVisibility(View.GONE);
         submitAddress.setVisibility(View.GONE);
         addressProgress.setVisibility(View.VISIBLE);
      }

      protected String doInBackground(String... args) {

         try {
            HashMap<String ,String> params = new HashMap<>();
            params.put(SessionManager.IdKey, userId);
            params.put(Constants.accTypeString, Constants.brandString);
            params.put(Constants.addressString, address);
            params.put(Constants.modeString, Constants.vLocation);

            JSONObject json = jsonParser.makeHttpRequest(Constants.verificationsUrl, Constants.parserPost, params);

            if (json != null) {
               JSONObject status = json.getJSONObject(Constants.jsonResponse);
               return status.getString(Constants.statusKey);
            }

         }catch (Exception e) {
            e.printStackTrace();
         }

         return null;
      }

      protected void onPostExecute(String statusKey) {

         if (statusKey != null) {

            switch (statusKey) {

               // if the status is OK
               case Constants.statusOk:

                  // hide and display views
                  addressProgress.setVisibility(View.GONE);
                  lStatus.setVisibility(View.VISIBLE);
                  opMessage = getResources().getString(R.string.verificationPending);

                  // set message
                  lStatus.setText(opMessage);
                  break;

               // if the status is OK
               case Constants.statusError:

                  opMessage = getResources().getString(R.string.error_processing);

                  // toast an error message
                  Toast.makeText(getApplicationContext(),opMessage,Toast.LENGTH_SHORT).show();
                  break;
            }
         }
      }
   }
}