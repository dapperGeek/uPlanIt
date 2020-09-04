package com.dappergeek0.uplanit.datetime_and_placepickers;

/**
 * Created by DapperGeek0 on 2/11/2017.
 */

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
    implements DatePickerDialog.OnDateSetListener {

   Integer viewID;

   public DatePickerFragment(){
      //Required empty constructor
   }

   // Instantiate fragment new instance
   public static DatePickerFragment newInstance(Integer viewID) {
      DatePickerFragment datePickerFragment = new DatePickerFragment();
      Bundle args = new Bundle();
      args.putInt("view_id", viewID);
      datePickerFragment.setArguments(args);
      return datePickerFragment;
   }
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the current date as the default date in the picker
      final Calendar c = Calendar.getInstance();
      int year = c.get(Calendar.YEAR);
      int month = c.get(Calendar.MONTH);
      int day = c.get(Calendar.DAY_OF_MONTH);



      // Create a new instance of DatePickerDialog and return it
      return new DatePickerDialog(getActivity(), this, year, month, day);
   }

   public void onDateSet(DatePicker view, int year, int month, int day) {
      // Do something with the date chosen by the user
      Calendar calendar = Calendar.getInstance();
      calendar.set(year, month, day);

      SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyy");
      String output = formatter.format(calendar.getTime());

      viewID = getArguments().getInt("view_id");
      TextInputEditText dateView = (TextInputEditText) getActivity().findViewById(viewID);
      dateView.setText(output);
   }
}
