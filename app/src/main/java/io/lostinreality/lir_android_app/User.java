package io.lostinreality.lir_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by jose on 28/07/16.
 */
public class User implements Parcelable {
    private String id;
    private Long numberId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Integer noOfStories;
    private Integer noOfSaved;
    private Integer noOfFollowers;
    private Integer noOfFollowing;
    private Boolean publicprofile;
    private Boolean currentUserFollows;


    private static final int LOAD_CURRENT_USER_DATA_API_REQUEST_CODE = 3001;
    private static final int LOAD_CURRENT_USER_STORIES_API_REQUEST_CODE = 3002;
    private static final int LOAD_PUBLIC_PROFILE_DATA_API_REQUEST_CODE = 3004;
    private static final int LOAD_PUBLIC_PROFILE_STORIES_API_REQUEST_CODE = 3005;
    private static final int FOLLOW_USER_API_REQUEST_CODE = 3006;
    private static final int UPLOAD_USER_AVATAR_API_REQUEST_CODE = 3007;



    public User() {};

    private void setUserData(User userdata) {
        this.id = userdata.getId();
        this.numberId = userdata.getNumberId();
        this.firstName = userdata.getFirstName();
        this.lastName = userdata.getLastName();
        this.fullName = userdata.getFullName();
        this.email = userdata.getEmail();
        this.avatarUrl = userdata.getAvatarUrl();
        this.noOfStories = userdata.getNoOfStories();
        this.noOfSaved = userdata.getNoOfSaved();
        this.noOfFollowers = userdata.getNoOfFollowers();
        this.noOfFollowing = userdata.getNoOfFollowing();
        this.publicprofile = userdata.isPublic();
        this.currentUserFollows = userdata.getCurrentUserFollows();
    }

    public String getId() { return this.id; }

    public Long getNumberId() { return this.numberId; }

    public String getFirstName() { return this.firstName; }

    public String getLastName() { return this.lastName; }

    public String getFullName() { return this.fullName; }

    public String getEmail() { return this.email; }

    public String getAvatarUrl() {
        if (this.avatarUrl == null)
            return "/assets/images/lir-logo.png";
        if (Constants.reg_exp_pattern.matcher(this.avatarUrl).find()) {
            return avatarUrl;
        } else {
            return Constants.LIR_SERVER_URL_DOMAIN + avatarUrl;
        }
    }

    public Integer getNoOfStories() { return this.noOfStories; }

    public Integer getNoOfSaved() { return this.noOfSaved; }

    public Integer getNoOfFollowers() { return this.noOfFollowers; }

    public Integer getNoOfFollowing() { return this.noOfFollowing; }

    public Boolean isPublic() { return (publicprofile != null && publicprofile); }

    public Boolean getCurrentUserFollows() { return currentUserFollows; }

    public void setNumberId(Long nid) { this.numberId = nid; }

    public void setPublicprofile(Boolean bool) { this.publicprofile = bool; }

    public void setCurrentUserFollows(Boolean bool) { this.currentUserFollows = bool; }

    public void setAvatarUrl(String avtr) {
        avatarUrl = avtr;
    }

