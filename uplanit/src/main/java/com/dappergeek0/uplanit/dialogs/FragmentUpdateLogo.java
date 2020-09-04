package com.dappergeek0.uplanit.dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.ImageEncoder;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.SessionManager;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentUpdateLogo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentUpdateLogo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentUpdateLogo extends DialogFragment implements View.OnClickListener {

//    Declare Views
   private ProgressBar progressBar;
   private ImageView logoView;
   RelativeLayout dButtons;
   private Button selectImgBtn, dismissBtn, saveBtn;
   View dialogView;

   //
   private String brandLogo, imgPath, encodedString;
   private int widthHeight = Constants.brandLogoDimensions;

   UpdateLogoTask updateLogoTask;

   ContextWrapper contextWrapper;

   SessionManager sessionManager;

   public FragmentUpdateBrandInfo.ReloadBrandDetailsListener bListener;

   private OnFragmentInteractionListener mListener;

   public FragmentUpdateLogo() {
      // Required empty public constructor
   }

   /**
    * Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
    * @return A new instance of fragment FragmentUpdateLogo.
    */
   public static FragmentUpdateLogo newInstance() {
      return new FragmentUpdateLogo();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      dialogView = inflater.inflate(R.layout.fragment_update_logo, container, false);

      contextWrapper = new ContextWrapper(getContext());
      sessionManager = new SessionManager(getContext());

      progressBar = (ProgressBar) dialogView.findViewById(R.id.dialog_progress_bar);
      dButtons = (RelativeLayout) dialogView.findViewById(R.id.dialog_buttons);
      logoView = (ImageView) dialogView.findViewById(R.id.brand_logo_view);
      selectImgBtn = (Button) dialogView.findViewById(R.id.select_image_btn);
      dismissBtn = (Button) dialogView.findViewById(R.id.dismiss_btn);
      saveBtn = (Button) dialogView.findViewById(R.id.submit_btn);

      //  Display current logo in imageView
      brandLogo = Constants.LogoPath(sessionManager.getPreference(Constants.logoString));
      Picasso.with(getContext()).load(brandLogo).into(logoView);

      // Set OnClickListeners
      logoView.setOnClickListener(this);
      selectImgBtn.setOnClickListener(this);
      dismissBtn.setOnClickListener(this);
      saveBtn.setOnClickListener(this);

      return dialogView;
   }

   @Override
   public void onClick(View v){
      int id = v.getId();

      switch (id){

         case R.id.brand_logo_view:
            showFileChooser();
            break;

         //
         case R.id.select_image_btn:
            showFileChooser();
            break;

         //
         case R.id.dismiss_btn:
            dismiss();
            break;

         //
         case R.id.submit_btn:
            attemptUpload();
            break;
      }

   }

   // TODO: Rename method, update argument and hook method into UI event
   public void onButtonPressed(Uri uri) {
      if (mListener != null) {
         mListener.onFragmentInteraction(uri);
      }
   }

   @Override
   public void onAttach(Context context) {
      super.onAttach(context);
      try {
         bListener = (FragmentUpdateBrandInfo.ReloadBrandDetailsListener) getActivity();
      } catch (ClassCastException e) {
         throw new ClassCastException(context.toString() + " must implement ReloadBrandDetailsListener");
      }
   }

   @Override
   public void onDetach() {
      super.onDetach();
      mListener = null;
   }

   /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
   public interface OnFragmentInteractionListener {
      void onFragmentInteraction(Uri uri);
   }

   /**
    * Method to show image chooser
    */
   private void showFileChooser() {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE_REQUEST);
   }

   /**
    * Handling the image chooser activity result
    * @param requestCode
    * @param resultCode
    * @param data
    */
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);

      try {
         // When an Image is picked
         if (requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == Constants.RESULT_OK
             && null != data) {
            // Get the GlideImage from data
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            // Get the cursor
            Cursor cursor = contextWrapper.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            imgPath = cursor.getString(columnIndex);
            cursor.close();
            /**
             * Process sampled down image using the same dimensions for width and height values
             */
            Bitmap bitmapImage = ImageEncoder.decodeSampledBitmapFromFile(imgPath,widthHeight,widthHeight);
            /**
             * Set the GlideImage in ImageView
             */
            logoView.setImageBitmap(bitmapImage);
         } else {
            Toast.makeText(getContext(), getResources().getString(R.string.warning_no_image_selected),
                Toast.LENGTH_SHORT).show();
         }
      } catch (Exception e) {
         Toast.makeText(getContext(), getResources().getString(R.string.wrong_something), Toast.LENGTH_SHORT)
             .show();
      }
   }

   private void attemptUpload() {
      if (updateLogoTask != null) {
         return;
      }

      boolean cancel = false;

      /**
       * // When Image is selected from Gallery
       */
      if (imgPath != null && !imgPath.isEmpty()) {

         // Convert image to String using Base64
         ImageEncoder.encodeImageToString(imgPath, widthHeight, widthHeight);
      }

      // When Image is not selected from Gallery
      else {

         // make toast if new image is not selected
         Toast.makeText(
             getContext(),
             getResources().getString(R.string.warning_select_image),
             Toast.LENGTH_SHORT).show();
         cancel = true;
      }

      if (cancel) {

         // make toast if new image is not selected
         Toast.makeText(
             getContext(),
             getResources().getString(R.string.warning_select_image),
             Toast.LENGTH_SHORT).show();
      } else {
         updateLogoTask = new UpdateLogoTask(ImageEncoder.encodedString);
         updateLogoTask.execute((String) null);
      }
   }

   public class UpdateLogoTask extends AsyncTask<String, String, String >{

      private final String id;
      private final String encodedImage;
      private final JSONParser jsonParser;

      UpdateLogoTask(String encodedImage){
         this.id = sessionManager.getPreference(SessionManager.IdKey);
         this.encodedImage = encodedImage;
         this.jsonParser = new JSONParser();
      }

      @Override
      protected void onPreExecute(){
         showProgress(true);
      }

      @Override
      protected String doInBackground(String ... args){

         try {
            HashMap<String, String> params = new HashMap<>();

            params.put(Constants.modeString, Constants.modeLogoUpdate);
            params.put(Constants.IdString, id);
            params.put(Constants.IMAGE, encodedImage);

            JSONObject json = jsonParser.makeHttpRequest(Constants.brandUpdateUrl, Constants.parserPost, params);

            if (json != null){

               //Get the authentication status or error message
               JSONObject jStatus = json.getJSONObject(Constants.statusKey);
               String statusKey = jStatus.getString(Constants.statusKey);

               // if status os OK, get logo detail
               if (statusKey.equals(Constants.statusOk)){

                  JSONObject response = json.getJSONObject(Constants.jsonResponse);
                  String logo = response.getString(Constants.logoString);

                  // update logo in session
                  sessionManager.updateLogo(logo);
               }
               // return the string indicating server response status (statusKey)
               return statusKey;
            }
         }
         catch (Exception e){
            e.printStackTrace();
         }

         return null;
      }

      @Override
      protected void onPostExecute(String status){
         showProgress(false);
         String toast = null;

         if (status != null){

            switch (status){

               // handle case when status is OK
               case Constants.statusOk:
                  // Toast message
                  toast = getResources().getString(R.string.info_update_ok);

                  // implement interface to reload the dashboard details
                  bListener.ReloadBrandDetails();

                  // close dialog
                  dismiss();
                  break;

               //
               case Constants.statusError:
                  toast = getResources().getString(R.string.error_processing);
                  break;
            }

         }
         else {

            // Make toast indicating connection error
            toast = getResources().getString(R.string.error_dataConnection);
         }

         /** toast indicating AsyncTask status */
         Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
      }
   }

   /**
    * Shows the progress UI and hides the login form.
    */
   @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
   public void showProgress(final boolean show) {
      // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
      // for very easy animations. If available, use these APIs to fade-in
      // the progress spinner.
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
         int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

         dismissBtn.setEnabled(!show);
         saveBtn.setEnabled(!show);
         progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
         progressBar.animate().setDuration(shortAnimTime).alpha(
             show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
               progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
         });
      } else {
         // The ViewPropertyAnimator APIs are not available, so simply show
         // and hide the relevant UI components.
         dismissBtn.setEnabled(!show);
         saveBtn.setEnabled(!show);
         progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
         progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
      }
   }
}
