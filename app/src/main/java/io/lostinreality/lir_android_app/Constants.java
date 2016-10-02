package io.lostinreality.lir_android_app;

import java.util.regex.Pattern;

/**
 * Created by jose on 12/07/16.
 */
public final class Constants {

    public final static int USER_PASS_LOGIN = 3000;
    public final static int FACEBOOK_LOGIN = 3001;

    public final static String LIR_SERVER_URL_DOMAIN = "http://www.lostinreality.io";

    //API ENTRIES
    public static final String LOGIN_USERPASS_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/authenticate/userpass";
    public static final String LOGIN_FACEBOOK_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/authenticatemobile/facebook";
    public static final String SIGNUP_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/signup";
    public static final String LOAD_PUBLIC_STORIES_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/listpublicstories/";
    public static final String LOAD_PUBLIC_FOLLOWING_AND_PRIVATE_STORIES_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/listpublicfollowingandprivatestories/";
    public static final String LOAD_PUBLIC_FOLLOWING_AND_PRIVATE_STORIES_WITH_LOCATION_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/listpublicfollowingandprivatestorieswithlocation/";
    public static final String SUBMIT_STORY_DATA_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/";
    public static final String CREATE_STORY_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/create/";
    public static final String LOAD_STORY_DATA_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/load/";
    public static final String LIKE_STORY_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/like/";
    public static final String BOOKMARK_STORY_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/save/";
    public static final String PUBLISH_STORY_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/publish/";
    public static final String UPLOAD_IMAGE_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/uploadimage/";
    public static final String UPLOAD_STORY_THUMBNAIL_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/uploadthumbnail/";
    public static final String UPLOAD_USER_AVATAR_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/user/uploadavatar";

    public static final String LOAD_CURRENT_USER_DATA_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/user";
    public static final String LOAD_CURRENT_USER_STORIES_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/listuserstories";
    public static final String LOAD_PUBLIC_PROFILE_DATA_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/publicprofile/";
    public static final String LOAD_PUBLIC_PROFILE_STORIES_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/listuserstories/";

    public static final String PICTURES_SERVER_PATH = LIR_SERVER_URL_DOMAIN + "/uploads/images/";

    public static final String READ_STORY_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/read/";
    public static final String READ_STORY_TRIGGER_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/read/trigger/";

    public static final String DELETE_STORY_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/story/";

    public static final String FOLLOW_USER_API_ENTRY = LIR_SERVER_URL_DOMAIN + "/user/follow/";

    //TAGS
    public static final String TAG = "LIR_Application";

    public static final Integer LOGIN_REQUEST_TAG = 2001;
    public static final Integer LIKE_REQUEST_TAG = 2002;

    //RESULTS & REQUEST CODES
    public static final int RESULT_LOCATION_SUBMITED = 1001;
    public static final int RESULT_IMAGES_SUBMITED = 1002;
    public static final int RESULT_LOCATION_CHOSEN = 1003;
    public static final int REQUEST_CODE_PLACE_AUTOCOMPLETE = 1004;
    public static final int REQUEST_CODE_LOCATION_UPDATED = 1005;
    public static final int REQUEST_CODE_LOCATION_REMOVED = 1006;
    public static final int RESULT_STORY_THUMBNAIL_CHOOSEN = 1007;
    public static final int RESULT_STORY_LOCATION_SUBMITED = 1008;
    public static final int REQUEST_CODE_STORY_POST = 1009;



    public static final int SHAREDPREFS_CHOSEN_LOCATION_SIZE = 10;



    /**
     * Id to identity permission requests.
     */
    public static final int REQUEST_READ_CONTACTS = 0;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 2;



    public static final int SECTION = 0;
    public static final int LOCATION_SECTION = 1;
    public static final int HEADER_SECTION = 2;

    public static final int STORY_TEXT = 10;
    public static final int PICTURE_CONTAINER = 11;
    public static final int STORY_SUBTITLE = 12;

    public static final int DRAFT = 0;
    public static final int PUBLISH_WITH_EVERYONE = 1;
    public static final int PUBLISH_WITH_FOLLOWERS = 2;
    public static final int PUBLISH_PRIVATELY = 3;

    public static final int STORY_FORMAT_OPEN = 0;
    public static final int STORY_FORMAT_SINGLE = 1;

    public static final double LOCATION_AREA_RADIUS_PIX = 100;


    public static final double DEFAULT_ZOOM = 12;
    public static final double DEFAULT_LATITUDE = 0.0;
    public static final double DEFAULT_LONGITUDE = 0.0;
    public static final String DEFAULT_LOCATION_NAME = "no location";
    public static final double DEFAULT_RADIUS = 0.0;

    public static final String NOMINATIM_API_REVESE_GEOCODE_ENTRY = "http://nominatim.openstreetmap.org/reverse";

    public static final Pattern reg_exp_pattern =  Pattern.compile("https?\\:\\/\\/");

    public static final String QUERY_PARAM_LOGIN_EMAIL = "?username=";
    public static final String QUERY_PARAM_LOGIN_PASS = "&password=";


}
