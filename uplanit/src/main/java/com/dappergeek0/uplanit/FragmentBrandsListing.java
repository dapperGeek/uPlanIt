package com.dappergeek0.uplanit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class FragmentBrandsListing extends Fragment {

    private GetBrandsListingTask getBrandsListingTask = null;

   // Argument sent from category fragment
    private Integer argsCategoryId = 0;
    private String argsCategory;

   // star ratings resource
    private TypedArray ratingStars;

    ArrayList<HashMap<String, String>> brandsArrayList = new ArrayList<>();

   // views
    View rootView;
    View mProgressView;
    TextView mProgressString, emptyTextView;
    Button reloadButton;

    ImageView connErrorImage;
    TextView connErrorText;

   // Screen title variable
    String screenTitle;

    SwipeRefreshLayout swipeContainer;
    BrandsAdapter brandsAdapter;

    SpacesItemDecoration decoration;

   // Search parameters HashMap
   HashMap<String, String> searchParams = new HashMap<>();

   // Variables for search parameters
   String qKeyword, qLocation, qCatId;

    public FragmentBrandsListing(){
        // Empty constructor required for fragment subclasses
    }

    public static FragmentBrandsListing newInstance(Integer categoryId, String category) {
        FragmentBrandsListing fragmentBrandsListing = new FragmentBrandsListing();
        Bundle args = new Bundle();
        args.putInt(Constants.categoryId, categoryId);
        args.putString(Constants.categoryString, category);
        fragmentBrandsListing.setArguments(args);
        return fragmentBrandsListing;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_brands_listing, container, false);

        // Loading progress views
        mProgressView = rootView.findViewById(R.id.app_progress_bar);
        mProgressString =(TextView) rootView.findViewById(R.id.progress_loading_string);

        // Layout elements for connection error
        connErrorImage = (ImageView) rootView.findViewById(R.id.conn_error_image);
        connErrorText = (TextView) rootView.findViewById(R.id.conn_error_text);

       // Empty search result views
       emptyTextView = (TextView) rootView.findViewById(R.id.empty_result);
       reloadButton = (Button) rootView.findViewById(R.id.reload_button);

       ratingStars = getActivity().getResources().obtainTypedArray(R.array.rating_stars);

       searchParams = null;

       // Get search parameters if search is initiated
       try {
          searchParams = (HashMap<String, String>) getArguments().getSerializable(Constants.searchString);
       }
       catch (NullPointerException e){
          e.printStackTrace();
       }

       if (searchParams != null) {

          // Get search parameter from the passed argument
          qKeyword = searchParams.get(Constants.SEARCH_KEYWORD_STRING);
          qLocation = searchParams.get(Constants.locationString);
          qCatId = searchParams.get(Constants.categoryId);

          // Assign search title when for search screen
          screenTitle = "Search for: " + qKeyword ;

       }
       else {

        /*Assign value null or integer to selectedCategoryId depending if
         *  argument is supplied to fragment
         */
          argsCategoryId = getArguments() == null ? 0 : getArguments().getInt(Constants.categoryId);

          //Show title as appropriate
          screenTitle = argsCategory = getArguments() == null ? getString(R.string.title_activity_featured) : getArguments().getString(Constants.categoryString);
       }
        /*
         * set the AppBar title
         */
        getActivity().setTitle(screenTitle);
        setHasOptionsMenu(true);

        //Execute task to show brands listing
        getBrandsListingTask = searchParams == null ?
            new GetBrandsListingTask(argsCategoryId) :
            new GetBrandsListingTask(qKeyword, qLocation, qCatId);
        getBrandsListingTask.execute();

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide the connection error views
                connErrorText.setVisibility(View.GONE);
                connErrorImage.setVisibility(View.GONE);
                if (brandsAdapter != null) {
                    //clear brand listing in adapter memory
                    brandsAdapter.clear();
                }
                //Execute task to show brands listing on refresh
               getBrandsListingTask = searchParams == null ?
                   new GetBrandsListingTask(argsCategoryId) :
                   new GetBrandsListingTask(qKeyword, qLocation, qCatId);
                getBrandsListingTask.execute();
//                brandsAdapter.addAll(brandsArrayList);

                // setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        return rootView;
    }

    // Create ActionBar Menu for Fragment
    @Override
    public void onCreateOptionsMenu(
        Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.featured, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks
        switch (item.getItemId()){
          // Handle the search action
            case R.id.action_search:
             Intent searchBrands = new Intent(getContext(), MainSearch.class);
             startActivity(searchBrands);
             return true;

            default:
               return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public class GetBrandsListingTask extends AsyncTask<String, String, ArrayList>{

       private String sKeyword;
       private String sLocation;
       private String sCategory;

        JSONParser jsonParser = new JSONParser();

        private String  mainId;

       // Constructor for default or category brands listing
        GetBrandsListingTask(Integer selectedId){
            this.mainId = selectedId.toString();
        }

       // Constructor when search is initiated
       GetBrandsListingTask(String keyword, String sLocation, String sCategory){
          this.sKeyword = keyword;
          this.sLocation = sLocation;
          this.sCategory = sCategory;
       }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        protected ArrayList<HashMap<String,String>> doInBackground(String... args) {
            try {
                HashMap<String, String> params = new HashMap<>();

               // Default brands listing parameters
               if (searchParams == null){
                  params.put("query", "brands");
                  params.put("content", "featured");
                  params.put("selectedId", mainId);
               }

               // Brand search parameters
               else{
                  params.put(Constants.SEARCH_KEYWORD_STRING, sKeyword);
                  params.put(Constants.locationString, sLocation);
                  params.put(Constants.categoryId, sCategory);
               }

                // Fetch brands list as JSONObject from url
                JSONObject json = jsonParser.makeHttpRequest(Constants.brandsListingUrl, Constants.parserPost, params);

               // Process server response
                if (json != null) {
                    //Get the authentication status or error message
                    JSONObject serverAuth = json.getJSONObject("serverAuth");
                    String auth_key = serverAuth.getString("auth_key");
                    //If authentication is OK get user account data
                    if(auth_key.equals(Constants.fetchOK)){

                        //Get data from the user account
                        JSONObject result = json.getJSONObject("result");
                        JSONArray brandsListing = result.getJSONArray("BRANDS_LISTING");

                        for (int i = 0; i < brandsListing.length(); i++){
                            JSONObject feats = brandsListing.getJSONObject(i);
                            String id = feats.getString(Constants.IdString);
                            String categoryId = feats.getString(Constants.categoryIdColumnString);
                            String logo = feats.getString(Constants.logoString);
                            String rating = feats.getString(Constants.ratingString);
                            String phone = feats.getString(Constants.phoneString);
                            String address = feats.getString(Constants.addressString);
                            String name = feats.getString(Constants.nameString);
                            String description = feats.getString(Constants.descString);
                           String vPhone = feats.getString(Constants.vPhone);
                           String vLocation = feats.getString(Constants.vLocation);

                            // Declare new HashMap
                            HashMap<String, String> featured = new HashMap<>();

                            featured.put(Constants.IdString, id);
                            featured.put(Constants.categoryId, categoryId);
                            featured.put(Constants.logoString, logo);
                            featured.put(Constants.ratingString, rating);
                            featured.put(Constants.vPhone, vPhone);
                            featured.put(Constants.vLocation, vLocation);
                            featured.put(Constants.phoneString, phone);
                            featured.put(Constants.addressString, StringHandler.replaceComma(address));
                            featured.put(Constants.brandNameString, name);
                            featured.put(Constants.descString, StringHandler.replaceComma(description));
                            brandsArrayList.add(featured);
                        }
                    }
                    return brandsArrayList;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList brandsListing) {
            getBrandsListingTask = null;
            showProgress(false);

            if (brandsListing != null) {

               // Show brands listing if results is not empty
                if (!brandsListing.isEmpty()) {
                    try {

                       // Lookup the recyclerView in activity layout
                       RecyclerView rvBrands = (RecyclerView) rootView.findViewById(R.id.rvBrands);
                       // Add decoration (Divider)
                       RecyclerView.ItemDecoration itemDecoration = new
                           DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                       rvBrands.addItemDecoration(itemDecoration);
                       // Add animator
                       rvBrands.setItemAnimator(new SlideInUpAnimator());
                       decoration = decoration == null ? new SpacesItemDecoration(16) : new SpacesItemDecoration(0);
                       rvBrands.addItemDecoration(decoration);

                       // Implement ItemClickSupport class for onItemClick in recyclerView item
                       ItemClickSupport.addTo(rvBrands).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                          @Override
                          public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                             // open brandActivity on item click
                             String feats = brandsListing.get(position).toString();
                             Intent intent = new Intent(getContext(), BrandActivity.class);
                             intent.putExtra(Constants.intentExtra,feats);
                             startActivity(intent);
                          }
                       });
                       // Create adapter passing in the sample user data
                       brandsAdapter = new BrandsAdapter(getContext(), brandsListing);
                       // Attach the adapter to the recyclerView to populate items
                       rvBrands.setAdapter(brandsAdapter);
                       // Set layout manager to position the items
                       rvBrands.setLayoutManager(new LinearLayoutManager(getContext()));

                    } catch (Exception e) {
                       e.printStackTrace();
                    }
                }

                // Show page reload if listing is empty
               else {

                   try {

                      //
                      String display = searchParams == null
                          ? argsCategory + " " + Constants.categoryString
                          : qKeyword;

                      // Concatenate empty result text with the search keyword parameter or category
                      String empty_result = getResources().getString(R.string.empty_search) + display;

                      // Make the reload button and text visible
                      emptyTextView.setVisibility(View.VISIBLE);
                      emptyTextView.setText(empty_result);
                      reloadButton.setVisibility(View.VISIBLE);

                      // Reload button onClickListener
                      reloadButton.setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View v) {
                            // nullify search parameter
                            searchParams = null;

                            // Hide the no result found views
                            reloadButton.setVisibility(View.INVISIBLE);
                            emptyTextView.setVisibility(View.INVISIBLE);

                            // Reset screen title to default
                            getActivity().setTitle(R.string.title_activity_featured);

                            // implement the default brands listing task
                            getBrandsListingTask = new GetBrandsListingTask(0);
                            getBrandsListingTask.execute();
                         }
                      });
                   }catch (IllegalStateException e) {
                      e.printStackTrace();
                   }

                }
            }
            else{
                try {
                   // Set view for network error
                   connErrorImage.setVisibility(View.VISIBLE);
                   connErrorText.setText(getResources().getString(R.string.swipe_to_refresh_page));
                   connErrorText.setVisibility(View.VISIBLE);
                }catch (IllegalStateException e){
                   e.printStackTrace();
                }
            }
        }

        @Override
        protected void onCancelled() {
            getBrandsListingTask = null;
        }
    }

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
    public class BrandsAdapter extends
        RecyclerView.Adapter<BrandsAdapter.ViewHolder> {

        // Store a member variable for the contacts
        private ArrayList<String> mBrandsList;
        // Store the context for easy access
        private Context mContext;

        // Pass in the contact array into the constructor
    public BrandsAdapter(Context context, ArrayList<String> brandsList) {
            mBrandsList = brandsList;
            mContext = context;
        }

        // Easy access to the context object in the recyclerView
        private Context getContext() {
            return mContext;
        }

        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public class ViewHolder extends RecyclerView.ViewHolder{
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView brandName, brandDescription, listCategory;
            public ImageView listImage, listRating;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                listImage = (ImageView) itemView.findViewById(R.id.list_image);
                brandName = (TextView) itemView.findViewById(R.id.brand_name);
                listRating = (ImageView) itemView.findViewById(R.id.list_rating);
                listCategory = (TextView) itemView.findViewById(R.id.list_category);
                brandDescription = (TextView) itemView.findViewById(R.id.description);
            }
        }

        @Override
        public BrandsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View brandView = inflater.inflate(R.layout.brands_list_row, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(brandView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(BrandsAdapter.ViewHolder viewHolder, int position) {
            // Get the data model based on position
//            Contact contact = mContacts.get(position);

            ImageView listImage = viewHolder.listImage;
            TextView brandName = viewHolder.brandName;
            TextView listCategory = viewHolder.listCategory;
            TextView brandDescription = viewHolder.brandDescription;
            ImageView listRating = viewHolder.listRating;

            // get the url from the data you passed to the `Map`
            String logo_url = Constants.LogoPath(brandsArrayList.get(position).get(Constants.logoString));

           //
           String rating = brandsArrayList.get(position).get(Constants.ratingString) ;

           //
           int ratingIcon = ratingStars.getResourceId(Integer.parseInt(rating),-1);

           // shorten the description text
            String bDescription = StringHandler.revertComma(StringHandler.shortenString(brandsArrayList.get(position).get(Constants.descString)));

           // brand name
            String bName = brandsArrayList.get(position).get(Constants.brandNameString);

           // category id
           String categoryId = brandsArrayList.get(position).get(Constants.categoryId);

           String[] categories = getResources().getStringArray(R.array.categories);

           // category
           String bCategory = StringHandler.brandCategory(categoryId,categories);

            // set brand name, category and description
            brandDescription.setText(bDescription);
            brandName.setText(bName);
            listRating.setImageResource(ratingIcon);

           // do not show the brand list row category has ALREADY been selected
           if (argsCategoryId == 0 || searchParams != null) {
              listCategory.setText(bCategory);
           }

            // set brand logo, check if null
            if(!logo_url.isEmpty()){
                Picasso.with(getContext())
                    .load(logo_url)
                    .placeholder(R.drawable.ic_no_logo)
                    .transform(new CropCircleTransformation())
                    .into(listImage);
            }
        }

        @Override
        public int getItemCount() {
            return mBrandsList.size();
        }

        // Clean all elements of the recycler
        public void clear() {
            mBrandsList.clear();
            notifyDataSetChanged();
        }

        // Add a list of items
        public void addAll(ArrayList<String> list) {
            mBrandsList.addAll(list);
            notifyDataSetChanged();
        }
    }
/*
Decorator which adds spacing around the tiles in a Grid layout RecyclerView. Apply to a RecyclerView with:
Feel free to add any value you wish for SpacesItemDecoration. That value determines the amount of spacing.
Source: http://blog.grafixartist.com/pinterest-masonry-layout-staggered-grid/
*/

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;
        public SpacesItemDecoration(int space) {
            this.mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//            outRect.left = mSpace;
//            outRect.right = mSpace;
            if (outRect.bottom < 1 ) {
                outRect.bottom = mSpace;
            }
            // Add top margin only for the first item to avoid double space between items
//            if (parent.getChildAdapterPosition(view) == 0)
//                outRect.top = mSpace;
        }
    }

    // Shows the progress UI
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
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}