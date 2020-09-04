package com.dappergeek0.uplanit.dialogs;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.DatabaseHandler;
import com.dappergeek0.uplanit.FragmentFavorites;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.fragments.UserEventsFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DialogActionClear#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DialogActionClear extends DialogFragment implements View.OnClickListener {

   // declare views
   private TextView confirmTextView;
   private Button dismissBtn, submitButton;
   private View dialogView;

   // classes
   private DatabaseHandler dbHandler;

   // interface
   FragmentReloadListener reloadListener;

   // TODO: Rename parameter arguments, choose names that match
   // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
   private static final String ARG_PARAM1 = "param1";
   private static final String ARG_PARAM2 = "param2";

   // TODO: Rename and change types of parameters
   private String clearTarget;
   private String confirmText;


   public DialogActionClear() {
      // Required empty public constructor
   }

   /**
    * Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
//    * @param param1 Parameter 1.
//    * @param param2 Parameter 2.
    * @return A new instance of fragment DialogActionClear.
    */
   // TODO: Rename and change types and number of parameters
   public static DialogActionClear newInstance(String clearTarget) {
      DialogActionClear fragment = new DialogActionClear();
      Bundle args = new Bundle();
      args.putString(ARG_PARAM1, clearTarget);
//      args.putString(ARG_PARAM2, param2);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onAttach(Context context){
      super.onAttach(context);

      try {
         reloadListener = (FragmentReloadListener) getActivity();
      } catch (ClassCastException e) {
         throw new ClassCastException((context.toString()) + " must implement FragmentReloadListener");
      }
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (getArguments() != null) {
         clearTarget = getArguments().getString(ARG_PARAM1);
//         mParam2 = getArguments().getString(ARG_PARAM2);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      dialogView = inflater.inflate(R.layout.dialog_action_clear, container, false);

      // assign classes
      dbHandler = new DatabaseHandler(getContext());

      confirmTextView = (TextView) dialogView.findViewById(R.id.confirm_clear);
      dismissBtn = (Button) dialogView.findViewById(R.id.dismiss_btn);
      submitButton = (Button) dialogView.findViewById(R.id.submit_btn);

      // change buttons text
      String noText = "No";
      String yesText = "Yes";
      dismissBtn.setText(noText);
      submitButton.setText(yesText);

      // get argument to determine clear action target
      switch (clearTarget) {

         // case of events clear
         case Constants.myEventsFragTag:
            confirmText = getResources().getString(R.string.confirm_events_clear);
            break;

         //  clear favorites
         case Constants.favoritesFragtag:
            confirmText = getResources().getString(R.string.confirm_fav_clear);
            break;
      }

      //
      confirmTextView.setText(confirmText);

      // set onClickListeners
      dismissBtn.setOnClickListener(this);
      submitButton.setOnClickListener(this);

      //set dialog title
     getDialog().setTitle(getResources().getString(R.string.title_clear_list));

      return dialogView;
   }

   @Override
   public void onClick(View view){

      //
      Fragment fragment = null;

      switch (view.getId()){

         //
         case R.id.dismiss_btn:
            dismiss();
            break;

         //
         case R.id.submit_btn:

            //
            switch (clearTarget) {

               //
               case Constants.myEventsFragTag:
                  dbHandler.clearEvents();
                  fragment = new UserEventsFragment();
                  break;

               //
               case Constants.favoritesFragtag:
                  dbHandler.clearFavorites();
                  fragment = new FragmentFavorites();
            }

            reloadListener.FragmentReload(fragment, clearTarget);
            dismiss();
            break;
      }

   }

   // interface to reload the favorites screen after clearing the list
   public interface FragmentReloadListener {
      void FragmentReload(Fragment fragment, String fragmentTag);
   }
}
