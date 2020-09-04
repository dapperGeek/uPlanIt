package com.dappergeek0.uplanit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class EventViewerActivity extends AppCompatActivity implements View.OnClickListener {

   // Intent extra message
   Integer eventIdExtraString;

   // DatabaseHandler class
   DatabaseHandler databaseHandler;

   // StringHandler class declaration
   static StringHandler stringHandler = new StringHandler();

   // Views declarations
   ImageView eventImageView;
   Button eUpdate, eShare;
   TextView startInfo, endInfo, eLocation, eDetails;
   Toolbar toolbar;

   //
   String thumb, eName, startDate, startTime,
       endDate, endTime, location, _details;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_event_viewer);

      //
      databaseHandler = new DatabaseHandler(getApplicationContext());

      // Get intent extras
      eventIdExtraString = getIntent().getIntExtra(Constants.intentExtra, -1);

      // Get event data from the event ID
      Event event = databaseHandler.getEvent(eventIdExtraString);

           // Views assignment
      eventImageView = (ImageView) findViewById(R.id.event_image);
      eUpdate = (Button) findViewById(R.id.update_event);
      eShare = (Button) findViewById(R.id.share_event);
      startInfo = (TextView) findViewById(R.id.start_info);
      endInfo = (TextView) findViewById(R.id.end_info);
      eLocation = (TextView) findViewById(R.id.event_location);
      eDetails = (TextView) findViewById(R.id.details);

      //
      thumb = event.getImage();
      eName = event.getName();
      startDate = event.getStartDate();
      startTime = event.getStartTime();
      endDate = event.getEndDate();
      endTime = event.getEndTime();
      location = event.getLocation();
      _details = event.getDetail();

      toolbar = (Toolbar) findViewById(R.id.toolbar);

      // Set toolbar back button icon
      toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
      setSupportActionBar(toolbar);
      try {
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      }catch (NullPointerException e) {
         e.printStackTrace();
      }

      // Set buttons onClickListeners
      eUpdate.setOnClickListener(this);
      eShare.setOnClickListener(this);

      setupEvent(event);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater menuInflater = getMenuInflater();
      menuInflater.inflate(R.menu.menu_view_event, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      switch (item.getItemId()){
         // Handle back button click event
         case android.R.id.home:
            finish();
            return true;

         // Handle appbar edit click event
         case R.id.action_edit:
            updateEvent();
            return true;

         // Handle appBar share event click action
         case R.id.action_share:
            shareEvent();
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onResume(){
      Event event = databaseHandler.getEvent(eventIdExtraString);
      setupEvent(event);
      super.onResume();
   }

   public void setupEvent(Event event) {
      // Set the screen title
      setTitle(stringHandler.capWords(event.getName()));

      Bitmap eImage = ImageEncoder.decodeSampledBitmapFromFile(event.getImage(), 100, 100);
      eventImageView.setImageBitmap(eImage);
      startInfo.setText(event.getStartDate() + " " + event.getStartTime());
      endInfo.setText(event.getEndDate() + " " + event.getEndTime());
      eLocation.setText(event.getLocation());
      eDetails.setText(event.getDetail());
   }

   @Override
   public void onClick(View view){

      // Handle action for update button click
      if (view == eUpdate){
         updateEvent();
      }

      // Handle action for share button click
      if (view == eShare){
         shareEvent();
      }
   }

   public void shareEvent(){
      String shareMessage = StringHandler.shareEventMessage(eName, startDate, startTime, location, _details);

      Intent shareEvent =  new Intent();
      shareEvent.setAction(Intent.ACTION_SEND);
      shareEvent.putExtra(Intent.EXTRA_TEXT, shareMessage);
      shareEvent.setType("text/plain");
      startActivity(shareEvent);
   }

   public void updateEvent(){
      Intent editEvent = new Intent(this, ActivityPlanEvent.class);
      editEvent.putExtra(Constants.SYNC_TYPE, Constants.SYNC_UPDATE);
      editEvent.putExtra(Constants.IdString, eventIdExtraString);
      startActivity(editEvent);
   }
}
