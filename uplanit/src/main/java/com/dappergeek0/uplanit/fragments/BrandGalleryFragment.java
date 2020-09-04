package com.dappergeek0.uplanit.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dappergeek0.uplanit.Constants;
import com.dappergeek0.uplanit.JSONParser;
import com.dappergeek0.uplanit.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import info.androidhive.glide.activity.SlideshowDialogFragment;
import info.androidhive.glide.adapter.GalleryAdapter;
import info.androidhive.glide.model.GlideImage;


public class BrandGalleryFragment extends Fragment{

    private static final String ARGUMENT_EXTRAS = "extra_string";
    private String stringExtra;

    //    Arrays and Lists
    private ArrayList<GlideImage> serverImages = new ArrayList<>();
    private HashMap<Integer,String> serverImagesPaths = new HashMap<>();
    private ArrayList<HashMap<Integer,String>> fetchDetails = new ArrayList<>();

    private RelativeLayout emptyGalleryView;
    private ProgressBar mProgressView;
    private View rootView;

    private GalleryAdapter serverAdapter;
    private RecyclerView serverRecyclerView;
    //Create instance of base64 image encoder class
    private String imgPath, id;

    public BrandGalleryFragment() {
        // Required empty public constructor
    }

    public static BrandGalleryFragment newInstance(int page, String title, String extra_string){
        BrandGalleryFragment fragment = new BrandGalleryFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_EXTRAS,extra_string);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_brand_gallery, container, false);
        // Layout UI components
        emptyGalleryView = (RelativeLayout) rootView.findViewById(R.id.empty_gallery_view);

        //Get data from the intent EXTRA_MESSAGE
        stringExtra = getArguments().getString(ARGUMENT_EXTRAS);
        stringExtra = stringExtra.replaceAll("[{}]","");
        String[] keyValuePairs = stringExtra.split(",") ;
        Map<String,String> map = new HashMap<>();
        for(String pair : keyValuePairs){
            try {
                String[] entry = pair.split("=");
                map.put(entry[0].trim(),entry[1].trim());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Brand id from map values
        id = map.get(Constants.IdString);

        // Fetch brand galley by brand id
        new RetrieveGalleryTask(id).execute();

        // assign views
        mProgressView = (ProgressBar) rootView.findViewById(R.id.progress_spinner);
        serverRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_server);
        //RecyclerView LayoutManager for server images
        RecyclerView.LayoutManager sLayoutManager = new GridLayoutManager(getContext(), Constants.gridColumns);
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

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                SlideshowDialogFragment newFragment = SlideshowDialogFragment.newInstance();
                newFragment.setArguments(bundle);
                newFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));

         return rootView;
    }

    /**
     * AsyncTask to retrieve brand gallery
     */
    public class RetrieveGalleryTask extends AsyncTask<String, String,ArrayList<HashMap<Integer,String>>> {
        private final String id;
        JSONParser jsonParser = new JSONParser();

        RetrieveGalleryTask(String sId){
            id = sId;
        }

        @Override
        protected void onPreExecute(){
            // show progress animation
            showProgress(true);
        }

        @Override
        protected ArrayList<HashMap<Integer,String>> doInBackground(String... args){
            try {
                //Prepare data to be sent to JSONParser class for server POST
                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.IdString, id);

                JSONObject json = jsonParser.makeHttpRequest(
                        Constants.getGalleryUrl, Constants.parserPost, params);

                if (json != null) {
                    //Get the authentication status or error message
                    JSONObject serverAuth = json.getJSONObject(Constants.serverAuthKey);
                    String auth_key = serverAuth.getString(Constants.authenticationKey);
//                    //If authentication is OK get user account data
                    if(auth_key.equals(Constants.fetchOK)) {
                        HashMap<Integer,String> fetchStatus = new HashMap<>();
                        fetchStatus.put(Constants.FETCH_STATUS_POS,auth_key);
                        fetchDetails.add(fetchStatus);
                        //Get data from the user account
                        JSONObject result = json.getJSONObject(Constants.jsonResult);
                        JSONArray brandGallery = result.getJSONArray("brandGallery");
                        for (int i = 0; i < brandGallery.length(); i++) {
                            JSONObject data = brandGallery.getJSONObject(i);
                            serverImagesPaths.put(i, data.getString("url"));
                        }
                        fetchDetails.add(serverImagesPaths);
                        return fetchDetails;
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<Integer,String>> fetchDetails){
            // hide progress animation
            showProgress(false);

            if (fetchDetails != null) {

                String fetchStatus = fetchDetails.get(Constants.FETCH_STATUS_POS).get(Constants.FETCH_STATUS_POS);

                switch (fetchStatus) {
                    case Constants.fetchOK:
                        serverImagesPaths = fetchDetails.get(Constants.GALLERY_POS);

                        for (int i = 0; i < serverImagesPaths.size(); i++) {
                            imgPath = Constants.galleryImagePath(serverImagesPaths.get(i));

                            GlideImage glideImage = new GlideImage();
                            glideImage.setName("");
                            glideImage.setSmall(imgPath);
                            glideImage.setMedium(imgPath);
                            glideImage.setLarge(imgPath);
                            glideImage.setTimestamp("");

                            serverImages.add(glideImage);
//                          doGlide(serverImages);
                        }
                        serverRecyclerView.setVisibility(View.VISIBLE);
                        serverAdapter.notifyDataSetChanged();
                        break;

                    case Constants.fetchNone:
                        // Show empty gallery view
                        emptyGalleryView.setVisibility(View.VISIBLE);
                        break;
                }
            }
            else {
                // Show empty gallery view
                emptyGalleryView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Shows the progress UI
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            try {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

               try {
                   mProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
                   mProgressView.animate().setDuration(shortAnimTime).alpha(
                       show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                       @Override
                       public void onAnimationEnd(Animator animation) {
                           mProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
                       }
                   });

                   mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                   mProgressView.animate().setDuration(shortAnimTime).alpha(
                       show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                       @Override
                       public void onAnimationEnd(Animator animation) {
                           mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                       }
                   });
               }
               catch (NullPointerException e){
                   e.printStackTrace();
               }
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}