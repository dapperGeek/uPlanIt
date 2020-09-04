package com.dappergeek0.uplanit.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.OpenWebLinks;
import com.dappergeek0.uplanit.R;
import com.dappergeek0.uplanit.SessionManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;

import org.json.JSONArray;
import org.json.JSONObject;

import info.androidhive.glide.activity.SlideshowDialogFragment;
import info.androidhive.glide.adapter.GalleryAdapter;
import info.androidhive.glide.model.GlideImage;

public class BrandDashGalleryFragment extends Fragment implements View.OnClickListener{

    private Integer premiumStatus;
    private String id, username;
    private int REQUEST_CODE_PICKER = 2000;

//    Arrays and Lists
    private ArrayList<Image> images = new ArrayList<>();
    private ArrayList<GlideImage> selectedImages = new ArrayList<>();
    private ArrayList<GlideImage> serverImages = new ArrayList<>();
    private HashMap<Integer,String> imagePaths = new HashMap<>();
    private HashMap<Integer,String> serverImagesPaths = new HashMap<>();

    private View mProgressView;
    private TextView mProgressString;
   private RelativeLayout emptyGalleryView;

   // SessionManager class
   SessionManager sessionManager;

    private View rootView;
    private GalleryAdapter selectedAdapter;
    private GalleryAdapter serverAdapter;
    private RecyclerView selectedRecyclerView;
    private RecyclerView serverRecyclerView;

    private String imgPath, encodedString, emptyGalleryText;
    private HashMap<Integer,String> encodedImages = new HashMap<>();
    private int imageWidth = Constants.brandLogoDimensions;
    private int imageHeight = Constants.brandLogoDimensions;
    //Upload button
    Button buttonUpload;
    Button clearSelection;

    RelativeLayout buttonsGallery;

    public static Bitmap bitmap;
   ImageView deleteImage;
   View sView;

    public BrandDashGalleryFragment() {
        // Required empty public constructor
    }

    public static BrandDashGalleryFragment newInstance(){
        return new BrandDashGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        rootView = inflater.inflate(R.layout.fragment_brand_dash_gallery, container, false);
        //Get user details from data passed to fragment

        sessionManager = new SessionManager(getContext());

        //Session user details
        id = sessionManager.getPreference(SessionManager.IdKey);
        username = sessionManager.getPreference(SessionManager.usernameKey);
        premiumStatus = sessionManager.getIntPreference(SessionManager.premiumKey);

       // text to show for empty gallery
       emptyGalleryText = premiumStatus.equals(1)
           ? getResources().getString(R.string.empty_gallery_premium)
           : getResources().getString(R.string.empty_gallery_admin);

        // assign views
        emptyGalleryView =(RelativeLayout) rootView.findViewById(R.id.empty_gallery_view);
        mProgressView = rootView.findViewById(R.id.app_progress_bar);
        mProgressString =(TextView) rootView.findViewById(R.id.progress_loading_string);
       TextView emptyGalleryTextView = (TextView) rootView.findViewById(R.id.empty_gallery);

       sView = inflater.inflate(R.layout.fragment_image_slider, null, false);
       deleteImage = (ImageView) sView.findViewById(R.id.delete_image);
       deleteImage.setVisibility(View.VISIBLE);

       // set empty gallery text
       emptyGalleryTextView.setText(emptyGalleryText);

       //Fetch pictures from brand server gallery
       new RetrieveGalleryTask(id).execute();

       // Hide or show the empty gallery text accordingly
       emptyGalleryView.setVisibility(selectedImages.size() > 0 ? View.GONE : View.VISIBLE);

       // show number of pictures in gallery
//       showCount();

       setHasOptionsMenu(true);//Set fragment options menu
       getActivity().invalidateOptionsMenu();

        //View for images selected from phone
        selectedRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_selected);
        //View for images downloaded from server
        serverRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_server);

        //Layout for gallery buttons
        buttonsGallery = (RelativeLayout) rootView.findViewById(R.id.buttons_gallery);

        //Upload to gallery button
        buttonUpload = (Button) rootView.findViewById(R.id.button_upload_images);

        //Clear selection button
        clearSelection = (Button) rootView.findViewById(R.id.button_clear_selected);

        //Cancel selection button
