package com.dappergeek0.uplanit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.Objects;


public class SpinnerFragment extends Fragment {

    String[] categories;//Categories Array string placeholder
    ArrayList<HashMap<String, String>> titlesArrayList = new ArrayList<>();//ArrayList for category titles
    ArrayList<HashMap<String, String>> idsArrayList = new ArrayList<>();//ArrayList for category IDs
    ArrayList<String> titlesArrayString = new ArrayList<>();
    Spinner spinner;
    AutoCompleteTextView autoCompleteTextView;
   ProgressBar progressBar;
    String inputText;
    private CreateCompleteTask mAutoTask = null;
   String [] descriptionArray;
   ArrayList<String> descArray = new ArrayList<>();
   String myApiKey = "AIzaSyCR_c60PtDunKmSzs2ozSDUGfSHXmWO2uM";

   LoginButton fbLogin;
   TextView fbToken;
    View rootView;

   CallbackManager callbackManager;

   public SpinnerFragment(){
    }

    public static SpinnerFragment newInstance(){
        SpinnerFragment spinnerFragment = new SpinnerFragment();
        return spinnerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

       callbackManager = CallbackManager.Factory.create();

       rootView = inflater.inflate(R.layout.spinner_fragment,container,false);
        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        autoCompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.place_autocomplete_input);
       progressBar = (ProgressBar) rootView.findViewById(R.id.place_autocomplete_progress);

        inputText = autoCompleteTextView.getText().toString();

        categories = getActivity().getResources().getStringArray(R.array.categories);

        CreateSpinner();

       fbLogin = (LoginButton) rootView.findViewById(R.id.fb_login_button);
       fbToken = (TextView) rootView.findViewById(R.id.fb_token);

       fbLogin.setReadPermissions("public_profile","email");
       // If using in a fragment
       fbLogin.setFragment(this);

       // FBLogin callback
       fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
          @Override
          public void onSuccess(LoginResult loginResult) {
             GraphRequest request = GraphRequest.newMeRequest(
                 loginResult.getAccessToken(),
                 new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                       Log.v("Main", response.toString());
                       setProfileToView(object);

                       try {
                          String name = object.getString(Constants.nameString);
                          String email = object.getString(Constants.emailString);

                          RegisterFBTask mAuthTask = new RegisterFBTask(Constants.FB_LOGIN, name, email);
                          mAuthTask.execute();
                       }
                       catch (Exception e){
                          e.printStackTrace();
                       }

                    }
                 });

             Bundle parameters = new Bundle();
             parameters.putString("fields", "id,name,email");
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

       Button openWeb = (Button) rootView.findViewById(R.id.open_web);

       openWeb.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             Intent openWeb = new Intent(getContext(), WebViewActivity.class);
