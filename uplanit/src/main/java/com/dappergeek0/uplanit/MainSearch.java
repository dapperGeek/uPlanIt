package com.dappergeek0.uplanit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.dappergeek0.uplanit.datetime_and_placepickers.CreateAutoCompleteTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainSearch extends AppCompatActivity implements View.OnClickListener {

   // Declare Views
   AutoCompleteTextView inKeywordView, inLocation;
   ProgressBar keywordProgress, placeProgress;
   Spinner categoriesSpinner;
   Button doSearch;

   // Array to store categories from resource
   String[] categories;

   // Keyword suggestions from server json response
   ArrayList<String> suggestions = new ArrayList<>();

   // String ArrayList storing category titles
   ArrayList<String> titlesArrayList = new ArrayList<>();

   // String ArrayList storing category IDs
   ArrayList<String > idsArrayList = new ArrayList<>();

   // Suggested brands ArrayList
   ArrayList<HashMap<String, String >> brandsArraysList = new ArrayList<>();

   // Selected category Id : String value
   private String catId;

   // Task to implement places autocomplete
   CreateAutoCompleteTask mAutoTask;

   // Task to get suggested brands for keyword input
   KeywordAutoCompleteTask keyTask;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main_search);
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      // Set toolbar back button icon
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      // Views assignments
      inKeywordView = (AutoCompleteTextView) findViewById(R.id.search_keyword);
      inLocation = (AutoCompleteTextView)findViewById(R.id.location);
      keywordProgress = (ProgressBar) findViewById(R.id.keyword_autocomplete_progress);
      placeProgress = (ProgressBar) findViewById(R.id.place_autocomplete_progress);
      categoriesSpinner = (Spinner) findViewById(R.id.category_spinner);
      doSearch = (Button) findViewById(R.id.do_search);

      // Create categories spinner
      categories = getApplicationContext().getResources().getStringArray(R.array.categories);
      CreateSpinner();

      // Keyword autoComplete
      inKeywordView.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {

         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            String keyword = s.toString();
            if (keyword.length() > 2){
               keyTask = new KeywordAutoCompleteTask(keyword);
               keyTask.execute();
            }
         }

         @Override
         public void afterTextChanged(Editable s) {

         }
      });

      // Location autoComplete implementation
      inLocation.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
            String sString = s.toString();
            if (sString.length() > 2) {
               mAutoTask = new CreateAutoCompleteTask(getApplicationContext(), sString, Constants.placesKey, inLocation, placeProgress);
               mAutoTask.execute();
            }
         }

         @Override
         public void afterTextChanged(Editable s) {
         }
      });

      // Set OnClickListener on the search button
      doSearch.setOnClickListener(this);

   }

   // AppBar Back button & menu options
   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      switch (item.getItemId()){
         case android.R.id.home:
            finish();
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   // Handle onClick action(s)
   @Override
   public void onClick(View v){

      // Handle Search button onClick action
      if (v == doSearch){
         attemptSearch();
      }
   }

   // Spinner to show app categories
   public void CreateSpinner() {

      // Add default selected category
      titlesArrayList.add("All Categories");
      idsArrayList.add(String.valueOf(0));

      for (String str : categories) {
         String[] splits = str.split("=");
         String cId = splits[0];
         String cTitle = splits[1];
         titlesArrayList.add(cTitle);
         idsArrayList.add(cId);
      }

      // Create an ArrayAdapter using the string array and a default spinner layout
      ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),R.layout.spinner_style, titlesArrayList);
      // Specify the layout to use when the list of choices appears
      adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
      // Apply the adapter to the spinner
      categoriesSpinner.setAdapter(adapter);

      categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
         @Override
         public void onItemSelected(AdapterView<?> parent, View view,
                                    int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            catId = idsArrayList.get(pos);
         }

         public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
         }
      });
   }

   // AsyncTask to get keyword autoComplete suggestions
   public class KeywordAutoCompleteTask extends AsyncTask<String, String, ArrayList<String>>{
      private final String inKeyword;
      JSONParser jsonParser = new JSONParser();

      public KeywordAutoCompleteTask(String inKeyword){
         this.inKeyword = inKeyword;
      }

      @Override
      protected void onPreExecute() {
         keywordProgress.setVisibility(View.VISIBLE);
      }

      @Override
      protected ArrayList<String> doInBackground(String... args){

         try {
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.SEARCH_KEYWORD_STRING, inKeyword);

            // Send input to server
            JSONObject json = jsonParser.makeHttpRequest(
                Constants.searchKeywordUrl, Constants.parserPost, params);

            if (json != null){
               // Get server response status
               JSONObject statusKey = json.getJSONObject(Constants.STATUS);
               String status = statusKey.getString(Constants.STATUS);

               // If status is OK, proceed
               if (status.equals(Constants.STATUS_OK)){
                  JSONObject result = json.getJSONObject(Constants.RESULT);
                  JSONArray suggestionArray = result.getJSONArray(Constants.SUGGESTIONS);

                  // Loop through suggestions array
                  //noinspection LoopStatementThatDoesntLoop
                  for (int i = 0; i < suggestionArray.length(); i++){
                     JSONObject sugg = suggestionArray.getJSONObject(i);
                     String id = sugg.getString(Constants.IdString);
                     String categoryId = sugg.getString(Constants.categoryIdColumnString);
                     String name = sugg.getString(Constants.nameString);
                     String logo = sugg.getString(Constants.logoString);
                     String phone = sugg.getString(Constants.phoneString);
                     String address = sugg.getString(Constants.addressString);
                     String description = sugg.getString(Constants.descString);
                     String vPhone = sugg.getString(Constants.vPhone);
                     String vLocation = sugg.getString(Constants.vLocation);

                     // Declare HashMap
                     HashMap<String,String> suggestionsHashMap = new HashMap<>();

                     // Add suggestions to HashMap
                     suggestionsHashMap.put(Constants.IdString, id);
                     suggestionsHashMap.put(Constants.categoryId, categoryId);
                     suggestionsHashMap.put(Constants.brandNameString, name);
                     suggestionsHashMap.put(Constants.logoString, logo);
                     suggestionsHashMap.put(Constants.phoneString, phone);
                     suggestionsHashMap.put(Constants.addressString, address);
                     suggestionsHashMap.put(Constants.descString, description);
                     suggestionsHashMap.put(Constants.vLocation, vLocation);
                     suggestionsHashMap.put(Constants.vPhone, vPhone);

                     // Add HashMap to ArrayList if not already added
                     if (!brandsArraysList.contains(suggestionsHashMap)) {
                        brandsArraysList.add(suggestionsHashMap);
                     }

                     // Add brandName to Adapter ArrayList if not already added
                     if (!suggestions.contains(name)){
                        suggestions.add(name);
                     }
                     return suggestions;
                  }
               }
            }
         }
         catch (Exception e){
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(final ArrayList<String> suggestions){

         keywordProgress.setVisibility(View.GONE);

         if (suggestions != null){
            ArrayAdapter<String> autoTextAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_autocomplete_style,suggestions);
            // Specify the layout to use when the list of choices appears
            autoTextAdapter.setDropDownViewResource(R.layout.custom_autocomplete_style);

            //
            inKeywordView.setAdapter(autoTextAdapter);

            inKeywordView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
               @Override
               public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                  //
                  String sBrand = brandsArraysList.get(position).toString();

                  // Start Intent to show brand selected from dropdown list
                  Intent showBrand = new Intent(getApplication(), BrandActivity.class);
                  showBrand.putExtra(Constants.intentExtra, sBrand);
                  startActivity(showBrand);
               }
            });

         }
         else {
            Toast.makeText(getApplicationContext(), "No suggestions", Toast.LENGTH_SHORT).show();
         }
      }
   }

   // Verifies search parameters mostly keyword before search is initiated
   private void attemptSearch(){

      boolean cancel = false;
      View focusView = null;

      // Reset errors.
      inKeywordView.setError(null);
      inLocation.setError(null);

      // Store values at the time of the search attempt.
      String sKeyword = inKeywordView.getText().toString();
      String sLocation = inLocation.getText().toString();

      if (TextUtils.isEmpty(sKeyword)){
         inKeywordView.setError(getString(R.string.error_field_required));
         focusView = inKeywordView;
         cancel = true;
      }
      else if (!StringHandler.isValidTextInput(sKeyword)){
         inKeywordView.setError(getString(R.string.error_invalid_input_xters));
         focusView = inKeywordView;
         cancel = true;
      }

      if (!TextUtils.isEmpty(sLocation) && !StringHandler.isValidTextInput(sLocation)){
         inLocation.setError(getString(R.string.error_invalid_input_xters));
         focusView = inLocation;
         cancel = true;
      }

      if (cancel){
         // There was an error; don't attempt update and focus the first
         // form field with an error.
         focusView.requestFocus();
      }
      else {
         sLocation = !TextUtils.isEmpty(sLocation) ? sLocation : "All";
         HashMap<String, String> searchParams = new HashMap<>();
         searchParams.put(Constants.SEARCH_KEYWORD_STRING, sKeyword);
         searchParams.put(Constants.locationString, sLocation);
         searchParams.put(Constants.categoryId, catId);

         Intent searchIntent = new Intent(getApplicationContext(), MainActivity.class);
         searchIntent.putExtra(Constants.searchString, searchParams);
         startActivity(searchIntent);
         finish();
      }
   }

}
