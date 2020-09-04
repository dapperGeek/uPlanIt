package com.dappergeek0.uplanit.datetime_and_placepickers;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by DapperGeek0 on 2/13/2017.
 */

public class CreateAutoCompleteTask extends AsyncTask<String, String, ArrayList > {

   String [] descriptionArray;
   ArrayList<String> descArray = new ArrayList<>();

   Context context;

   private final String inText;
   private final String placesKey;
   private final AutoCompleteTextView cView;
   private final ProgressBar autoProgress;
   JSONParser jsonParser = new JSONParser();
   private final String apiUrl = "http://myapp.worldwidevoyagerng.com/get_places.php";


   public CreateAutoCompleteTask(Context _context, String inputText, String apiKey, AutoCompleteTextView textView, ProgressBar pBar) {
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

//         JSONObject json = jsonParser.makeHttpRequest(
//             "https://urevent.000webhostapp.com/get_places.php", "GET", params);

         JSONObject json = jsonParser.makeHttpRequest(
             Constants.placesUrl, "GET", params);

         if (json != null) {
            //Get the response status or error message
            JSONArray predictions = json.getJSONArray("predictions");
            int arraySize = predictions.length();
            descriptionArray = new String[arraySize];
            for (int i=0; i<arraySize; i++) {
               JSONObject data = predictions.getJSONObject(i);
               String description = data.getString("description");
               descArray.add(description);
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

         ArrayAdapter<String> autoTextAdapter = new ArrayAdapter<String>(context, R.layout.spinner_style,descriptionsList);
         // Specify the layout to use when the list of choices appears
         autoTextAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

         //
         cView.setAdapter(autoTextAdapter);
      }
   }
}