//             Intent openWeb = new Intent(getContext(), AndroidDatabaseManager.class);
             openWeb.putExtra(Constants.openUrl, Constants.privacyUrl);
             startActivity(openWeb);
          }
       });

       autoCompleteTextView.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
             String sString = s.toString();
             mAutoTask = new CreateCompleteTask(getContext(), sString, Constants.placesKey, autoCompleteTextView, progressBar);
             mAutoTask.execute();
          }

          @Override
          public void afterTextChanged(Editable s) {
          }
       });

        return rootView;
    }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      callbackManager.onActivityResult(requestCode, resultCode, data);
   }

   private void setProfileToView(JSONObject jsonObject) {
      fbToken.setText(jsonObject.toString());
   }

    public void CreateSpinner(){

        for (String str : categories) {
            //HashMap to save category titles
            HashMap<String, String> titleHashMap = new HashMap<>();
            //HashMap to store category IDs
            HashMap<String, String> idHashMap = new HashMap<>();
            String[] splits = str.split("=");
            String cId = splits[0];
            String cTitle = splits[1];
            titleHashMap.put("category", cTitle);
            idHashMap.put("id", cId);
            titlesArrayList.add(titleHashMap);
            idsArrayList.add(idHashMap);
            titlesArrayString.add(cTitle);
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.spinner_style,titlesArrayString);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)
                String category = titlesArrayList.get(pos).get("category");
                String catId = idsArrayList.get(pos).get("id");
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    //
    public class CreateCompleteTask extends AsyncTask<String, String, ArrayList > {

       String [] descriptionArray;
       ArrayList<String> descArray = new ArrayList<>();
       ArrayList<String> placeIds = new ArrayList<>();

       Context context;

       private final String inText;
       private final String placesKey;
       private final AutoCompleteTextView cView;
       private final ProgressBar autoProgress;
       JSONParser jsonParser = new JSONParser();
       private final String apiUrl = "http://myapp.worldwidevoyagerng.com/get_places.php";


       public CreateCompleteTask(Context _context, String inputText, String apiKey, AutoCompleteTextView textView, ProgressBar pBar) {
          inText = inputText;
          placesKey = apiKey;
          cView = textView;
          context = _context;
          autoProgress = pBar;
       }

       @Override
       protected void onPreExecute() {
          autoProgress.setVisibility(View.VISIBLE);
       }
       @Override
       protected ArrayList doInBackground(String ... args){

          try {
             HashMap<String, String> params = new HashMap<>();
             params.put(Constants.placesInput, inText);
             params.put(Constants.placesApiString, placesKey);

             JSONObject json = jsonParser.makeHttpRequest(
                 Constants.placesUrl, "GET", params);

             if (json != null) {
                //Get the response status or error message
                JSONArray predictions = json.getJSONArray("predictions");
                int arraySize = predictions.length();
                descriptionArray = new String[arraySize];
                for (int i=0; i<arraySize; i++) {
                   JSONObject data = predictions.getJSONObject(i);
                   String description = data.getString(Constants.descString);
                   String placeId = data.getString("place_id");

                   descArray.add(description);
                   placeIds.add(placeId);
                }
                return descArray;
             }
          } catch (Exception e) {
             e.printStackTrace();
          }
          return null;
       }

       @Override
       protected void onPostExecute(ArrayList descriptionsList){
          autoProgress.setVisibility(View.GONE);

          if (descriptionsList != null){

             ArrayAdapter<String > autoTextAdapter = new ArrayAdapter<String>(context, R.layout.spinner_style,descriptionsList);
             // Specify the layout to use when the list of choices appears
             autoTextAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);


             //
             cView.setAdapter(autoTextAdapter);

             cView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                      Toast.makeText(getContext(),placeIds.get(position),Toast.LENGTH_SHORT).show();
                }
             });

          }
       }
    }

    public class RegisterFBTask extends AsyncTask<String, String, String>{

       JSONParser jsonParser = new JSONParser();

       private final String param1;
       private final String param2;
       private final String mLoginMode;

       RegisterFBTask(String loginMode, String param1, String param2) {
          this.mLoginMode = loginMode;
          this.param1 = param1;
          this.param2 = param2;
       }

       @Override
       protected void onPreExecute(){
          progressBar.setVisibility(View.VISIBLE);
       }

       @Override
       protected String doInBackground(String ... args){
          // get authentication against a network service.
          try {
             HashMap<String, String> params = new HashMap<>();
             params.put(Constants.LOGIN_MODE, mLoginMode);

             // parameters for native app login
             if (mLoginMode.equals(Constants.APP_LOGIN)) {
                params.put(Constants.usernameString, param1);
                params.put(Constants.passHashString, param2);
             }

             // post parameters for FB login
             else {
                params.put(Constants.nameString, param1);
                params.put(Constants.emailString, param2);
             }

             JSONObject json = jsonParser.makeHttpRequest(Constants.loginUrl, Constants.parserPost, params);

             if (json != null){
                return json.toString();
             }
          }
          catch (Exception e){
             e.printStackTrace();
          }
          return null;
       }

       @Override
       protected void onPostExecute(String json){
          progressBar.setVisibility(View.GONE);

          if (json != null){
             fbToken.setText(json);
          }
          else {
             String text = "error in process";
             fbToken.setText(text);
          }
       }
    }
}