    // Parcelling Object User
    private User(Parcel in){
        String[] data = new String[13];

        in.readStringArray(data);
        this.id = data[0];
        this.numberId = Long.parseLong(data[1]);
        this.firstName = data[2];
        this.lastName = data[3];
        this.fullName = data[4];
        this.email = data[5];
        this.avatarUrl = data[6];
        this.noOfStories = Integer.parseInt(data[7]);
        this.noOfSaved = Integer.parseInt(data[8]);
        this.noOfFollowers = Integer.parseInt(data[9]);
        this.noOfFollowing = Integer.parseInt(data[10]);
        this.publicprofile = Boolean.valueOf(data[11]);
        this.currentUserFollows = Boolean.valueOf(data[12]);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeStringArray(new String[]{
                this.id,
                this.numberId.toString(),
                this.firstName,
                this.lastName,
                this.fullName,
                this.email,
                this.avatarUrl,
                this.noOfStories.toString(),
                this.noOfSaved.toString(),
                this.noOfFollowers.toString(),
                this.noOfFollowing.toString(),
                this.publicprofile.toString(),
                this.currentUserFollows.toString()
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private void saveOnDevice(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("current_user_data", new Gson().toJson(this));
        editor.commit();
    }

    private void saveStoriesOnDevice(Context ctx, List<Story> userstories) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("current_user_stories_data", new Gson().toJson(userstories));
        editor.commit();
    }

    private void saveBookmarkedStoriesOnDevice(Context ctx, List<Story> bookstories) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("current_user_bookmarked_stories_data", new Gson().toJson(bookstories));
        editor.commit();
    }

    private Boolean loadFromDevice(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        User loadeduser = new Gson().fromJson(sharedPref.getString("current_user_data", null), User.class);
        if (loadeduser != null) {
            this.setUserData(loadeduser);
            return true;
        }
        return false;
    }

    public List<Story> loadStoriesFromDevice(Context ctx) {
        List<Story> result = Story.getAllUserStoriesStoredOnDevice(ctx, this);
//        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
//        Story[] userstories = new Gson().fromJson(sharedPref.getString("current_user_stories_data", null), Story[].class);
//        for (Story ustory : userstories) {
//            if (ustory.isStoredOnDevice(ctx)) {
//                ustory.loadStoryFromDevice(ctx);
//                result.add(ustory);
//            }
//        }
        //result.addAll(Story.getUserDraftStoriesStoredOnDevice(ctx,this));
        return result;
    }

    public List<Story> loadBookmarkedStoriesFromDevice(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        Story[] bookstories = new Gson().fromJson(sharedPref.getString("current_user_bookmarked_stories_data", null), Story[].class);
        return new LinkedList<Story>(Arrays.asList(bookstories));
    }

    public static User getCurrentUser(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        User loadeduser = new Gson().fromJson(sharedPref.getString("current_user_data", null), User.class);
        User user = new User();
        if (loadeduser != null) {
            user.setUserData(loadeduser);
            return user;
        }
        return null;
    }

    public static Boolean deleteCurrentUserFromDevice(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("current_user_data");
        editor.commit();
        return false;
    }

    public void loadData(Context ctx) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url;
        int requestcode;
        if (isPublic()) {
            url = Constants.LOAD_PUBLIC_PROFILE_DATA_API_ENTRY + this.getNumberId();
            requestcode = LOAD_PUBLIC_PROFILE_DATA_API_REQUEST_CODE;
        } else {
            url = Constants.LOAD_CURRENT_USER_DATA_API_ENTRY;
            requestcode = LOAD_CURRENT_USER_DATA_API_REQUEST_CODE;
        }
        final Map<String, String> mHeaders = new HashMap<String, String>();
        mHeaders.put("Content-Type", "text/plain");
        JsonObjectRequest loadDataRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new UserListnerSuccess(ctx, this, requestcode,null),
                new UserListnerError(ctx,this,requestcode)) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }
        };
        queue.add(loadDataRequest);
    }

    public void loadStories(Context ctx) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url;
        int requestcode;
        if (isPublic()) {
            url = Constants.LOAD_PUBLIC_PROFILE_STORIES_API_ENTRY + this.getNumberId();
            requestcode = LOAD_PUBLIC_PROFILE_STORIES_API_REQUEST_CODE;
        } else {
            url = Constants.LOAD_CURRENT_USER_STORIES_API_ENTRY;
            requestcode = LOAD_CURRENT_USER_STORIES_API_REQUEST_CODE;
        }
        JsonArrayRequest loadstoriesRequest = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                new JSONArrayUserListnerSuccess(ctx, this, requestcode),
                new UserListnerError(ctx,this,requestcode));
        queue.add(loadstoriesRequest);
    }

    public void sendFollow(Context ctx, Long userid) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url = Constants.FOLLOW_USER_API_ENTRY + userid;
        final Map<String, String> mHeaders = new HashMap<String, String>();
        mHeaders.put("Content-Type", "text/plain");
        int requestcode = FOLLOW_USER_API_REQUEST_CODE;
        JsonObjectRequest sendFollowRequest = new JsonObjectRequest(Request.Method.PUT,
                url,
                null,
                new UserListnerSuccess(ctx,this,requestcode,null),
                new UserListnerError(ctx,this,requestcode)) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }
        };
        queue.add(sendFollowRequest);
    }

    public void uploadUserAvatar(Context ctx, String imagepath, Object viewobj) {
        File imageFile = new File(imagepath);
        FileBody fileBody = new FileBody(imageFile);
        String url = Constants.UPLOAD_USER_AVATAR_API_ENTRY;
        int requestcode = UPLOAD_USER_AVATAR_API_REQUEST_CODE;
        MultipartRequest multipartRequest = new MultipartRequest(
                url,
                imageFile.getName(),
                fileBody,
                new UserListnerSuccess(ctx,this,requestcode,viewobj),
                new UserListnerError(ctx,this,requestcode));
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        queue.add(multipartRequest);
    }


    private static class UserListnerSuccess implements Response.Listener<JSONObject> {
        private Context context;
        private User user;
        private int requestcode;
        private Object object;

        public UserListnerSuccess(Context ctx, User usr, int rqcode, Object obj) {
            context = ctx;
            user = usr;
            requestcode = rqcode;
            object = obj;
        }

        @Override
        public void onResponse(JSONObject response) {
            switch (requestcode) {
                case LOAD_CURRENT_USER_DATA_API_REQUEST_CODE:
                    User userresponce = new Gson().fromJson(response.toString(), User.class);
                    user.setUserData(userresponce);
                    user.saveOnDevice(context);
                    if (context instanceof ProfileActivity) {
                        ((ProfileActivity) context).onProfileLoadFinished();
                    }
                    break;
                case LOAD_PUBLIC_PROFILE_DATA_API_REQUEST_CODE:
                    User publicprofileresponce = new Gson().fromJson(response.toString(), User.class);
                    user.setUserData(publicprofileresponce);
                    if (context instanceof ProfileActivity) {
                        ((ProfileActivity) context).onProfileLoadFinished();
                    }
                    break;
                case FOLLOW_USER_API_REQUEST_CODE:
                    FollowResponse followresponce = new Gson().fromJson(response.toString(), FollowResponse.class);
                    if (context instanceof ProfileActivity) {
                        ((ProfileActivity) context).setUserFollowView(Boolean.valueOf(followresponce.currentUserFollowsUser), followresponce.noOfFollowersOfUser);
                    }
                    break;
                case UPLOAD_USER_AVATAR_API_REQUEST_CODE:
                    AvatarResponse avatarresponce = new Gson().fromJson(response.toString(), AvatarResponse.class);
                    ImageView targetview = (ImageView) object;
                    user.setAvatarUrl(avatarresponce.imageUrl);
                    Glide.with(context).load(user.getAvatarUrl()).into(targetview);
                    break;
            }
        }

        private class FollowResponse {
            private Integer noOfFollowersOfUser;
            private String currentUserFollowsUser;
        }

        private class AvatarResponse {
            private Long userId;
            private String imageUrl;
        }

    }

    private static class JSONArrayUserListnerSuccess implements Response.Listener<JSONArray> {
        private Context context;
        private User user;
        private int requestcode;

        public JSONArrayUserListnerSuccess(Context ctx, User usr, int rqcode) {
            context = ctx;
            user = usr;
            requestcode = rqcode;
        }

        @Override
        public void onResponse(JSONArray response) {
            switch (requestcode) {
                case LOAD_CURRENT_USER_STORIES_API_REQUEST_CODE:
                    Log.i(Constants.TAG, response.toString());
                    Story[] storiesarray = new Gson().fromJson(response.toString(), Story[].class);
                    List<Story> userstorieslist = new ArrayList<Story>();
                    List<Story> savedstorieslist = new ArrayList<Story>();
                    int count = 0;
                    while (!storiesarray[count].isDummy() && count < storiesarray.length) {
                        userstorieslist.add(storiesarray[count]);
                        count++;
                    }
                    count++;
                    while (count < storiesarray.length) {
                        savedstorieslist.add(storiesarray[count]);
                        count++;
                    }
                    user.saveStoriesOnDevice(context, userstorieslist);
                    user.saveBookmarkedStoriesOnDevice(context, savedstorieslist);
                    List<Story> result = new ArrayList<>();
                    result.addAll(Story.getUserDraftStoriesStoredOnDevice(context, user));
                    result.addAll(userstorieslist);
                    ((ProfileActivity) context).onUserStoriesLoadFinished(result, savedstorieslist, true);
                    break;
                case LOAD_PUBLIC_PROFILE_STORIES_API_REQUEST_CODE:
                    Log.i(Constants.TAG, response.toString());
                    Story[] pprofilestoriesarray = new Gson().fromJson(response.toString(), Story[].class);
                    List<Story> pprofilestorieslist = new ArrayList<>(Arrays.asList(pprofilestoriesarray));
                    ((ProfileActivity) context).onPublicProfileStoriesLoadFinished(pprofilestorieslist);
                    break;
            }
        }


    }

    private static class UserListnerError implements Response.ErrorListener {
        private Context context;
        private User user;
        private int requestcode;

        public UserListnerError(Context ctx, User usr, int rqcode) {
            context = ctx;
            user = usr;
            requestcode = rqcode;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if(error instanceof NoConnectionError) {
                Toast.makeText(context, context.getString(R.string.offline_message), Toast.LENGTH_LONG).show();
            } else {
                Utils.reattemptlogin(context);
            }

            switch (requestcode) {
                case LOAD_CURRENT_USER_DATA_API_REQUEST_CODE:
                    if (error instanceof NoConnectionError || error instanceof AuthFailureError) {
                        user.loadFromDevice(context);
                        if (context instanceof ProfileActivity) {
                            ((ProfileActivity) context).onProfileLoadFinished();
                        }
                    }
                    break;
                case LOAD_CURRENT_USER_STORIES_API_REQUEST_CODE:
                    List<Story> userstorieslist = user.loadStoriesFromDevice(context);
                    List<Story> savedstorieslist = user.loadBookmarkedStoriesFromDevice(context);
                    ((ProfileActivity) context).onUserStoriesLoadFinished(userstorieslist, savedstorieslist,false);
                    break;
                case LOAD_PUBLIC_PROFILE_DATA_API_REQUEST_CODE:
                    ((ProfileActivity) context).finish();
                    break;
            }
        }
    }
}
