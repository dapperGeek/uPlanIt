package com.dappergeek0.uplanit.datetime_and_placepickers;

/**
 * Created by DapperGeek0 on 2/13/2017.
 */

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener {

   Integer viewID;

   public TimePickerFragment(){
      //Required empty constructor
   }

   // Instantiate fragment new instance
   public static TimePickerFragment newInstance(Integer viewID) {
      TimePickerFragment timePickerFragment = new TimePickerFragment();
      Bundle args = new Bundle();
      args.putInt("view_id", viewID);
      timePickerFragment.setArguments(args);
      return timePickerFragment;
   }
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the current date as the default date in the picker
      final Calendar c = Calendar.getInstance();
      int hour = c.get(Calendar.HOUR_OF_DAY);
      int minute = c.get(Calendar.MINUTE);



      // Create a new instance of DatePickerDialog and return it
      return new TimePickerDialog(getActivity(), this, hour, minute, false);
   }

   public void onTimeSet(TimePicker view, int hour, int minute) {
      // Do something with the date chosen by the user
      String am_pm;

      Calendar datetime = Calendar.getInstance();
      datetime.set(Calendar.HOUR_OF_DAY, hour);
      datetime.set(Calendar.MINUTE, minute);
      String sep = ":";

      if (hour == 0) {
         hour = 12;
         am_pm = "AM";
      }

      else if (hour == 12) {
         am_pm = "PM";
      }

      else if (hour > 12) {
         hour -= 12;
         am_pm = "PM";
      }
      else {
         am_pm = "AM";
      }

      if (minute < 10) {
         sep = ":0";
      }
      String output = hour + sep + minute + " " + am_pm;

      viewID = getArguments().getInt("view_id");
      TextInputEditText TimeView = (TextInputEditText) getActivity().findViewById(viewID);
      TimeView.setText(output);
   }
}

