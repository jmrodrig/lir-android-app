package io.lostinreality.lir_android_app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.FacebookSdkNotInitializedException;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * Created by jose on 25/07/16.
 */
public class Utils {

    public static double distanceBetweenCoordinates(double lat1, double lon1, double lat2,
                                                    double lon2, double el1, double el2) {

    /*
    * Calculate distance between two points in latitude and longitude taking
    * into account height difference. If you are not interested in height
    * difference pass 0.0. Uses Haversine method as its base.
    *
    * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
    * el2 End altitude in meters
    * @returns Distance in Meters
    *
    * http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
    */

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static void fetchGeoCodeAddress(Context context, Location l, int addressdetail) {
        RequestQueue queue;
        queue = RequestsSingleton.getInstance(context).getRequestQueue();
        String url = Constants.NOMINATIM_API_REVESE_GEOCODE_ENTRY + "?format=json&lat=" + l.latitude + "&lon=" + l.longitude + "&zoom=" + l.zoom + "&addressdetails=" + addressdetail;
        StringRequest stringReq = new StringRequest(url, new GeoCodeResponseListener(context,l), new GeoCodeErrorListener(context,l));
        queue.add(stringReq);
    }

    private static class GeoCodeResponseListener implements Response.Listener<String> {
        private Context context;
        private Location location;

        public GeoCodeResponseListener(Context ctx, Location l) {
            context = ctx;
            location = l;
        }

        @Override
        public void onResponse(String response) {
            Toast.makeText(context, "location", Toast.LENGTH_LONG).show();
            location.name = "cococ";
        }
    }

    private static class GeoCodeErrorListener implements Response.ErrorListener {
        private Context context;
        private Location location;

        public GeoCodeErrorListener(Context ctx, Location l) {
            context = ctx;
            location = l;
        }
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(context, "no location", Toast.LENGTH_LONG).show();
            location.name = "cococ";
        }
    }

    public static AttributeSet getAttributeSetFromResource(Context ctx, int resource) {
        XmlPullParser parser = ctx.getResources().getXml(resource);
        AttributeSet attributes = Xml.asAttributeSet(parser);
        return attributes;
    }

