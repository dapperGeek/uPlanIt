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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dappergeek0.uplanit.dialogs.DialogActionClear;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class FragmentFavorites extends Fragment {

   // Argument sent from category fragment
   ArrayList<HashMap<String, String>> brandsArrayList = new ArrayList<>();

   // variables
   String id, type;

   // star ratings resource
   private TypedArray ratingStars;

   // Declare Views
   View rootView;
   View mProgressView;
   TextView mProgressString;
   RelativeLayout emptyFavView;
   Button reloadButton;
   ImageView connErrorImage;
   TextView connErrorText;

   //
   DialogActionClear.FragmentReloadListener reloadListener;

   // classes
   GetFavorites getFavorites = null;
   SessionManager sessionManager;
   DatabaseHandler databaseHandler;
   FavoritesHandlerClass favoritesHandlerClass;
   SwipeRefreshLayout swipeContainer;
   SpacesItemDecoration decoration;
   BrandsAdapter brandsAdapter;

   public FragmentFavorites() {
      // Required empty public constructor
   }

   /**
    * Use this factory method to create a new instance of
    * this fragment using the provided parameters.
    *
    * @return A new instance of fragment FragmentFavorites.
    */
   public static FragmentFavorites newInstance() {
      return new FragmentFavorites();
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      // Inflate the layout for this fragment
      rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

      // Assign classes
      sessionManager = new SessionManager(getContext());
      databaseHandler = new DatabaseHandler(getContext());
      favoritesHandlerClass = new FavoritesHandlerClass(getContext());

      // Loading progress views
      mProgressView = rootView.findViewById(R.id.app_progress_bar);
      mProgressString =(TextView) rootView.findViewById(R.id.progress_loading_string);

      // Layout elements for connection error
      connErrorImage = (ImageView) rootView.findViewById(R.id.conn_error_image);
      connErrorText = (TextView) rootView.findViewById(R.id.conn_error_text);

      // Empty search result views
      emptyFavView = (RelativeLayout) rootView.findViewById(R.id.empty_fav_view);
      reloadButton = (Button) rootView.findViewById(R.id.reload_button);

      // Lookup the swipe container view
      swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);

      ratingStars = getActivity().getResources().obtainTypedArray(R.array.rating_stars);

        /*
         * set the AppBar title
         */
      getActivity().setTitle(getResources().getString(R.string.nav_favorites));

      this.id = sessionManager.isLoggedIn()
          ? sessionManager.getPreference(SessionManager.IdKey)
          : String.valueOf(0);
      this.type = sessionManager.isLoggedIn()
          ? sessionManager.getPreference(SessionManager.accountType)
          : "";

      // get if any favorite is saved before making db query
      if (favoritesHandlerClass.countFavorites() > 0){

         // show favorites
         showFavorites();
      }
      else {
         // show empty favorites View
         emptyFavView.setVisibility(View.VISIBLE);
      }

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
            showFavorites();

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

      // set options menu
      setHasOptionsMenu(true);
      getActivity().invalidateOptionsMenu();

      // return view
      return rootView;
   }

   /**
    * Create ActionBar Menu for Fragment
    */
   @Override
   public void onCreateOptionsMenu(
       Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.favorites, menu);

      // show or hide the clear button depending if favorites exist
      MenuItem clearFavBtn = menu.findItem(R.id.action_clear);
      clearFavBtn.setVisible(favoritesHandlerClass.countFavorites() > 0);
   }

   // method displays user favorites
   private void showFavorites(){
      getFavorites = new GetFavorites();
      getFavorites.execute();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      // Handle action bar item clicks
      switch (item.getItemId()){

         // Handle the clear favorites action
         case R.id.action_clear:
            DialogFragment dialogFragment = DialogActionClear.newInstance(Constants.favoritesFragtag);
            String dialogTag = Constants.clearTaskDialog;
            showDialogFragment(dialogFragment, dialogTag);
            return true;

         default:
            return super.onOptionsItemSelected(item);
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
         mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
         mProgressString.setVisibility(show ? View.VISIBLE : View.GONE);
      }
   }

   /**
    *  Asyntask getFavorites
    */
   public class GetFavorites extends AsyncTask<String , String, ArrayList<HashMap<String,String>>>{

      private String id;
      private String type;
      Integer[] brandIds;
      String IdsString = "";
      JSONParser jsonParser = new JSONParser();

      GetFavorites(){
         this.id = sessionManager.isLoggedIn()
                     ? sessionManager.getPreference(SessionManager.IdKey)
                     : String.valueOf(0);
         this.type = sessionManager.isLoggedIn()
                     ? sessionManager.getPreference(SessionManager.accountType)
                     : "";
      }

      /**  */
      @Override
      protected void onPreExecute() {
         super.onPreExecute();
         showProgress(true);
      }

      /**  */
      @Override
      protected ArrayList<HashMap<String,String>> doInBackground(String ... args){

         try {
            HashMap<String, String> params = new HashMap<>();

            // Query SQLite for favorites stored
            brandIds = databaseHandler.getFavorites(Integer.parseInt(id), type);

            // change the returned Integer array into a comma separated string
               if (brandIds != null){

                  for (int i = 0; i < brandIds.length; i++) {

                     if (i == brandIds.length - 1) {
                        IdsString += brandIds[i];
                     } else
                        IdsString += brandIds[i] + ",";
                  }
               }

            params.put(Constants.modeString, Constants.modeRead);
            params.put(Constants.IdString, id);
            params.put(Constants.accTypeString, type);
            params.put(Constants.brandIdString, IdsString);

            JSONObject json = jsonParser.makeHttpRequest(Constants.favoritesUrl, Constants.parserPost, params);
//
            if (json != null) {
               //Get the authentication status or error message
               JSONObject statusKey = json.getJSONObject(Constants.statusKey);
               String status = statusKey.getString(Constants.statusKey);
               //If authentication is OK get user account data
               if(status.equals(Constants.statusOk)){

                  //Get data from the user account
                  JSONObject result = json.getJSONObject(Constants.jsonResponse);
                  JSONArray brandsListing = result.getJSONArray(Constants.jsonListing);

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
         }
         catch (Exception e){
            e.printStackTrace();
         }
         return null;
      }

      @Override
      protected void onPostExecute(final ArrayList brandsListing) {
         getFavorites = null;
         showProgress(false);

         if (brandsListing != null) {

            // show brands listing if results is not empty
            if (!brandsListing.isEmpty()) {
               try {

               // Lookup the recyclerView in activity layout
               RecyclerView rvFavorites = (RecyclerView) rootView.findViewById(R.id.rvFavorites);
               // Add decoration (Divider)
               RecyclerView.ItemDecoration itemDecoration = new
                   DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
                  rvFavorites.addItemDecoration(itemDecoration);
               // Add animator
               rvFavorites.setItemAnimator(new SlideInUpAnimator());
               decoration = decoration == null ? new SpacesItemDecoration(16) : new SpacesItemDecoration(0);
               rvFavorites.addItemDecoration(decoration);

               // implement ItemClickSupport class for onItemClick in recyclerView item
               ItemClickSupport.addTo(rvFavorites).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                  @Override
                  public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                     // open brandActivity on item click
                     String fav = brandsListing.get(position).toString();
                     Intent intent = new Intent(getContext(), BrandActivity.class);
                     intent.putExtra(Constants.intentExtra, fav);
                     startActivityForResult(intent, Constants.REQUEST_CODE_PICKER);
                  }
               });
               // Create adapter passing in the sample user data
               brandsAdapter = new BrandsAdapter(getContext(), brandsListing);
               // Attach the adapter to the recyclerView to populate items
               rvFavorites.setAdapter(brandsAdapter);
               // Set layout manager to position the items
               rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }

            // Show page reload if listing is empty due to server timeout
            else {
               try {

               reloadButton.setVisibility(View.VISIBLE);
               // Reload button onClickListener
               reloadButton.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                     // nullify search parameter

                     // Hide the no result found views
                     reloadButton.setVisibility(View.INVISIBLE);
                     emptyFavView.setVisibility(View.INVISIBLE);

                     // Implement the default favorites listing task
                     showFavorites();
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
               connErrorText.setText(getResources().getString(R.string.fav_not_found) + "\n    " + getResources().getString(R.string.swipe_to_refresh_page));
               connErrorText.setVisibility(View.VISIBLE);
            }catch (IllegalStateException e){
               e.printStackTrace();
            }
         }
      }
   }

  /* Decorator which adds spacing around the tiles in a Grid layout RecyclerView. Apply to a RecyclerView with:
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
         listCategory.setText(bCategory);


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

   public void showDialogFragment(DialogFragment dialogFragment, String fragmentTag ){
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
      dialogFragment.show(ft, fragmentTag);
   }

   @Override
   public void onAttach(Context context){
      super.onAttach(context);

      try {
         reloadListener = (DialogActionClear.FragmentReloadListener) getActivity();
      }catch (ClassCastException e){
         throw new ClassCastException(context.toString() + " must implement");
      }
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data){
      super.onActivityResult(requestCode, resultCode, data);
      if ((requestCode == Constants.REQUEST_CODE_PICKER)){
         Fragment fragment = FragmentFavorites.newInstance();
         String fragmentTag = Constants.favoritesFragtag;
         reloadListener.FragmentReload(fragment, fragmentTag);
      }
   }
}
