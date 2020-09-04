package com.dappergeek0.uplanit;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.dappergeek0.uplanit.fragments.*;
import com.dappergeek0.uplanit.dialogs.*;

/**
 * Created by DapperGeek0 on 3/12/2017.
 */

public class ShowDialogFragmentClass extends Fragment{

   public ShowDialogFragmentClass(){

   }

   public static ShowDialogFragmentClass newInstance(String clearTarget){
      ShowDialogFragmentClass showDialog = new ShowDialogFragmentClass();
      Bundle args = new Bundle();
      args.putString("Tag", clearTarget);
      showDialog.setArguments(args);
      return showDialog;
   }

   public void showDialog(DialogFragment fragment, String fragmentTag){

      // DialogFragment.show() will take care of adding the fragment
      // in a transaction.  We also want to remove any currently showing
      // dialog, so make our own transaction and take care of that here.
      FragmentTransaction ft = getFragmentManager().beginTransaction();
      Fragment prev = getFragmentManager().findFragmentByTag(fragmentTag);
      if (prev != null) {
         ft.remove(prev);
      }
      ft.addToBackStack(null);

      // Create and show the dialog.
      DialogFragment newFragment = fragment;
      newFragment.show(ft, fragmentTag);
   }
}