//        cancelUpload = (Button) rootView.findViewById(R.id.button_cancel_upload);

        //RecyclerView LayoutManager for selected images
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), Constants.gridColumns);
        //RecyclerView LayoutManager for server images
        RecyclerView.LayoutManager sLayoutManager = new GridLayoutManager(getContext(), Constants.gridColumns);

        //Gallery adapter to display selected images
        selectedAdapter = new GalleryAdapter(getContext(), selectedImages);

        selectedRecyclerView.setLayoutManager(mLayoutManager);
        selectedRecyclerView.setItemAnimator(new DefaultItemAnimator());
        selectedRecyclerView.setAdapter(selectedAdapter);

        //Gallery adapter to display server images
        serverAdapter = new GalleryAdapter(getContext(), serverImages);

        serverRecyclerView.setLayoutManager(sLayoutManager);
        serverRecyclerView.setItemAnimator(new DefaultItemAnimator());
        serverRecyclerView.setAdapter(serverAdapter);

        serverRecyclerView.addOnItemTouchListener(new GalleryAdapter.RecyclerTouchListener(getContext(), serverRecyclerView, new GalleryAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("glideImages", serverImages);
                bundle.putInt("position", position);

               // add admin specific details
               bundle.putString(Constants.brandIdString, sessionManager.getPreference(SessionManager.IdKey));

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

        //Setting clickListener on upload button
        buttonUpload.setOnClickListener(this);

        //Setting clickListener cancel button
        clearSelection.setOnClickListener(this);

        return rootView;//return View
    }

    /**
     * Create ActionBar Menu for Fragment
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_brand_dash_gallery, menu);

       MenuItem upload = menu.findItem(R.id.upload_images);
       MenuItem goPremium = menu.findItem((R.id.upload_more_premium));

       // switching the upload images title
       String uploadTitle = gallerySize() > 0
           ? getResources().getString(R.string.upload_more_images)
           : getResources().getString(R.string.upload_images);

       upload.setTitle(uploadTitle);

       // hide or show menu items depending on user premium status
       if (premiumStatus != 1) {
          upload.setVisible(gallerySize() < Constants.maxUploadCount);
          goPremium.setVisible(gallerySize() >= Constants.maxUploadCount);
       }
       getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){

           //
            case R.id.home:
                return true;

            // handle logout option selected
            case R.id.brand_logout:
                // Clearing all data from Shared Preferences
                sessionManager.logoutUser();
                return true;

            // handle upload images selected option
            case R.id.upload_images:
                start();
               return true;

            // handle go premium link
           case R.id.upload_more_premium:
              OpenWebLinks.openWebPage(
                     getContext(),
                     Constants.gopremiumUrl,
                     getResources().getString(R.string.go_premium));
              return true;

            default:
               return super.onOptionsItemSelected(item);

        }
    }

    // Recommended builder
    public void start() {

       // determine the amount of selectable images depending on premium status
       int maxPick = Constants.pickLimit;
       int pickLimit;
       int pickNum;
       if (premiumStatus == 1) {
          pickLimit = Constants.pickLimit;
       }
       else {
          pickLimit = gallerySize() == 0 ? maxPick : maxPick - gallerySize();
       }

       pickNum = pickLimit >= maxPick ? maxPick : pickLimit;

       ImagePicker.create(this)
             .folderMode(true) // set folder mode (false by default)
             .folderTitle("Folder") // folder selection title
             .imageTitle("Tap to select") // image selection title
             .single() // single mode
             .multi() // multi mode (default mode)
             .limit(pickNum) // max images can be selected (999 by default)
             .showCamera(true) // show camera or not (true by default)
             .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
             .origin(images) // original selected images, used in multi mode
             .start(REQUEST_CODE_PICKER); // start image picker activity with request code
    }

   // method returns number of images in brand gallery
   private int gallerySize() {
      return serverImagesPaths.size();
   }

   // showing the floating number of images in gallery
//   private void showCount() {
//      //
//      if (gallerySize() > 0) {
//         // text for picture count
//         picCount =  premiumStatus.equals(1)
//             ? gallerySize() + " pictures"
//             : gallerySize() + " of " + Constants.maxUploadCount + " pictures";
//         pictureCount.setText(picCount);
//         pictureCount.setVisibility(View.VISIBLE);
//      }
//   }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == Constants.RESULT_OK && data != null) {
            selectedImages.clear();
//            serverImages.clear();
            serverRecyclerView.setVisibility(View.GONE);
            buttonsGallery.setVisibility(View.VISIBLE);

            images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);

           // hide the empty gallery text accordingly
           emptyGalleryView.setVisibility(images.size() > 0 ? View.GONE : View.VISIBLE);

           // loop through selected images
            for (int i = 0; i < images.size(); i++) {

                imgPath = images.get(i).getPath();
                imagePaths.put(i,images.get(i).getPath());

                GlideImage glideImage = new GlideImage();
                glideImage.setName("");
                glideImage.setSmall(imgPath);
                glideImage.setMedium(imgPath);
                glideImage.setLarge(imgPath);
                glideImage.setTimestamp("");

                selectedImages.add(glideImage);
            }
            selectedRecyclerView.setVisibility(View.VISIBLE);
            selectedAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Task to convert image to string
     */
    public String encodeImageToString(String imgPath) {
        //Bitmap to get image from gallery
        /**
         * Process sampled down image using the same dimensions for width and height values
         */
        bitmap = decodeSampledBitmapFromFile(imgPath,imageWidth,imageHeight);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Must compress the Image to reduce image size to make upload easy
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] byte_arr = stream.toByteArray();
        // Encode Image to String
        return Base64.encodeToString(byte_arr, 0);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
    /**
     * calculate the sample size of image to load
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return inSampleSize
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * AsyncTask to upload selected pictures
     */
    public class UploadToGalleryTask extends AsyncTask<String, String,HashMap<Integer,String>>{
        private final String id;
        private final String username;
        private final HashMap<Integer,String > selectedPaths;
        JSONParser jsonParser = new JSONParser();

        UploadToGalleryTask(String sId, String sUsername, HashMap<Integer,String>selectedImagesPaths){
            id = sId;
            username = sUsername;
            selectedPaths = selectedImagesPaths;
        }

        @Override
        protected void onPreExecute(){
           //
            showProgress(true);
        }

        @Override
        protected HashMap<Integer,String> doInBackground(String... args){
            try {
                //Encode selected images to bytes and add to encoded images array
                for (int i = 0; i< selectedPaths.size(); i++){
                    encodedString = encodeImageToString(selectedPaths.get(i));
                    encodedImages.put(i,encodedString);
                  }
                //Prepare data to be sent to JSONParser class for server POST
                HashMap<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("username", username);
                params.put("images", encodedImages.toString());

                JSONObject json = jsonParser.makeHttpRequest(
                        Constants.uploadToGalleryUrl, Constants.parserPost, params);

                if (json != null) {
                    //Get the authentication status or error message
                    JSONObject serverAuth = json.getJSONObject("serverAuth");
                    String auth_key = serverAuth.getString("auth_key");
//                    //If authentication is OK get user account data
                    if(auth_key.equals(Constants.fetchOK)) {
                           //Get data from the user account
                        JSONObject result = json.getJSONObject("result");
                        JSONArray brandGallery = result.getJSONArray("brandGallery");
                        for (int i = 0; i < brandGallery.length(); i++) {
                            JSONObject data = brandGallery.getJSONObject(i);
                            serverImagesPaths.put(i, data.getString("url"));
                        }
                        return serverImagesPaths;
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<Integer,String>serverImagesPaths){
            //
            showProgress(false);

            if (serverImagesPaths != null) {
                selectedImages.clear();
                serverImages.clear();
                selectedRecyclerView.setVisibility(View.GONE);
                buttonsGallery.setVisibility(View.GONE);

                for (int i = 0; i < serverImagesPaths.size(); i++) {
                    imgPath = Constants.galleryImagePath(serverImagesPaths.get(i));

                    GlideImage glideImage = new GlideImage();
                    glideImage.setName("");
                    glideImage.setSmall(imgPath);
                    glideImage.setMedium(imgPath);
                    glideImage.setLarge(imgPath);
                    glideImage.setTimestamp("");

                    serverImages.add(glideImage);
//                    doGlide(serverImages);
                }
                serverRecyclerView.setVisibility(View.VISIBLE);
                serverAdapter.notifyDataSetChanged();

                Toast.makeText(getContext(),"Picture(s) uploaded successfully", Toast.LENGTH_SHORT).show();
            }else {
                /**
                 Make toast indicating connection error
                 */
                Toast.makeText(getContext(), getString(R.string.error_dataConnection), Toast.LENGTH_SHORT).show();
            }
        }
    }
/**
     * AsyncTask to retrieve brand gallery
     */
    public class RetrieveGalleryTask extends AsyncTask<String, String,HashMap<Integer,String>>{
        private final String id;
        JSONParser jsonParser = new JSONParser();

    RetrieveGalleryTask(String sId){
            id = sId;
        }

        @Override
        protected void onPreExecute(){
            showProgress(true);
           emptyGalleryView.setVisibility(View.GONE);
        }

        @Override
        protected HashMap<Integer,String> doInBackground(String... args){
            try {
                //Prepare data to be sent to JSONParser class for server POST
                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.IdString, id);

                JSONObject json = jsonParser.makeHttpRequest(
                        Constants.getGalleryUrl, Constants.parserPost, params);

                if (json != null) {
                    //Get the authentication status or error message
                    JSONObject serverAuth = json.getJSONObject("serverAuth");
                    String auth_key = serverAuth.getString("auth_key");
//                    //If authentication is OK get user account data
                    if(auth_key.equals(Constants.fetchOK)) {
                           //Get data from the user account
                        JSONObject result = json.getJSONObject("result");
                        JSONArray brandGallery = result.getJSONArray("brandGallery");
                        for (int i = 0; i < brandGallery.length(); i++) {
                            JSONObject data = brandGallery.getJSONObject(i);
                            serverImagesPaths.put(i, data.getString("url"));
                        }
                        return serverImagesPaths;
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<Integer,String> dImages){

            //
            showProgress(false);

            if (dImages != null) {

               // clear recycler views
                selectedImages.clear();
                serverImages.clear();

               // hide ui elements
                selectedRecyclerView.setVisibility(View.GONE);
                buttonsGallery.setVisibility(View.GONE);
                emptyGalleryView.setVisibility(View.GONE);

               // refresh recycler view images
               for (int i = 0; i < dImages.size(); i++) {
                    imgPath = Constants.galleryImagePath(dImages.get(i));

                    GlideImage glideImage = new GlideImage();
                    glideImage.setName("");
                    glideImage.setSmall(imgPath);
                    glideImage.setMedium(imgPath);
                    glideImage.setLarge(imgPath);
                    glideImage.setTimestamp("");

                    serverImages.add(glideImage);
                }
                serverRecyclerView.setVisibility(View.VISIBLE);
                serverAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View v) {
        //Upload button action
        if (v == buttonUpload) {
            new UploadToGalleryTask(id, username,imagePaths).execute();
        }

        if (v == clearSelection) {
            selectedImages.clear();
            buttonsGallery.setVisibility(View.GONE);
            selectedRecyclerView.setVisibility(View.GONE);
            serverRecyclerView.setVisibility(View.VISIBLE);
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
            try {
                int shortAnimTime = getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);

                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
                mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
           mProgressView.setBackgroundColor(getResources().getColor(R.color.colorBackground));
           mProgressView.setPadding(10, 10, 10, 10);
           mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}