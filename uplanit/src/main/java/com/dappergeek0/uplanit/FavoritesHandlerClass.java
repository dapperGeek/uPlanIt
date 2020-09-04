package com.dappergeek0.uplanit;

import android.content.Context;

/**
 * Created by DapperGeek0 on 3/8/2017.
 */

public class FavoritesHandlerClass {

   private Integer id = 0;
   String type;

   Context context;

   SessionManager sessionManager;
   DatabaseHandler databaseHandler;

   public FavoritesHandlerClass(){
   }

   public FavoritesHandlerClass(Context context){
      this.context = context;
      this.sessionManager = new SessionManager(context);
      this.databaseHandler = new DatabaseHandler(context);
   }

   // Adding favorite to SQLite db
   public void addFavorite(String brandId){

      if (sessionManager.isLoggedIn()){
         id = Integer.parseInt(sessionManager.getPreference(SessionManager.IdKey));
         type = sessionManager.getPreference(SessionManager.accountType);
         databaseHandler.addFavorite(id, Integer.parseInt(brandId), type);
      }
      else {
         databaseHandler.addFavorite(Integer.parseInt(brandId));
      }
   }

   // Removing favorite
   public void removeFavorite(String brandId){

      if (sessionManager.isLoggedIn()){
         id = Integer.parseInt(sessionManager.getPreference(SessionManager.IdKey));
         type = sessionManager.getPreference(SessionManager.accountType);
         databaseHandler.removeFavorite(Integer.parseInt(brandId), id, type);
      }
      else {
         databaseHandler.removeFavorite(Integer.parseInt(brandId), null, null);
      }
   }

   public Boolean isFavorite(String brandId){
      Boolean favorite;

      if (sessionManager.isLoggedIn()){
         id = Integer.parseInt(sessionManager.getPreference(SessionManager.IdKey));
         type = sessionManager.getPreference(SessionManager.accountType);

         favorite = databaseHandler.isFavorite(id, Integer.parseInt(brandId), type);
      }
      else {
        favorite = databaseHandler.isFavorite(Integer.parseInt(brandId));
      }

      return favorite;
   }

   public Integer countFavorites(){

      Integer num;

      if (sessionManager.isLoggedIn()){
         id = Integer.parseInt(sessionManager.getPreference(SessionManager.IdKey));
         type = sessionManager.getPreference(SessionManager.accountType);

        num = databaseHandler.countFavorites(id, type);
      }
      else {
        num = databaseHandler.countFavorites(null, null);
      }
      return num;
   }

//   public class countServerFavorites extends AsyncTask<String , String , Integer>{
//
//      JSONParser jsonParser = new JSONParser();
//      private final String userId;
//
//      public countServerFavorites(String userId){
//         this.userId = userId;
//      }
//
//      @Override
//      protected Integer doInBackground(String ... args){
//
//         try {
//            HashMap<String ,String > params = new HashMap<>();
//            params.put(Constants.modeString, Constants.modeCount);
//            params.put(Constants.IdString, userId);
//
//            JSONObject json = jsonParser.makeHttpRequest(Constants.favoritesUrl, Constants.parserPost, params);
//
//            if (json != null){
//
//               JSONObject jResponse = json.getJSONObject(Constants.jsonResponse);
//               String status = jResponse.getString(Constants.statusKey);
//               favsCount = jResponse.getInt(Constants.modeCount);
//
//               if (status.equals(Constants.statusOk)){
//                  sessionManager.setPreference(SessionManager.favsKey, favsCount);
//               }
//               return favsCount;
//            }
//
//         }
//         catch (Exception e){
//            e.printStackTrace();
//         }
//         return null;
//      }
//   }
}
