package com.dappergeek0.uplanit;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dappergeek0.uplanit.dialogs.DialogActionClear;
import com.dappergeek0.uplanit.dialogs.FragmentUpdateBrandInfo;
import com.dappergeek0.uplanit.fragments.*;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener, FragmentCategories.OnListItemSelectedListener, FragmentLogin.OnLoginSuccessListener, FragmentLogin.ButtonToRegisterListener, FragmentUserRegistration.ButtonToLoginListener, FragmentUpdateBrandInfo.ReloadBrandDetailsListener, DialogActionClear.FragmentReloadListener {

	SessionManager sessionManager;
	String userId;
  Integer favsCount = 0;

	HashMap<String,String> loginDetails = new HashMap<>();
	String accountType = null;
	NavigationView navigationView;
	View headerView;
	// Insert the fragment by replacing any existing fragment
	FragmentManager fragmentManager = getSupportFragmentManager();
	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

	private Fragment currentFragment;
	FloatingActionButton fab;
	TextView loginStatusView;
  ImageView navLogo;

	// classes
	FavoritesHandlerClass favoritesHandlerClass;
	DatabaseHandler databaseHandler;

	// Intent extra messages
	HashMap<String, String> searchParams = new HashMap<>(); // search intent
  String showEvent = null;

	Bundle args = new Bundle();

	// HashMap to contain array from BrandsCategoriesTask
	HashMap<Integer, String> brandsCategories = new HashMap<>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_);

		// Current Fragment visible in activity
		currentFragment = fragmentManager.findFragmentById(R.id.content_frame);

     // get search parameters if search is done
		try {
			searchParams = (HashMap<String, String>) getIntent().getSerializableExtra(Constants.searchString);
		}catch (NullPointerException e){
			e.printStackTrace();
		}

     //
     try {
        showEvent =  getIntent().getStringExtra(Constants.showEvents);
     }catch (NullPointerException e){
        e.printStackTrace();
     }

		// Navigation drawer View
		navigationView = (NavigationView) findViewById(R.id.nav_view);

		//Navigation header View
		headerView = navigationView.getHeaderView(0);

		// New SessionManager class to handle session operations
		sessionManager  = new SessionManager(getApplicationContext());

		accountType = sessionManager.isLoggedIn()?sessionManager.getPreference(SessionManager.accountType):null;

		userId = sessionManager.isLoggedIn()?sessionManager.getPreference(SessionManager.IdKey):null;

		if (savedInstanceState == null && searchParams == null && showEvent == null) {
				LoadFragment(new FragmentBrandsListing(), Constants.mainFragTag, Constants.navMainPosition);
		}

     // if search is active
		if (searchParams != null) {
       Fragment searchFragment = new FragmentBrandsListing();

       args.putSerializable(Constants.searchString, searchParams);
       searchFragment.setArguments(args);
       // Set the corresponding nav drawer position as checked
       setNavigation(0);

       // add fragment to the fragment_container view
       fragmentTransaction.replace(R.id.content_frame, searchFragment, Constants.searchString);
       fragmentTransaction.commit();
		}

    // if showEvents active
     if (showEvent != null){
        Fragment userEvents = new UserEventsFragment();

        // Set the corresponding nav drawer position as checked
        setNavigation(Constants.navEventPosition);

        // add fragment to the fragment_container view
        fragmentTransaction.add(R.id.content_frame, userEvents, Constants.showEvents);
        fragmentTransaction.addToBackStack(Constants.showEvents);
        fragmentTransaction.commit();
     }

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Logged in status view
		loginStatusView = (TextView) headerView.findViewById(R.id.login_status);
		navLogo = (ImageView) headerView.findViewById(R.id.app_logo);

		fab = (FloatingActionButton) findViewById(R.id.fab);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Handle FloatingActionButton action
				if (sessionManager.isLoggedIn()){
					planNewEvent();
				}
				else {
					Toast.makeText(getApplicationContext(),R.string.prompt_create,Toast.LENGTH_SHORT).show();
					AddFragment(new FragmentLogin(),Constants.loginFragTag);
				}
			}
		});

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		  navigationView.setNavigationItemSelectedListener(this);

		// assign classes
     favoritesHandlerClass = new FavoritesHandlerClass(getApplicationContext());
		databaseHandler = new DatabaseHandler(getApplicationContext());

		try {
			favsCount = favoritesHandlerClass.countFavorites();
		} catch (Exception e){
			e.printStackTrace();
		}

		/** Get array of category IDs from brands DB */
		new BrandCategoriesTask().execute();
		showNavigation();
	}

	@Override
	protected void onResume(){
		showNavigation();
				super.onResume();
	}

	// Launches the Plan New Event activity
	public void planNewEvent(){
		Intent createEvent = new Intent(getApplicationContext(),ActivityPlanEvent.class);
		createEvent.putExtra(Constants.SYNC_TYPE, Constants.SYNC_CREATE);
		startActivity(createEvent);
	}

	/*
	 * Show appropriate navigation drawer items depending on login status
	 */
	protected void showNavigation(){
		Menu menu = navigationView.getMenu();

		MenuItem nav_login = menu.findItem(R.id.nav_login);
		MenuItem nav_logout = menu.findItem(R.id.nav_logout);
		MenuItem nav_my_events = menu.findItem(R.id.nav_my_events);
		MenuItem nav_account = menu.findItem(R.id.nav_account);
		MenuItem nav_favorites = menu.findItem(R.id.nav_favorites);

   // get number of favorites and show in navigation
   favsCount = favoritesHandlerClass.countFavorites();
   String favTitle = favsCount == 0
      ? ""
      : "                                 " + favsCount;
  nav_favorites.setTitle(getResources().getString(R.string.nav_favorites) + favTitle);

		if(!sessionManager.isLoggedIn()){
			nav_login.setVisible(true);
			nav_account.setVisible(false);
			nav_my_events.setVisible(false);
			nav_logout.setVisible(false);
			String displayStatus = "Log In";
			loginStatusView.setText(displayStatus);
      navLogo.setImageResource(R.drawable.logo);

       // set onClickListener on the Log in text
      loginStatusView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            // show the login screen
            AddFragment(new FragmentLogin(), Constants.loginFragTag);

            // close the drawer
            closeDrawer();
         }
      });
		}
		else {
			// empty string placeholder for fbProfilePicture
      String fbPix = "";

      // account type
			accountType = sessionManager.getPreference(SessionManager.accountType);

      // login method
      String loginMode = sessionManager.getPreference(SessionManager.loginMode);

			String username = sessionManager.getPreference(SessionManager.usernameKey);

			String name = sessionManager.getPreference(SessionManager.nameKey);

			String displayName = (!TextUtils.isEmpty(name)) ? name : username ;

			String loginStatus = "Logged in as " + StringHandler.capWords(displayName);

       // try retrieve fbPix in sharedPrefs if available
       try {
          fbPix = sessionManager.getPreference(SessionManager.fbPicture);
       }catch (Exception e){
          e.printStackTrace();
       }
       // image to show in nav header
      String navIcon =
          // if account type is brand and registration is completed
          accountType.equals(Constants.brandString)
              && sessionManager.isProfileComplete()
              //show brand logo
          ? Constants.LogoPath(sessionManager.getPreference(SessionManager.logoKey))
              // if login mode is Fb login, show fb profile pix
          : loginMode.equals(Constants.FB_LOGIN)
          ? fbPix
              // else assign empty string
           : "";

			nav_login.setVisible(false);
			nav_account.setVisible(true);
			nav_my_events.setVisible(true);
			nav_logout.setVisible(true);

      // set text indicating login status
			loginStatusView.setText(loginStatus);

      // set Nav Image if nav icon is not empty string
       if (!navIcon.equals("")){
          Picasso.with(getApplicationContext())
              .load(navIcon)
							.placeholder(R.drawable.ic_no_logo)
              .transform(new CropCircleTransformation())
              .into(navLogo);
       }

       // set onClickListener on the navigation image to open user account
       navLogo.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
               showAccount();
             }
          });

       // set onClickListener on the login status view
       loginStatusView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
             showAccount();
          }
       });
		}
	}

   // opens the my account fragment for non navigation item click
   private void showAccount() {
      String fragmentTag = null;
      Fragment fragment = null;
      assert accountType != null;
      switch (accountType) {

         // Condition for brand user account
         case Constants.brandString:
            //Do check for incomplete brand profile
            if (sessionManager.isProfileComplete()){
               // If profile is complete, go to brand dashboard
               fragment = FragmentBrandDashboard.newInstance();
               fragmentTag = Constants.brandDashboardFragTag;
            }
            else { // Handle My account action when registration in incomplete
               fragment = FragmentCompleteBrandRegistration.newInstance();
               fragmentTag = Constants.brandUpdateFragTag;
            }
            break;
         // Condition for base user account
         case Constants.patronString:
            fragment = FragmentUserDashboard.newInstance();
            fragmentTag = Constants.patronDashboardFragTag;
            break;
      }

      // open account fragment
      AddFragment(fragment,fragmentTag);

      // close drawer
      closeDrawer();
   }

   // closes the navigation drawer
   private void closeDrawer(){
      // close drawer
      DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
      drawer.closeDrawer(GravityCompat.START);
   }

   // method returns account fragment and tag
   @Nullable
   private HashMap<Fragment,String> accountData() {
      //
      String fragmentTag = null;
      Fragment fragment = null;
      HashMap<Fragment,String > accountData = null;
      assert accountType != null;
      switch (accountType) {

         // Condition for brand user account
         case Constants.brandString:
            //Do check for incomplete brand profile
            if (sessionManager.isProfileComplete()){
               // If profile is complete, go to brand dashboard
               fragment = FragmentBrandDashboard.newInstance();
               fragmentTag = Constants.brandDashboardFragTag;
            }
            else { // Handle My account action when registration in incomplete
               fragment = FragmentCompleteBrandRegistration.newInstance();
               fragmentTag = Constants.brandUpdateFragTag;
            }
            break;
         // Condition for base user account
         case Constants.patronString:
            fragment = FragmentUserDashboard.newInstance();
            fragmentTag = Constants.patronDashboardFragTag;
            break;
      }

      accountData.put(fragment,fragmentTag);

      return null;
   }

	// Toast to indicate app exit after second press at default fragment
	private long mLastPress = 0;
	int TOAST_DURATION = 2000;
	Toast onBackPressedToast;

	@Override
	public void onBackPressed() {
		// Currently active fragment
		currentFragment = fragmentManager.findFragmentById(R.id.content_frame);

		//Default onBackPressed : close drawer first
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		}
		else {

			//Load the default (FragmentBrandsListing) fragment if it is NOT the visible fragment
			if (!currentFragment.getClass().equals(FragmentBrandsListing.class)){

					fragmentManager.popBackStack(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
					navigationView.getMenu().getItem(0).setChecked(true);
					setTitle(R.string.title_activity_featured);
					showNavigation();
					fab.setVisibility(View.VISIBLE);

         // if new event has just been added, reload default fragment, as it
         // will not be in the back stack
         if (currentFragment.equals(fragmentManager.findFragmentByTag(Constants.showEvents))) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, new FragmentBrandsListing(), Constants.mainFragTag);
            ft.commit();
         }
			}
			else {

				// if showing brands from selected category
				if (currentFragment.equals(fragmentManager.findFragmentByTag(Constants.categoryBrandsTag))) {
					fragmentManager.popBackStack();
				}

				// If featured fragment is active, show toast to close app
				else {
					long currentTime = System.currentTimeMillis();
					if (currentTime - mLastPress > TOAST_DURATION) {
						onBackPressedToast = Toast.makeText(getApplicationContext(), R.string.prompt_to_exit, Toast.LENGTH_SHORT);
						onBackPressedToast.show();
						mLastPress = currentTime;
					} else {
						if (onBackPressedToast != null) {
							onBackPressedToast.cancel();
							// Difference with previous answer. Prevent continuing showing toast after application exit.
							onBackPressedToast = null;

							// exit application
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
								finishAffinity();
							} else { // finish activity and exit app
								finish();
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		// Create a new fragment and specify the fragment to show based on nav item clicked
		Fragment fragment = null;

		//Get ID for current active fragment
		currentFragment = fragmentManager.findFragmentById(R.id.content_frame);

		// attributes
		Class fragmentClass = null;
		boolean doFrag = true;
		boolean webItemSelected = false;
		String fragmentTag = null;
		String webUrl = null;
		String webTitle = null;

		Resources resource = getResources();


		//Get account type (Brand/Patron) for account related options
	 	accountType = sessionManager.isLoggedIn()?sessionManager.getPreference(SessionManager.accountType):null;

		switch (id){

			// Handle the FragmentBrandsListing brands action
			case R.id.nav_featured_brands:
				fragmentClass = FragmentBrandsListing.class;
				fragmentTag = Constants.mainFragTag;
				break;

			// my super test fragment
			// Handle the FragmentBrandsListing brands action
//			case R.id.nav_spinner:
//				fragmentClass = SpinnerFragment.class;
//				fragmentTag = "spinner";
//				break;

			// Handle the Categories screen action
			case R.id.nav_categories:
				fragmentClass = FragmentCategories.class;
				fragmentTag = Constants.categoriesFragtag;
				break;

			// Handle the Categories screen action
			case R.id.nav_favorites:
				fragmentClass = FragmentFavorites.class;
				fragmentTag = Constants.favoritesFragtag;
				break;

			// Handle the My Account action
			case R.id.nav_account:
				assert accountType != null;
				switch (accountType) {

					// Condition for brand user account
					case Constants.brandString:
						//Do check for incomplete brand profile
						if (sessionManager.isProfileComplete()){
							// If profile is complete, go to brand dashboard
							fragmentClass = FragmentBrandDashboard.class;
							fragmentTag = Constants.brandDashboardFragTag;
						}
						else { // Handle My account action when registration in incomplete
							fragmentClass = FragmentCompleteBrandRegistration.class;
							fragmentTag = Constants.brandUpdateFragTag;
						}
						break;
					// Condition for base user account
					case Constants.patronString:
						fragmentClass = FragmentUserDashboard.class;
						fragmentTag = Constants.patronDashboardFragTag;
						break;
				}
				break;

			// Handle the login action
			case R.id.nav_login:
				fragmentClass = FragmentLogin.class;
				fragmentTag = Constants.loginFragTag;
				break;

			//Handle logout action
			case R.id.nav_logout:
				doFrag = false;
				sessionManager.logoutUser();
				if (!currentFragment.getClass().equals(FragmentBrandsListing.class)){
					Fragment postLogoutFrag = new FragmentBrandsListing();
					AddFragment(postLogoutFrag,Constants.mainFragTag);
				}
				showNavigation();
				navigationView.getMenu().getItem(0).setChecked(true);
				break;

			//Handle the my events action
			case R.id.nav_my_events:
				fragmentTag = Constants.myEventsFragTag;
				fragmentClass = UserEventsFragment.class;
				break;

			// selecting options to open web links
			//
			case R.id.nav_terms:
				doFrag = false;
				webItemSelected = true;
				webUrl = Constants.termsUrl;
				webTitle = resource.getString(R.string.usage_terms);
				break;

			//
			case R.id.nav_about:
				doFrag = false;
				webItemSelected = true;
				webUrl = Constants.aboutUrl;
				webTitle = resource.getString(R.string.about_page);
				break;

			//
			case R.id.nav_contact:
				doFrag = false;
				webItemSelected = true;
				webUrl = Constants.contactUrl;
				webTitle = resource.getString(R.string.contact_us);
				break;

      // invite friends
       case R.id.nav_invite:
          doFrag = false;
          webItemSelected = false;
          shareApp();
          break;

			// default action
			default:
				fragmentClass = FragmentBrandsListing.class;
				fragmentTag = Constants.mainFragTag;
				break;
		}
		try {
				fragment = (Fragment) fragmentClass.newInstance();
			}
		catch (Exception e) {
			e.printStackTrace();
		}
		// Insert the fragment by replacing any existing fragment
		//Compare current and selected nav IDs before fragment creation
		if (doFrag) {

			// Add fragment if selected fragment is not the visible fragment
			if (!currentFragment.getClass().getName().equals(fragment.getClass().getName())){
				assert fragmentTag != null;
				switch (fragmentTag) {

					// Add bundle arguments depending on Nav drawer selected item
					case Constants.categoriesFragtag: // if Categories is selected
						args.putSerializable(Constants.brandsCategories,brandsCategories);
						fragment.setArguments(args);
						break;

					case Constants.brandUpdateFragTag: // If redirected to brand complete profile screen
						args.putString(Constants.extraString,loginDetails.toString());
						fragment.setArguments(args);
						break;
				}

				// Do FragmentTransaction to add new fragment
				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.hide(currentFragment);
				ft.add(R.id.content_frame, fragment, fragmentTag);
				ft.addToBackStack(null);
				ft.commit();
			}
		}

		// open web activity nav links
		if (webItemSelected) {
			openWebPage(webUrl, webTitle);
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	// method opens the web view activity
	public void openWebPage(String webUrl, String title){

		//
		Intent openPage = new Intent(getApplication(), WebViewActivity.class);
		openPage.putExtra(Constants.openUrl, webUrl);
		openPage.putExtra(Constants.urlTitle, title);
		startActivity(openPage);
	}

   // method to share app
   public void shareApp() {
      Intent shareApp =  new Intent();
      shareApp.setAction(Intent.ACTION_SEND);
      shareApp.putExtra(Intent.EXTRA_TEXT, StringHandler.shareAppMessage());
      shareApp.setType("text/plain");
      startActivity(shareApp);
   }

	//Load default fragment
	public void LoadFragment(Fragment fragment, String fragmentTag, int position){
		switch (fragmentTag){

      // Handle Categories dialogue
      case "categoriesDialogue":
        args.putSerializable("brandsCategories",brandsCategories);
        fragment.setArguments(args);
        break;
		}

		// Set the corresponding nav drawer position as checked
		setNavigation(position);

		// add fragment to the fragment_container view
		fragmentTransaction.add(R.id.content_frame, fragment, fragmentTag);

		// Add fragment to backStack if not the default fragment
		if (!fragmentTag.equals(Constants.mainFragTag)) {
			fragmentTransaction.addToBackStack(fragmentTag);
		}

		// Commit the transaction
		fragmentTransaction.commit();
	}

	//Reset selected item on Nav drawer to default
	public void setNavigation(int position) {
		navigationView.getMenu().getItem(position).setChecked(true);
	}

	//Implement OnListItemSelected interface from the FragmentCategories
	@Override
	public void OnListItemSelected(Integer categoryId, String category){

		//TODO: Commented section below will be updated when the multi-pane layout support is added

//		FragmentBrandsListing featuredFragment = (FragmentBrandsListing) getSupportFragmentManager().findFragmentByTag(Constants.mainFragTag);
//		if (featuredFragment != null) {
//			// If article frag is available, we're in two-pane layout...
//
//			// Call a method in the ArticleFragment to update its content
////			articleFrag.updateArticleView(position);
//		} else {
			// Otherwise, we're in the one-pane layout and must swap frags...

			// Create fragment and pass values for the selected brand
			FragmentBrandsListing fragmentBrandsListing = new FragmentBrandsListing();
			Bundle args = new Bundle();
			args.putInt(Constants.categoryId, categoryId);
			args.putString(Constants.categoryString, category);
			fragmentBrandsListing.setArguments(args);

			// Add fragment with brands from selected category
			AddFragment(fragmentBrandsListing, Constants.categoryBrandsTag);
			setNavigation(Constants.navMainPosition);
//		}
	}

	// Implement the OnLoginSuccess interface from the login/register screens
	@Override
	public void OnLoginSuccess(String accountType, String extrasString, Boolean completeProfile) {

		// New fragment instance indicating screen after successful login
		Fragment postLoginFragment = null;
		String fragmentTag = null;
		navigationView.getMenu().getItem(2).setChecked(true);

		switch (accountType) {

			// Handle patron account login redirect
			case Constants.patronString:
				postLoginFragment = new FragmentUserDashboard();
				fragmentTag = Constants.patronDashboardFragTag;
				break;

			// Handle brand account login redirects
			case Constants.brandString:

				// If brand profile is complete, show brand dashboard
				if (completeProfile){
					postLoginFragment = new FragmentBrandDashboard();
					fragmentTag = Constants.brandDashboardFragTag;
				}

				else { // Show complete registration if profile in incomplete
					postLoginFragment = new FragmentCompleteBrandRegistration();
					fragmentTag = Constants.brandUpdateFragTag;
				}
		}
		// Create fragment bundle
		Bundle args = new Bundle();
		args.putString(Constants.extraString, extrasString);
		postLoginFragment.setArguments(args);

		//
		showNavigation();
		navigationView.getMenu().getItem(Constants.navAccountPosition).setChecked(true);
		AddFragment(postLoginFragment, fragmentTag);
	}

	@Override
	public void ButtonToRegister(Fragment fragment, String fragmentTag) {
		AddFragment(fragment,fragmentTag);
	}

	@Override
	public void ButtonToLogin(Fragment fragment, String fragmentTag) {
		AddFragment(fragment,fragmentTag);
	}

	// Dynamically add designated fragment to fragment frame
	public void AddFragment(Fragment fragment, String fragmentTag) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
		// Replace whatever is in the fragment_container view with this fragment,
		transaction.hide(currentFragment);
		transaction.add(R.id.content_frame, fragment, fragmentTag);
		transaction.addToBackStack(fragmentTag);
		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void ReloadBrandDetails() {
		AddFragment(new FragmentBrandDashboard(), Constants.brandDashboardFragTag);
	}

	@Override
	public void FragmentReload(Fragment fragment, String fragmentTag){

//		Fragment fragment = new FragmentFavorites();
//		String fragmentTag = Constants.favoritesFragtag;

		FragmentTransaction transact = getSupportFragmentManager().beginTransaction();

		currentFragment = fragmentManager.findFragmentById(R.id.content_frame);
		// Replace whatever is in the fragment_container view with this fragment,
		transact.remove(currentFragment);
		transact.add(R.id.content_frame, fragment, fragmentTag);
		transact.addToBackStack(fragmentTag);

		transact.commit();
	}

	// Task to fetch array of category IDs from brand users table for count
	public class BrandCategoriesTask extends AsyncTask<String, String, HashMap<Integer, String>> {

		@Override
		protected HashMap<Integer, String> doInBackground(String... args) {
			JSONParser jsonParser = new JSONParser();

			try {
				HashMap<String, String> params = new HashMap<>();
				params.put("query", "brands");

				//Send session username and userID to server to get details
				JSONObject json = jsonParser.makeHttpRequest(Constants.brandsCountUrl, Constants.parserPost, params);

				if (json != null){
					//Get the authentication status or error message
					JSONObject serverAuth = json.getJSONObject("serverAuth");
					String auth_key = serverAuth.getString("auth_key");
					//If authentication is OK get user account data
					if(auth_key.equals(Constants.fetchOK)) {
						//Get data from the user account
						JSONObject result = json.getJSONObject("result");
						JSONArray returned = result.getJSONArray("brands_categories");

						for (int i=0; i < returned.length(); i++){
							JSONObject data = returned.getJSONObject(i);
							String categoryId = data.getString("category_id");
							brandsCategories.put(i, categoryId);
						}
					}
					return brandsCategories;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}