    public static ListView expandListViewOnParentView(ListView listview) {
        listview.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        setListViewHeightBasedOnChildren(listview);
        return listview;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static Boolean copyFile(File source, File dest) {
        InputStream input = null;
        OutputStream output = null;
        try {
            try {
                input = new FileInputStream(source);
                output = new FileOutputStream(dest);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buf)) > 0) {
                    output.write(buf, 0, bytesRead);
                }
            } finally {
                input.close();
                output.close();
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static void reattemptlogin(Context context) {
        SharedPreferences appdata = context.getSharedPreferences("app_data",Context.MODE_PRIVATE);
        RequestQueue queue = RequestsSingleton.getInstance(context).getRequestQueue();
        int lastloginmethod = appdata.getInt("last_login_method", 0);
        switch (lastloginmethod) {
            case Constants.USER_PASS_LOGIN:
                String email = appdata.getString("last-credential-entry",null);
                String pass = appdata.getString("credential-pass-" + email,null);
                if (email != null && pass != null)
                    loginInServer(context,email,pass,queue);
                break;
            case Constants.FACEBOOK_LOGIN:
                try {
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    if (accessToken != null)
                        loginViaFacebook(context,accessToken.getToken(),queue);
                } catch (FacebookSdkNotInitializedException e) {
                    FacebookSdk.sdkInitialize(context.getApplicationContext());
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    if (accessToken != null)
                        loginViaFacebook(context,accessToken.getToken(),queue);
                }
                break;
        }
    }

    public static void loginInServer(Context ctx, String email, String password, RequestQueue queue) {
        String url = Constants.LOGIN_USERPASS_API_ENTRY + Constants.QUERY_PARAM_LOGIN_EMAIL + email + Constants.QUERY_PARAM_LOGIN_PASS + password;
        StringRequest logInRequest = new StringRequest(url, new LoginResponseListener(ctx), new LoginErrorListener(ctx));
        queue.add(logInRequest);
    }

    private static class LoginResponseListener implements Response.Listener<String> {
        private Context context;
        private String email, password;

        public LoginResponseListener(Context ctx) {
            context = ctx;
        }

        @Override
        public void onResponse(String response) {
            Toast.makeText(context, "Connected again!", Toast.LENGTH_LONG).show();
            onConnectionRestablished(context);
        }
    }

    private static class LoginErrorListener implements Response.ErrorListener {
        private Context context;

        public LoginErrorListener(Context ctx) {context = ctx;}
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    }

    public static void loginViaFacebook(Context ctx, String accessToken,RequestQueue queue) {
        String url = Constants.LOGIN_FACEBOOK_API_ENTRY;
        HashMap<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(url, new JSONObject(params), new FacebookLoginResponseListener(ctx), new FacebookLoginErrorListener(ctx));
        queue.add(jsonObjReq);
    }

    private static class FacebookLoginResponseListener implements Response.Listener<JSONObject> {
        private Context context;

        public FacebookLoginResponseListener(Context ctx) {context = ctx;}

        @Override
        public void onResponse(JSONObject response) {
            Toast.makeText(context, "Connected again!", Toast.LENGTH_LONG).show();
            onConnectionRestablished(context);
        }
    }

    private static class FacebookLoginErrorListener implements Response.ErrorListener {
        private Context context;

        public FacebookLoginErrorListener(Context ctx) {context = ctx;}
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    }

    private static void onConnectionRestablished(Context ctx) {
        if (ctx instanceof CreateOpenStoryActivity) {
            ((CreateOpenStoryActivity) ctx).onConnectionRestablished();
        } else if (ctx instanceof PostActivity) {
            ((PostActivity) ctx).onConnectionRestablished();
        } else if (ctx instanceof ReadStoryActivity) {
            ((ReadStoryActivity) ctx).onConnectionRestablished();
        } else if (ctx instanceof ProfileActivity) {
            ((ProfileActivity) ctx).onConnectionRestablished();
        } else if (ctx instanceof StoryListActivity) {
            ((StoryListActivity) ctx).onConnectionRestablished();
        }
    }

    public static boolean mayRequestLocation(final Activity activity, View view) {
        if (ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)) {
            Snackbar.make(view, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(activity, new String[]{ACCESS_FINE_LOCATION}, Constants.REQUEST_ACCESS_FINE_LOCATION);
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{ACCESS_FINE_LOCATION}, Constants.REQUEST_ACCESS_FINE_LOCATION);
        }
        return false;
    }

    public static boolean mayRequestMedia(final Activity activity, View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(activity,READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,READ_EXTERNAL_STORAGE)) {
            Snackbar.make(view, R.string.storage_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(activity,new String[]{READ_EXTERNAL_STORAGE}, Constants.REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(activity,new String[]{READ_EXTERNAL_STORAGE}, Constants.REQUEST_READ_EXTERNAL_STORAGE);
        }
        return false;
    }

    public static void setLastLocation(Context context, Location l) {
        context.getSharedPreferences("app_data",Context.MODE_PRIVATE).edit().putString("last_location", new Gson().toJson(l)).commit();
    }

    public static Location getLastLocation(Context context) {
        String llocationstring = context.getSharedPreferences("app_data",Context.MODE_PRIVATE).getString("last_location", null);
        if (llocationstring != null) {
            return new Gson().fromJson(llocationstring, Location.class);
        }
        return null;
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        Uri selectedImage = uri;
        String wholeID = DocumentsContract.getDocumentId(selectedImage);
        String id = wholeID.split(":")[1];
        String[] column = { MediaStore.Images.Media.DATA };

        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            return cursor.getString(columnIndex);
        } else
            return null;
    }
}
