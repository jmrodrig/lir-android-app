package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jose on 25/08/16.
 */
public class LauncherActivity extends AppCompatActivity {
    private GoogleApiClient mGoogleApiClient;
    private Activity _this = this;
    private android.location.Location mLastLocation;
    private Location lastLocation;
    private RelativeLayout launcherLayout;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private LoginFragment loginfragment;
    private SignupFrament signupfragment;
    private RequestQueue queue;
    private Boolean credentialsvalidity;
    private int lastloginmethod;
    private String signuptoken;
    private ProgressBar launchprogressbar;

    private static final int START_SIGNUP_REQUEST_CODE = 4001;
    private static final int FINISH_SIGNUP_REQUEST_CODE = 4002;

    public static final String QUERY_PARAM_LOGIN_EMAIL = "?username=";
    public static final String QUERY_PARAM_LOGIN_PASS = "&password=";
    public static final String QUERY_PARAM_SIGN_UP_EMAIL = "?email=";
    public static final String QUERY_PARAM_SIGN_UP_FNAME = "?firstName=";
    public static final String QUERY_PARAM_SIGN_UP_LNAME = "&lastName=";
    public static final String QUERY_PARAM_SIGN_UP_PASS = "&password.password1=";
    public static final String QUERY_PARAM_SIGN_UP_CONF_PASS = "&password.password2=";

    @Override
    public PendingIntent createPendingResult(int requestCode, Intent data, int flags) {
        return super.createPendingResult(requestCode, data, flags);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_launcher);

