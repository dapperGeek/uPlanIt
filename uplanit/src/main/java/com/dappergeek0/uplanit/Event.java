package com.dappergeek0.uplanit;

import android.content.Context;

/**
 * Created by DapperGeek0 on 2/8/2017.
 */

public class Event {

   //private variables
   Context context;
   SessionManager sessionManager;

   int id;
   private int user_id;
   private String acc_type;
   private String event_name;
   private String start_date;
   private String start_time;
   private String end_date;
   private String end_time;
   private String location;
   private String detail;
   private String imagePath;



   // Empty constructor
   public Event(){
   }

   // constructor
   public Event(String event_name,
                String imagePath,
                String start_date,
                String start_time,
                String end_date,
                String end_time,
                String location,
                String detail){
      this.event_name = event_name;
      this.imagePath = imagePath;
      this.start_date = start_date;
      this.start_time = start_time;
      this.end_date = end_date;
      this.end_time = end_time;
      this.location = location;
      this.detail = detail;
   }

   // constructor
   public Event(int id, int user_id){
      this.id = id;
      this.user_id = user_id;
   }

   // getting ID
   public int getID(){
      return this.id;
   }

   // setting id
   public void setID(int id){
      this.id = id;
   }

// getting sessionID
   public int getUserID(){
      return this.user_id;
   }

// getting ID
   public String getAccountType(){
      return this.acc_type;
   }

   // getting name
   public String getName(){
      return this.event_name;
   }

   // setting name
   public void setName(String name){
      this.event_name = name;
   }

   // getting image
   public String getImage(){
      return this.imagePath;
   }

   // setting image
   public void setImage(String imagePath){
      this.imagePath = imagePath;
   }

   // getting start date
   public String getStartDate(){
      return this.start_date;
   }

   // setting start date
   public void setStartDate(String start_date){
      this.start_date = start_date;
   }

   // getting start time
   public String getStartTime(){
      return this.start_time;
   }

   // setting start time
   public void setStartTime(String start_time){
      this.start_time = start_time;
   }

   // getting end date
   public String getEndDate(){
      return this.end_date;
   }

   // setting end date
   public void setEndDate(String end_date){
      this.end_date = end_date;
   }

   // getting end time
   public String getEndTime(){
      return this.end_time;
   }

   // setting end time
   public void setEndTime(String end_time){
      this.end_time = end_time;
   }

   // getting location
   public String getLocation(){
      return this.location;
   }

   // setting location
   public void setLocation(String location){
      this.location = location;
   }

   // getting end time
   public String getDetail(){
      return this.detail;
   }

   // setting end time
   public void setDetail(String detail){
      this.detail = detail;
   }
}