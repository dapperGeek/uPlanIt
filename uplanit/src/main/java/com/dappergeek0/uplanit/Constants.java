package com.dappergeek0.uplanit;

/**
 * Created by dapperGeek0 on 18/12/2016.
 * File: Constants.java
 * Usage: Contains all Constant strings and keys
 */
public class Constants {

    // Places API KEY
    public static final String placesKey = "AIzaSyBRb004ynqzOfiC3JdWyYqCFwnueFpqsuI";
    public static final String placesApiString = "placesKey";
    public static final String placesInput = "input";
    public static final String placesApiUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";


    // main search keyword input
    public static final String SEARCH_KEYWORD_STRING = "keyword";

    // intent strings
    public static final String searchString = "doSearch";
    public static final String showEvents = "showEvents";



    // Account types strings
    public static final String patronString = "patron";
    public static final String brandString = "brand";

    // Extra string text
    public static final String extraString = "extraString";

    // BrandCategories HashMap bundle name
    public static final String brandsCategories = "brandsCategories";

    /** WordPress links and strings */
    // site home page
    public static final String wordpressUrl = "http://uplanit.com.ng/";

    // terms and conditions url
    public static final String termsUrl = String.format("%sterms-of-use", wordpressUrl);

    // privacy policy url
    public static final String privacyUrl = String.format("%sprivacy-policy", wordpressUrl);

    // about us url
    public static final String aboutUrl = String.format("%sabout", wordpressUrl);

    // contact us url
    public static final String contactUrl = String.format("%scontact-us", wordpressUrl);

    // contact us url
    public static final String gopremiumUrl = String.format("%sgo-premium", wordpressUrl);

    // url string extra
    public static final String openUrl = "openUrl";

    // url string extra
    public static final String urlTitle = "urlTitle";


    /** Server uri */
    // Main server uri
    public static final String serverUrl = "http://app.uplanit.com.ng/";

    //Server url for user login
    public static final String loginUrl = String.format("%slogin_auth.php", serverUrl);

    //Server url for password reset
    public static final String pswdResetUrl = String.format("%spasswordReset.php", serverUrl);

    //Server url for brands listing
    public static final String brandsListingUrl = String.format("%sbrands_listing.php", serverUrl);

    //Server url for brands categories
    public static final String brandsCountUrl = String.format("%sbrand_categories.php", serverUrl);

    //Server url retrieving favorites
    public static final String favoritesUrl = String.format("%sfavorites.php", serverUrl);

    //Server url for password update
    public static final String updatePswdUrl = String.format("%supdatePswd.php", serverUrl);

    //Server url for new user registration
    public static final String userRegistrationUrl = String.format("%snew_user_registration.php", serverUrl);

    //Server url for new brand registration
    public static final String brandRegistrationUrl = String.format("%sbrand_user_registration.php", serverUrl);

    //Server url to update brand details
    public static final String brandUpdateUrl = String.format("%sbrand_update_details.php", serverUrl);

    //Upload to gallery url
    public static final String uploadToGalleryUrl = String.format("%sgallery_upload.php", serverUrl);

    //Retrieve gallery url
    public static final String getGalleryUrl = String.format("%sretrieve_gallery.php", serverUrl);

    //Retrieve gallery url
    public static final String eventSyncUrl = String.format("%sevent_sync.php", serverUrl);

    // Get Google Places Web Service url
    public static final String placesUrl = String.format("%sget_places.php", serverUrl);

    // Get main search keyword suggestion url
    public static final String searchKeywordUrl = String.format("%sbrandsAutoComplete.php", serverUrl);

    //Server url for new brand registration
    public static final String verificationsUrl = String.format("%sbrandVerifications.php", serverUrl);


    // Gallery images directory
    public static final String galleryDir = String.format("%simages/gallery/", serverUrl);

    // Brands logo directory
    public static final String logoDir = String.format("%simages/logo/", serverUrl);

    //SharedPreferences file name
    public static final String myPrefsName = "uPlanItPrefs";

    /**
     * Details page keys
     */
    public static final String IdString = "id";
    public static final String categoryIdColumnString = "category_id";
    public static final String categoryId = "categoryId" ;
    public static final String categoryString = "category" ;
    public static final String emailString = "email";
    public static final String passHashString = "passHash";
    public static final String accTypeString = "accountType";
    public static final String pictureString = "picture";
    public static final String validKeyString = "validKey";
    public static final String nameString = "name";
    public static final String usernameString = "username";
    public static final String logoString = "logo";
    public static final String ratingString = "rating";
    public static final String phoneString = "phone";
    public static final String addressString = "address";
    public static final String descString = "description";
    public static final String vPhone = "v_phone";
    public static final String vLocation = "v_location";
    public static final String completeString = "complete";
    public static final String premiumString = "premium";
    public static final String brandIdString = "brandId";
    public static final String brandNameString = "brandName";
    public static final String completeProfileString = "completeProfile";