        sharedPref = getSharedPreferences("app_data", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        queue = RequestsSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        launcherLayout = (RelativeLayout) findViewById(R.id.launcher_layout);
        launchprogressbar = (ProgressBar) findViewById(R.id.launcher_progress_bar);
        editor.putLong("read_story_from_link",-1).commit();

        if (getIntent().getBooleanExtra("user_logged_out",false)) {
            editor.putBoolean("credentials_validity", false);
            editor.commit();
        }
        credentialsvalidity = sharedPref.getBoolean("credentials_validity", false);

        if (getIntent().getData() != null) {
            String link = getIntent().getData().toString();
            if (link.contains(Constants.READ_STORY_API_ENTRY) && credentialsvalidity) {
                long readstoryfromlink = Long.parseLong(getIntent().getData().toString().split(Constants.READ_STORY_API_ENTRY)[1]);
                editor.putLong("read_story_from_link",readstoryfromlink).commit();
                attemptLogin();
            } else if (link.contains(Constants.SIGNUP_API_ENTRY)) {
                try {
                    signuptoken = getIntent().getData().toString().split(Constants.SIGNUP_API_ENTRY)[1];
                    loadSignUpFragment(true);
                } catch (IndexOutOfBoundsException e) {
                    loadSignUpFragment(false);
                }
            } else {
                attemptLogin();
            }
        } else {
            attemptLogin();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void attemptLogin() {
        if (!credentialsvalidity) {
            loadLogInFragment();
        } else {
            lastloginmethod = sharedPref.getInt("last_login_method", 0);
            switch (lastloginmethod) {
                case Constants.USER_PASS_LOGIN:
                    launchprogressbar.setVisibility(View.VISIBLE);
                    String email = sharedPref.getString("last-credential-entry", null);
                    String pass = sharedPref.getString("credential-pass-" + email, null);
                    if (email != null && pass != null)
                        loginInServer(email, pass);
                    break;
                case Constants.FACEBOOK_LOGIN:
                    launchprogressbar.setVisibility(View.VISIBLE);
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    if (accessToken != null) {
                        loginViaFacebook(accessToken.getToken());
                    } else {
                        loadLogInFragment();
                    }
                    break;
                default:
                    loadLogInFragment();
                    break;
            }
        }
    }

    private void saveCredentials(String email, String pass) {
        Set<String> credentialids = sharedPref.getStringSet("credentials-user-ids", new HashSet<String>() {
        });
        if (!credentialids.contains(email))
            credentialids.add(email);
        editor.putStringSet("credentials-user-ids", credentialids);
        editor.putString("credential-pass-" + email, pass);
        editor.putString("last-credential-entry", email);
        editor.commit();
    }

    private HashMap<String,String> getCredentials() {
        HashMap<String,String> credentialEntries = new HashMap<>();
        Set<String> credentialids = sharedPref.getStringSet("credentials-user-ids",new HashSet<String>() {});

        for (String email : credentialids) {
            String password = sharedPref.getString("credential-pass-" + email,"");
            credentialEntries.put(email,password);
        }
        return credentialEntries;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loginfragment.callbackManager.onActivityResult(requestCode, resultCode, data);
    }



    private void onLoginFinished(Boolean offline) {
        User user = new User();
        user.loadData(getBaseContext());
        long readstoryfromlink = sharedPref.getLong("read_story_from_link",-1);
        if (readstoryfromlink > -1) {
            readStoryFromLink(readstoryfromlink);
        } else {
            loadMainActivity(offline);
        }
    }

    private void onLoginFailed(VolleyError error) {
        launchprogressbar.setVisibility(View.GONE);
        if (loginfragment != null) loginfragment.showProgress(false);
        credentialsvalidity = sharedPref.getBoolean("credentials_validity", false);
        if (error instanceof NoConnectionError && credentialsvalidity) {
            Toast.makeText(_this, getString(R.string.offline_message), Toast.LENGTH_LONG).show();
            onLoginFinished(true);
        } else if (!credentialsvalidity && LoginManager.getInstance() != null) {
            LoginManager.getInstance().logOut();
            Toast.makeText(_this, "Mobile log in error. Please, try again.", Toast.LENGTH_LONG).show();
        }
    }

    public void loadMainActivity(Boolean offline) {
        Intent intent = new Intent(this, StoryListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * asynchronous login/registration task used to authenticate
     * the user on the server - via volley library.
     */
    public void loginInServer(String email, String password) {
        String url = Constants.LOGIN_USERPASS_API_ENTRY + QUERY_PARAM_LOGIN_EMAIL + email + QUERY_PARAM_LOGIN_PASS + password;
        StringRequest logInRequest = new StringRequest(url, new LoginResponseListener(this,email,password), new LoginErrorListener(this));
        queue.add(logInRequest);
    }

    private class LoginResponseListener implements Response.Listener<String> {
        private Context context;
        private String email, password;

        public LoginResponseListener(Context ctx,String e,String p) {
            context = ctx;
            email = e;
            password = p;
        }

        @Override
        public void onResponse(String response) {
            //if (loginfragment != null) loginfragment.showProgress(false);
            editor.putBoolean("credentials_validity", true);
            editor.putInt("last_login_method", Constants.USER_PASS_LOGIN);
            editor.commit();
            saveCredentials(email, password);
            onLoginFinished(false);
        }
    }

    private class LoginErrorListener implements Response.ErrorListener {
        private Context context;

        public LoginErrorListener(Context ctx) {context = ctx;}
        @Override
        public void onErrorResponse(VolleyError error) {
            onLoginFailed(error);
        }
    }

    public void loginViaFacebook(String accessToken) {
        String url = Constants.LOGIN_FACEBOOK_API_ENTRY;
        HashMap<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(url, new JSONObject(params), new FacebookLoginResponseListener(this), new FacebookLoginErrorListener(this));
        queue.add(jsonObjReq);
    }

    private class FacebookLoginResponseListener implements Response.Listener<JSONObject> {
        private Context context;

        public FacebookLoginResponseListener(Context ctx) {context = ctx;}

        @Override
        public void onResponse(JSONObject response) {
            //if (loginfragment != null) loginfragment.showProgress(false);
            editor.putBoolean("credentials_validity", true);
            editor.putInt("last_login_method", Constants.FACEBOOK_LOGIN);
            editor.commit();
            onLoginFinished(false);
        }
    }

    private class FacebookLoginErrorListener implements Response.ErrorListener {
        private Context context;

        public FacebookLoginErrorListener(Context ctx) {context = ctx;}
        @Override
        public void onErrorResponse(VolleyError error) {
            onLoginFailed(error);
        }
    }

    public void startSignupInServer(String email) {
        String url = Constants.SIGNUP_API_ENTRY + QUERY_PARAM_SIGN_UP_EMAIL + email;
        StringRequest signupRequest = new StringRequest(Request.Method.POST, url, new SignupResponseListener(this,START_SIGNUP_REQUEST_CODE), new SignupErrorListener(this,START_SIGNUP_REQUEST_CODE));
        queue.add(signupRequest);
    }

    public void finishSignupInServer(String firstName, String lastName, String password, String confirmpassword) {
        String url = Constants.SIGNUP_API_ENTRY + signuptoken +
                QUERY_PARAM_SIGN_UP_FNAME + firstName +
                QUERY_PARAM_SIGN_UP_LNAME + lastName +
                QUERY_PARAM_SIGN_UP_PASS + password +
                QUERY_PARAM_SIGN_UP_CONF_PASS + confirmpassword;
        StringRequest signupRequest = new StringRequest(Request.Method.POST, url, new SignupResponseListener(this,FINISH_SIGNUP_REQUEST_CODE), new SignupErrorListener(this,FINISH_SIGNUP_REQUEST_CODE));
        queue.add(signupRequest);
    }

    private class SignupResponseListener implements Response.Listener<String> {
        private Context context;
        private int requestcode;

        public SignupResponseListener(Context ctx,int rqc) {
            context = ctx;
            requestcode = rqc;
        }

        @Override
        public void onResponse(String response) {
            switch (requestcode) {
                case START_SIGNUP_REQUEST_CODE:
                    reloadLogInFragment();
                    Toast.makeText(_this, getString(R.string.sign_up_start_message), Toast.LENGTH_LONG).show();
                    break;
                case FINISH_SIGNUP_REQUEST_CODE:
                    onLoginFinished(false);
                    break;
            }
        }
    }

    private class SignupErrorListener implements Response.ErrorListener {
        private Context context;
        private int requestcode;

        public SignupErrorListener(Context ctx, int rqc) {
            context = ctx;
            requestcode = rqc;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof NoConnectionError && credentialsvalidity) {
                Toast.makeText(context, getString(R.string.offline_message), Toast.LENGTH_LONG).show();
            }
            switch (requestcode) {
                case START_SIGNUP_REQUEST_CODE:
                    reloadLogInFragment();
                    break;
                case FINISH_SIGNUP_REQUEST_CODE:
                    Toast.makeText(context, getString(R.string.sign_up_finish_error_message), Toast.LENGTH_LONG).show();
                    reloadLogInFragment();
                    break;
            }
        }
    }

    public void reloadSignUpFragment(Boolean finish) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out);
        signupfragment = new SignupFrament();
        Bundle b = new Bundle();
        b.putBoolean("finishlayout", finish);
        signupfragment.setArguments(b);
        ft.replace(R.id.fragment_container, signupfragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void loadSignUpFragment(Boolean finish) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
        signupfragment = new SignupFrament();
        Bundle b = new Bundle();
        b.putBoolean("finishlayout", finish);
        signupfragment.setArguments(b);
        ft.add(R.id.fragment_container, signupfragment);
        ft.commit();
    }

    public void loadLogInFragment() {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        loginfragment = new LoginFragment();
        ft.add(R.id.fragment_container, loginfragment);
        ft.commit();
    }

    public void reloadLogInFragment() {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out);
        loginfragment = new LoginFragment();
        ft.replace(R.id.fragment_container, loginfragment);
        ft.commit();
    }

    private void readStoryFromLink(long storyid) {
        editor.putLong("read_story_from_link",-1).commit();
        Intent intent = new Intent(getBaseContext(), ReadStoryActivity.class);
        intent.putExtra("storyid", storyid);
        startActivity(intent);
        finish();
    }
}
