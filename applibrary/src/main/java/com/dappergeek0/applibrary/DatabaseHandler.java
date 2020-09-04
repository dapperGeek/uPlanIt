//package com.dappergeek0.applibrary;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.MatrixCursor;
//import android.database.SQLException;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//public class DatabaseHandler extends SQLiteOpenHelper {
//
//   // All Static variables
//   // Context
//   Context context;
//
//   //SessionManager class
//   SessionManager sessionManager;
//   // Database Version
//   private static final int DATABASE_VERSION = 1;
//
//   // Database Name
//   private static final String DATABASE_NAME = "uPlanIt";
//
//   // Events table name
//   private static final String TABLE_EVENTS = "events";
//
//   // Favorites table name
//   private static final String TABLE_FAVORITES = "favorites";
//
//   // Events Table Columns names
//   private static final String KEY_ID = "id";
//   private static final String KEY_USER_ID = "user_id";
//   private static final String KEY_BRAND_ID = "brand_id";
//   private static final String KEY_ACC_TYPE = "account_type";
//   private static final String KEY_TYPE = "type";
//   private static final String KEY_NAME = "name";
//   private static final String KEY_IMAGE = "image";
//   private static final String KEY_CREATED = "created";
//   private static final String KEY_START_DATE = "start_date";
//   private static final String KEY_START_TIME = "start_time";
//   private static final String KEY_END_DATE = "end_date";
//   private static final String KEY_END_TIME = "end_time";
//   private static final String KEY_LOCATION = "location";
//   private static final String KEY_DETAIL = "detail";
//
//   // Events Table column positions
//   /**
//    * ID = 1, userID=2, accType = 3, name=4, image=5, created=6, startDate=7,
//    * startTime = 8, endDate = 9, endTime = 10, location = 11, detail = 12
//    */
//   private static final Integer POS_ID = 0;
//   private static final Integer POS_USERID = 1;
//   private static final Integer POS_TYPE = 2;
//   private static final Integer POS_NAME = 3;
//   private static final Integer POS_IMAGE = 4;
//   private static final Integer POS_CREATED = 5;
//   private static final Integer POS_STARTDATE = 6;
//   private static final Integer POS_STARTTIME = 7;
//   private static final Integer POS_ENDDATE = 8;
//   private static final Integer POS_ENDTIME = 9;
//   private static final Integer POS_LOCATION = 10;
//   private static final Integer POS_DETAIL = 11;
//
//   public DatabaseHandler(Context context) {
//      super(context, DATABASE_NAME, null, DATABASE_VERSION);
//      sessionManager  = new SessionManager(context);
//   }
//
//   // Creating Tables
//   @Override
//   public void onCreate(SQLiteDatabase db) {
//      String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
//          + KEY_ID + " INTEGER PRIMARY KEY,"
//          + KEY_USER_ID + " INTEGER,"
//          + KEY_ACC_TYPE + " TEXT,"
//          + KEY_NAME + " TEXT,"
//          + KEY_IMAGE + " TEXT,"
//          + KEY_CREATED + " TIMESTAMP,"
//          + KEY_START_DATE + " DATE,"
//          + KEY_START_TIME + " TIME,"
//          + KEY_END_DATE + " DATE,"
//          + KEY_END_TIME + " TIME,"
//          + KEY_LOCATION + " TEXT,"
//          + KEY_DETAIL + " TEXT" + ")";
//
//      String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
//          + KEY_ID + " INTEGER PRIMARY KEY,"
//          + KEY_USER_ID + " INTEGER,"
//          + KEY_TYPE + " TEXT,"
//          + KEY_BRAND_ID + " INTEGER" + ")";
//
//      db.execSQL(CREATE_EVENTS_TABLE);
//      db.execSQL(CREATE_FAVORITES_TABLE);
//   }
//
//   // Upgrading database
//   @Override
//   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//      // Drop older table if existed
//      db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
//      db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
//
//      // Create tables again
//      onCreate(db);
//   }
//
//   /**
//    * All CRUD(Create, Read, Update, Delete) Operations
//    */
//
//   // Adding new event
//   void addEvent(Event event) {
//      SQLiteDatabase db = this.getWritableDatabase();
//
//      ContentValues values = new ContentValues();
//      values.put(KEY_USER_ID, Integer.parseInt(sessionManager.getPreference(KEY_ID))); // User ID
//      values.put(KEY_ACC_TYPE, sessionManager.getPreference(KEY_ACC_TYPE)); // User account type
//      values.put(KEY_NAME, event.getName()); // Events Name
//      values.put(KEY_IMAGE, event.getImage()); // Events timeline image
//      values.put(KEY_CREATED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())); // Events created timestamp
//      values.put(KEY_START_DATE, event.getStartDate()); // Events start date
//      values.put(KEY_START_TIME, event.getStartTime()); // Events start time
//      values.put(KEY_END_DATE, event.getEndDate()); // Events end date
//      values.put(KEY_END_TIME, event.getEndTime()); // Events end time
//      values.put(KEY_LOCATION, event.getLocation()); // Events location
//      values.put(KEY_DETAIL, event.getDetail()); // Events detail
//
//      // Inserting Row
//      db.insert(TABLE_EVENTS, null, values);
//      db.close(); // Closing database connection
//   }
//
//   // Adding new favorite when not logged in
//  public void addFavorite(Integer brandId){
//      SQLiteDatabase db = this.getWritableDatabase();
//
//      ContentValues values = new ContentValues();
//      values.put(KEY_USER_ID, 0); // user id
//      values.put(KEY_BRAND_ID, brandId); // fav brand id
//      values.put(KEY_TYPE, ""); // account type// Inserting Row
//
//      // Inserting Row
//      db.insert(TABLE_FAVORITES, null, values);
//      db.close(); // Closing database connection
//   }
//
//   // Adding new favorite when logged in
//   public void addFavorite(Integer id, Integer brandId, String type){
//      SQLiteDatabase db = this.getWritableDatabase();
//
//      ContentValues values = new ContentValues();
//      values.put(KEY_USER_ID, id); // user id
//      values.put(KEY_BRAND_ID, brandId); // fav brand id
//      values.put(KEY_TYPE, type); // account type
//
//      // Inserting Row
//      db.insert(TABLE_FAVORITES, null, values);
//      db.close(); // Closing database connection
//   }
//
//   //
//   public void removeFavorite(Integer brandId, Integer id, String type){
//
//      SQLiteDatabase db = this.getWritableDatabase();
//
//      if (id != null){
//
//         db.delete(TABLE_FAVORITES, KEY_USER_ID + " = ? AND `" + KEY_TYPE + "` = ? AND `" + KEY_BRAND_ID + "` = ?",
//             new String[] { String.valueOf(id), type, String.valueOf(brandId) });
//      }
//      else {
//         db.delete(TABLE_FAVORITES,
//             KEY_TYPE + " = ? AND " + KEY_BRAND_ID + " = ?",
//             new String[] { "", String.valueOf(brandId) });
//      }
//
//      db.close();
//   }
//
//   //
//   Boolean isFavorite(Integer brandId){
//      Boolean favorite = false;
//
//      try {
//         SQLiteDatabase db = this.getReadableDatabase();
//
//         // Define a projection that specifies which columns from the database
//// you will actually use after this query.
//         String[] projection = { KEY_ID };
//
//         // Filter results WHERE
//         String selection = KEY_BRAND_ID + "= ? AND " + KEY_TYPE + "= ?";
//
//         // Arguments for WHERE clause
//         String[] selectionArgs = { String.valueOf(brandId), ""};
//
//// How you want the results sorted in the resulting Cursor
////		String sortOrder =
////			FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";
//
//         Cursor cursor = db.query(
//             TABLE_FAVORITES, // The table to query
//             projection, // The columns to return
//             selection,  // The columns for the WHERE clause
//             selectionArgs,  // The values for the WHERE clause
//             null,         // don't group the rows
//             null,  // don't filter by row groups
//             null
//         );
//
//         favorite = (cursor.getCount() != 0);
//      }
//      catch (NullPointerException e) {
//         e.printStackTrace();
//      }
//
//      return favorite;
//   }
//
//   //
//   Boolean isFavorite(Integer id, Integer brandId, String type){
//
//      Boolean favorite = false;
//
//      try {
//         SQLiteDatabase db = this.getReadableDatabase();
//
//         // Define a projection that specifies which columns from the database
//// you will actually use after this query.
//         String[] projection = { KEY_ID };
//
//         // Filter results WHERE
//         String selection = KEY_USER_ID + " = ? AND " + KEY_BRAND_ID + "= ? AND " + KEY_TYPE + "= ?";
//
//         // Arguments for WHERE clause
//         String[] selectionArgs = {String.valueOf(id), String.valueOf(brandId), type};
//
//// How you want the results sorted in the resulting Cursor
////		String sortOrder =
////			FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";
//
//         Cursor cursor = db.query(
//             TABLE_FAVORITES, // The table to query
//             projection, // The columns to return
//             selection,  // The columns for the WHERE clause
//             selectionArgs,  // The values for the WHERE clause
//             null,         // don't group the rows
//             null,  // don't filter by row groups
//             null
//         );
//
//         favorite = (cursor.getCount() != 0);
//      }
//      catch (NullPointerException e) {
//         e.printStackTrace();
//      }
//
////     String countQuery = "SELECT  * FROM " + TABLE_FAVORITES + " WHERE " + KEY_USER_ID + "=" + id + " AND " + KEY_BRAND_ID + "=" + brandId + " AND "+ KEY_TYPE + "= '" + type + "'";
////     SQLiteDatabase db = this.getReadableDatabase();
////     Cursor cursor = db.rawQuery(countQuery, null);
//
//      return favorite;
//   }
//
//   // Get all favorites
//   Integer[] getFavorites(Integer id, String type){
//
//      try {
//         SQLiteDatabase db = this.getReadableDatabase();
//
//         Cursor cursor;
//
//         /** Query for when type is not empty
//          * non-empty type string indicates fav added when signed in
//          */
//         if (sessionManager.isLoggedIn()){
//            // Define a projection that specifies which columns
//            // from the database you will actually use after this query.
//            String[] projection = { KEY_BRAND_ID };
//
//            // Filter results WHERE
//            String selection = KEY_USER_ID + " = ? AND "+ KEY_TYPE + "= ?";
//
//            // Arguments for WHERE clause
//            String[] selectionArgs = {String.valueOf(id), type};
//
//// How you want the results sorted in the resulting Cursor
////		String sortOrder =
////			FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";
//
//            cursor = db.query(
//                TABLE_FAVORITES, // The table to query
//                projection, // The columns to return
//                selection,  // The columns for the WHERE clause
//                selectionArgs,  // The values for the WHERE clause
//                null,         // don't group the rows
//                null,  // don't filter by row groups
//                null
//            );
//         }
//         else {
//            /** empty type string query */
//
//            String selectQuery = "SELECT " + KEY_BRAND_ID + " FROM " + TABLE_FAVORITES + " WHERE " + KEY_TYPE + " = ''";
//
//            cursor = db.rawQuery(selectQuery, null);
//
//         }
//
//         // Declare and assign return Integer array
//         Integer[] brandIds = new Integer[cursor.getCount()];
//
//         int i = 0;
//         while (cursor.moveToNext()){
//            int sId = cursor.getInt(cursor.getColumnIndex(KEY_BRAND_ID));
//            brandIds[i] = sId;
//            i++;
//         }
//         return brandIds;
//      }
//      catch (NullPointerException e) {
//         e.printStackTrace();
//      }
//      return null;
//   }
//
//   // count favorites
//   Integer countFavorites(Integer id, String type){
//
//      try {
//         SQLiteDatabase db = this.getReadableDatabase();
//
//         Cursor cursor;
//
//         /** Query for when type is not empty
//          * non-empty type string indicates fav added when signed in
//          */
//         if (sessionManager.isLoggedIn()){
//
//            // Define a projection that specifies which columns
//            // from the database you will actually use after this query.
//            String[] projection = { KEY_BRAND_ID };
//
////            // Filter results WHERE
//            String selection = KEY_USER_ID + " = ? AND " + KEY_TYPE + " = ? ";
////
////            // Arguments for WHERE clause
//            String[] selectionArgs = { String.valueOf(id), type };
//
//            cursor = db.query(
//                TABLE_FAVORITES, // The table to query
//                projection, // The columns to return
//                selection,  // The columns for the WHERE clause
//                selectionArgs,  // The values for the WHERE clause
//                null,         // don't group the rows
//                null,  // don't filter by row groups
//                null
//            );
//         }
//         else {
//            /** empty type string query */
//
//            String selectQuery = "SELECT " + KEY_BRAND_ID + " FROM " + TABLE_FAVORITES + " WHERE " + KEY_TYPE + " = ''";
//
//            cursor = db.rawQuery(selectQuery, null);
//         }
//
//         // Declare and assign return Integer array
//         return cursor.getCount();
//      }
//      catch (NullPointerException e) {
//         e.printStackTrace();
//      }
//      return null;
//   }
//
//   // Clear all favorites
//   public void clearFavorites(){
//      SQLiteDatabase db = getWritableDatabase();
//      db.execSQL("DELETE FROM " + TABLE_FAVORITES);
////      db.delete(TABLE_FAVORITES, KEY_USER_ID + " = ?", new String[] {null});
//      db.close();
//   }
//
//   // Getting single event
//   Event getEvent(int id) {
//      SQLiteDatabase db = this.getReadableDatabase();
//
//      Cursor cursor = db.query(TABLE_EVENTS, null, KEY_ID + "=?",
//          new String[] { String.valueOf(id) }, null, null, null, null);
//      if (cursor != null)
//         cursor.moveToFirst();
//
//      Event event = new Event();
//      event.setID(cursor.getInt(POS_ID));
//      event.setName(cursor.getString(POS_NAME));
//      event.setImage(cursor.getString(POS_IMAGE));
//      event.setStartDate(cursor.getString(POS_STARTDATE));
//      event.setStartTime(cursor.getString(POS_STARTTIME));
//      event.setEndDate(cursor.getString(POS_ENDDATE));
//      event.setEndTime(cursor.getString(POS_ENDTIME));
//      event.setLocation(cursor.getString(POS_LOCATION));
//      event.setDetail(cursor.getString(POS_DETAIL));
//
//      return event;
//   }
//
//   int getEventID(String user_id, String eventName){
//      int eventID = 0;
//      try {
//         SQLiteDatabase db = this.getReadableDatabase();
//
//         // Define a projection that specifies which columns from the database
//// you will actually use after this query.
//         String[] projection = {
//             KEY_ID
//         };
//
//         // Filter results WHERE "title" = 'My Title'
//         String selection = KEY_USER_ID + " = ? AND "+ KEY_NAME + "= ?";
//         String[] selectionArgs = { String.valueOf(user_id), eventName };
//
//// How you want the results sorted in the resulting Cursor
////		String sortOrder =
////			FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";
//
//         Cursor cursor = db.query(
//             TABLE_EVENTS, // The table to query
//             projection, // The columns to return
//             selection,  // The columns for the WHERE clause
//             selectionArgs,  // The values for the WHERE clause
//             null,         // don't group the rows
//             null,  // don't filter by row groups
//             null
//         );
//         if (cursor != null)
//            cursor.moveToFirst();
//         try {
//            eventID = Integer.parseInt(cursor.getString(POS_ID));
//         }catch (IndexOutOfBoundsException e) {
//            e.printStackTrace();
//         }
//      }catch (NullPointerException e) {
//         e.printStackTrace();
//      }
//      return eventID;
//   }
//
//   // Getting All Events
//   public List<Event> getAllEvents() {
//      List<Event> eventList = new ArrayList<>();
//      // Select All Query
//      String selectQuery = "SELECT  * FROM " + TABLE_EVENTS + " WHERE " + KEY_USER_ID + " = " + Integer.parseInt(sessionManager.getPreference(Constants.IdString)) + " ORDER BY " + KEY_ID + " DESC";
//
//      SQLiteDatabase db = this.getWritableDatabase();
//      Cursor cursor = db.rawQuery(selectQuery, null);
//
//      // looping through all rows and adding to list
//      if (cursor.moveToFirst()) {
//         do {
//            Event event = new Event();
//            event.setID(cursor.getInt( POS_ID));
//            event.setName(cursor.getString(POS_NAME));
//            event.setImage(cursor.getString(POS_IMAGE));
//            event.setStartDate(cursor.getString(POS_STARTDATE));
//            event.setStartTime(cursor.getString(POS_STARTTIME));
//            event.setEndDate(cursor.getString(POS_ENDDATE));
//            event.setEndTime(cursor.getString(POS_ENDTIME));
//            event.setLocation(cursor.getString(POS_LOCATION));
//            event.setDetail(cursor.getString(POS_DETAIL));
//            // Adding event to list
//            eventList.add(event);
//         } while (cursor.moveToNext());
//      }
//
//      // return event list
//      return eventList;
//   }
//
//   // Updating single event
//   public int updateEvent(Event event, String eventID) {
//      SQLiteDatabase db = this.getWritableDatabase();
//
//      ContentValues values = new ContentValues();
//      values.put(KEY_NAME, event.getName()); // Events Name
//      values.put(KEY_IMAGE, event.getImage()); // Events timeline image
//      values.put(KEY_START_DATE, event.getStartDate()); // Events start date
//      values.put(KEY_START_TIME, event.getStartTime()); // Events start time
//      values.put(KEY_END_DATE, event.getEndDate()); // Events end date
//      values.put(KEY_END_TIME, event.getEndTime()); // Events end time
//      values.put(KEY_LOCATION, event.getLocation()); // Events location
//      values.put(KEY_DETAIL, event.getDetail()); // Events detail
//
//      // updating row
//      return db.update(TABLE_EVENTS, values, KEY_ID + " = ?",
//          new String[] { eventID });
//   }
//
//   // Deleting single event
//   public void deleteEvent(Event event) {
//      SQLiteDatabase db = this.getWritableDatabase();
//      db.delete(TABLE_EVENTS, KEY_ID + " = ?",
//          new String[] { String.valueOf(event.getID()) });
//      db.close();
//   }
//
//   // Getting events Count
//   public int getEventsCount() {
//      String countQuery = "SELECT  * FROM " + TABLE_EVENTS + " WHERE " + KEY_USER_ID + "=" + Integer.parseInt(sessionManager.getPreference(KEY_ID));
//      SQLiteDatabase db = this.getReadableDatabase();
//      Cursor cursor = db.rawQuery(countQuery, null);
//
//      // return count
//      return cursor.getCount();
//   }
//
//   // Deleting events
//   public void clearEvents() {
//      SQLiteDatabase db = this.getWritableDatabase();
//      db.delete(TABLE_EVENTS, KEY_USER_ID + " = ?", new String[] {sessionManager.getPreference(KEY_ID)});
//      db.close();
//   }
//
//   /**
//    *  AndroidDatabaseManager helper method
//    */
//   public ArrayList<Cursor> getData(String Query){
//      //get writable database
//      SQLiteDatabase sqlDB = this.getWritableDatabase();
//      String[] columns = new String[] { "mesage" };
//      //an array list of cursor to save two cursors one has results from the query
//      //other cursor stores error message if any errors are triggered
//      ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
//      MatrixCursor Cursor2= new MatrixCursor(columns);
//      alc.add(null);
//      alc.add(null);
//
//
//      try{
//         String maxQuery = Query ;
//         //execute the query results will be save in Cursor c
//         Cursor c = sqlDB.rawQuery(maxQuery, null);
//
//
//         //add value to cursor2
//         Cursor2.addRow(new Object[] { "Success" });
//
//         alc.set(1,Cursor2);
//         if (null != c && c.getCount() > 0) {
//
//
//            alc.set(0,c);
//            c.moveToFirst();
//
//            return alc ;
//         }
//         return alc;
//      } catch(SQLException sqlEx){
//         Log.d("printing exception", sqlEx.getMessage());
//         //if any exceptions are triggered save the error message to cursor an return the arraylist
//         Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
//         alc.set(1,Cursor2);
//         return alc;
//      } catch(Exception ex){
//
//         Log.d("printing exception", ex.getMessage());
//
//         //if any exceptions are triggered save the error message to cursor an return the arraylist
//         Cursor2.addRow(new Object[] { ""+ex.getMessage() });
//         alc.set(1,Cursor2);
//         return alc;
//      }
//
//
//   }
//}