    /** Server modes */
    public static final String modeString = "mode";
    public static final String modeRead = "read";
    public static final String modeFetch = "fetch";
    public static final String modeRegistration = "registration";
    public static final String modeAdd = "add";
    public static final String modeUpdate = "update";
    public static final String modeCount = "count";
    public static final String modeLogoUpdate = "logo";
    public static final String modeSend = "send";

    /** JSONParser methods */
    public static final String parserPost = "POST";
    public static final String parserGet = "GET";

    //
    public static final String LOGIN_MODE = "loginMode";
    public static final String APP_LOGIN = "appLogin";
    public static final String FB_LOGIN = "fbLogin";
    public static final String loggedOut = "You've just logged out!";


    /**
     * Server response keys
     */
    public static final String fetchOK = "FETCH_OK";
    public static final String fetchNone = "FETCH_NONE";
    public static final String STATUS = "status";
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    public static final String RESULT = "result";
    public static final String SUGGESTIONS = "suggestions";

    public static final String jsonResponse = "jsonResponse";
    public static final String jsonResult = "result";
    public static final String jsonAccounts = "accounts";
    public static final String jsonListing = "listing";
    public static final String statusKey = "statusKey";
    public static final String statusOk = "statusOk";
    public static final String statusError = "statusError";
    public static final String statusUnavailable = "notAvailable";
    public static final String serverAuthKey = "serverAuth";
    public static final String authenticationKey = "auth_key";
    public static final String authenticationOk = "AUTH_OK";

    /**
     * Manipulating images
     */
    public static final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
    public static final int brandLogoDimensions = 250 ;
    public static final String UPLOAD_OK = "UPLOAD_OK" ;
    public static final String UPLOAD_ERROR = "UPLOAD_ERROR" ;
    public static final String GALLERY_OPERATION = "operation" ;
    public static final String GALLERY_UPLOAD = "UPLOAD" ;
    public static final String GALLERY_RETRIEVE = "RETRIEVE" ;
    /** Standard activity result: operation succeeded. */
    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int RESULT_OK = -1;
    public static final int REQUEST_CODE_PICKER = 2000;
    public static final String isCurrent = "current";
    public static final int gridColumns = 3;
    public static final int maxUploadCount = 9;
    public static final int pickLimit = 10;

    /** Fragment Tags */
    public static final String DO_FRAG = "DO_FRAG" ;
    public static final String mainFragTag = "featured" ;
    public static final String categoryBrandsTag = "categoryBrands" ;
    public static final String categoriesFragtag = "categories" ;
    public static final String favoritesFragtag = "favorites" ;
    public static final String loginFragTag = "login" ;
    public static final String patronDashboardFragTag = "patronDashboard";
    public static final String brandDashboardFragTag = "brandDashboard";
    public static final String brandUpdateFragTag = "brandUpdate";
    public static final String patronRegistrationFragTag = "registration";
    public static final String BRAND_REGISTRATION_FRAG_TAG = "brandRegistration";
    public static final String myEventsFragTag = "myEvents";
    public static final String changePswdFragTag = "changePassword";
    public static final String resetPswdFragTag = "resetPassword";
    public static final String updateBrandInfoTag = "updateBrandInfo";
    public static final String updateBrandLogoTag = "updateBrandLogo";
    public static final String clearTaskDialog = "clearTaskDialog";

    // Fragments Navigation Drawer Item positions
    public static final Integer navMainPosition = 0;
    public static final Integer navAccountPosition = 1;
    public static final Integer navEventPosition = 2;
    public static final Integer NAV_CATEGORIES_POS = 3;
    public static final Integer navFavoritesPosition = 4;
    public static final Integer NAV_LOGIN_POS = 5;
    public static final Integer NAV_LOGOUT_FRAG_POS = 6;

    // Notable ArrayList positions
    public static final Integer FETCH_STATUS_POS = 0;
    public static final Integer GALLERY_POS = 1;

   /**
    * Method to format the brand logo path from the server uri and image name
    * @param logoName
    */
    public static String LogoPath(String logoName) {
        return String.format("%s"+logoName, logoDir);
    }

   /**
    * Method formats gallery image uri from name and server uri
    * @param imageName
    * @return
    */
    public static String galleryImagePath(String imageName) {
        return String.format("%s"+imageName, galleryDir);
    }

    //
    public static String intentExtra = "IntentExtra";
   /**
    * Event operations strings
    */
    public static String EVENT = "event";
    public static String IMAGE = "image";
    public static String START_DATE = "startDate";
    public static String START_TIME = "startTime";
    public static String END_DATE = "endDate";
    public static String END_TIME = "endTime";
    public static String locationString = "location";
    public static String DETAIL = "detail";
    public static String ACCOUNT_ID = "userID";
    public static String SYNC_TYPE = "syncType";
    public static String SYNC_CREATE = "create";
    public static String SYNC_UPDATE = "update";
    public static String SYNC_DELETE = "delete";
}