package io.lostinreality.lir_android_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.share.ShareApi;
import com.facebook.share.model.ShareLinkContent;
import com.google.gson.Gson;

import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jose on 30/05/16.
 */
public class Story implements Parcelable {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String thumbnail;
    private Location location;
    private List<Location> locations;
    private String locationName;
    private Author author;
    private String articleTitle;
    private String articleDescription;
    private String articleImage;
    private String articleLink;
    private Integer noOfLikes;
    private Integer noOfSaves;
    private Integer noViews;
    private Boolean currentUserLikesStory;
    private Boolean currentUserSavedStory;
    private Boolean isDummy;
    private Double distance;
    private Integer noStories;
    private Boolean userCanEdit;
    private Integer published;
    private Integer format;


    private static final int SUBMIT_STORY_DATA_API_REQUEST_CODE = 2001;
    private static final int LOAD_STORY_API_REQUEST_CODE = 2002;
    private static final int CREATE_STORY_API_REQUEST_CODE = 2003;
    private static final int LIKE_STORY_API_REQUEST_CODE = 2004;
    private static final int BOOKMARK_STORY_API_REQUEST_CODE = 2011;
    private static final int PUBLISH_STORY_API_REQUEST_CODE = 2005;
    private static final int UPLOAD_IMAGE_API_REQUEST_CODE = 2006;
    private static final int PUBLISH_FACEBOOK_API_REQUEST_CODE = 2007;
    private static final int UPLOAD_STORY_THUMBNAIL_API_REQUEST_CODE = 2008;
    private static final int DELETE_STORY_API_REQUEST_CODE = 2009;
    private static final int UPLOAD_ALL_IMAGES_API_REQUEST_CODE = 2010;
    private static final int DOWNLOAD_STORY_API_REQUEST_CODE = 2012;
    private static final int READ_STORY_TRIGGER_REQUEST_CODE = 2013;

    public static final long TEMP_DEVICE_STORY_ID_INDEX = 900000000;

    //constructors
    public Story() {
        this.noOfLikes = 0;
        this.noOfSaves = 0;
        this.currentUserLikesStory = false;
        this.currentUserSavedStory = false;
        this.userCanEdit = true;
        this.published = 0;
        this.locations = new ArrayList<>();
    }

    //getters
    public  Long getId() {
        return id;
    }

    public  String getSummary() {
        return summary;
    }

    public  String getThumbnail() {
        return thumbnail;
    }

    public  Location getLocation() {
        return location;
    }

    public List<Location> getLocations() { return locations; }

    public  String getTitle() {
        return title;
    }

    public  String getLocationName() {
        return locationName;
    }

    public  List<ContentSection> getContent() {
        if (this.content == null)
            return new ArrayList<ContentSection>();
        ContentSection[] contentarray = new Gson().fromJson(this.content, ContentSection[].class);
        return new LinkedList<ContentSection>(Arrays.asList(contentarray));
    }

    public ContentSection getHeaderSection() {
        for (ContentSection section : getContent()) {
            if (section.getType() == Constants.HEADER_SECTION) {
                return section;
            }
        }
        return null;
    }

    public  String getAuthorName() {
        return author.getFullName();
    }

    public  String getUserAvatar() {
        return author.getAvatarUrl();
    }

    public  Author getAuthor() {
        return author;
    }

    public  Integer getPublished() {
        return published;
    }

    public Integer getNoLikes() { return noOfLikes;  }

    public Integer getNoOfSaves() { return noOfSaves;  }

    public Boolean getCurrentUserLikesStory() { return currentUserLikesStory; }

    public Boolean getCurrentUserSavedStory() { return currentUserSavedStory; }

    public  Boolean isDummy() { return isDummy; }

    public  Double getDistance() { return distance; }

    public  Integer getNoStories() {
        return noStories;
    }

    public Boolean canCurrentUserEdit() { return userCanEdit; }

    public Integer getFormat() { return format; }

    public Integer getNOViews() { return noViews; }


    //setters
    public void setId(Long id) { this.id = id; }

    public void setTitle(String text) { this.title = text; }

    public void setSummary(String smry) { this.summary = smry; }

    public void setContent(List<ContentSection> cntlist) {
        List<ContentSection> content = new ArrayList<>();
        ContentSection header = getHeaderSection();
        if (header != null )
            content.add(header);
        if (cntlist != null)
            content.addAll(cntlist);
        this.content = new Gson().toJson(content);
    }

    public void setLocation(Location l) {
        this.location = l;
    }

    public void setLocations(List<Location> llist) {
        this.locations = llist;
    }

    public void setLocationName(String ln) { this.locationName = ln; }

    public void setAuthor(Author a) { this.author = a; }

    public void setThumbnail(String thumbnailurl) { this.thumbnail = thumbnailurl; }

    public void setNoOfLikes(Integer nlikes) { this.noOfLikes = nlikes; }

    public void setNoOfSaves(Integer nsaves) { this.noOfSaves = nsaves; }

    public void setCurrentUserLikesStory(Boolean bool) { this.currentUserLikesStory = bool; }

    public void setCurrentUserSavedStory(Boolean bool) { this.currentUserSavedStory = bool; }

    public void setUserCanEdit(Boolean edit) { this.userCanEdit = edit; }

    public void setPublished(Integer pub) { this.published = pub; }

    public void setFormat(Integer f) {this.format = f; }

    public void setStory(Story st) {
        this.setId(st.getId());
        this.setTitle(st.getTitle());
        this.setSummary(st.getSummary());
        this.setContent(st.getContent());
        this.setLocation(st.getLocation());
        this.setLocations(st.getLocations());
        this.setLocationName(st.getLocationName());
        this.setAuthor(st.getAuthor());
        this.setThumbnail(st.getThumbnail());
        this.setNoOfLikes(st.getNoLikes());
        this.setNoOfSaves(st.getNoOfSaves());
        this.setCurrentUserLikesStory(st.getCurrentUserLikesStory());
        this.setCurrentUserSavedStory(st.getCurrentUserSavedStory());
        this.setUserCanEdit(st.canCurrentUserEdit());
        this.setPublished(st.getPublished());
        this.setFormat(st.getFormat());
        this.noViews = st.getNOViews();
    }

    public void addStoryLocation(Location l) {
        l.setAsMainStoryLocation(true);
        this.setLocation(l);
        this.setLocationName(l.name);
        this.locations.add(l);
        List<ContentSection> sectionslist = getContent();
        for (ContentSection section : sectionslist) {
            if (section.getType() == Constants.HEADER_SECTION) {
                section.setLocation(l);
                setContent(sectionslist);
                return;
            }
        }
        ContentSection headersection = new ContentSection();
        headersection.setType(Constants.HEADER_SECTION);
        headersection.setLocation(getLocation());
        sectionslist.add(headersection);
        setContent(sectionslist);
    }

    public void addStoryThumbnail(String fname) {
        this.setThumbnail(fname);
        List<ContentSection> content = getContent();
        for (ContentSection section : content) {
            if (section.getType() == Constants.HEADER_SECTION) {
                section.setLink(fname);
                setContent(content);
                return;
            }
        }
        ContentSection headersection = new ContentSection();
        headersection.setType(Constants.HEADER_SECTION);
        headersection.setLink(fname);
        content.add(headersection);
        setContent(content);
    }

    // Parcelling Object Story
    private Story(Parcel in){
        String[] data = new String[27];

        in.readStringArray(data);
        this.id = Long.parseLong(data[0]);
        this.title = data[1];
        this.summary = data[2];
        this.thumbnail = data[3];
        this.locationName = data[9];
        this.author = new Author(Long.parseLong(data[10]),data[11],data[12]);
        this.content = data[13];
        this.articleTitle = data[14];
        this.articleDescription = data[15];
        this.articleImage = data[16];
        this.articleLink = data[17];
        this.noOfLikes = Integer.parseInt(data[18]);
        this.noOfSaves = Integer.parseInt(data[22]);
        this.noViews = Integer.parseInt(data[26]);
        this.currentUserLikesStory = Boolean.parseBoolean(data[23]);
        this.currentUserSavedStory = Boolean.parseBoolean(data[24]);
        this.userCanEdit = Boolean.parseBoolean(data[19]);
        this.published = Integer.parseInt(data[21]);
        this.format = Integer.parseInt(data[25]);

        Location[] locationsarray = new Gson().fromJson(data[20], Location[].class);
        if (locationsarray != null)
            this.locations = new ArrayList<>(Arrays.asList(locationsarray));
        else
            this.locations = new ArrayList<>();


        if (!data[4].equals("")) {
            this.location = new Location(Double.parseDouble(data[4]),
                    Double.parseDouble(data[5]),
                    data[6],
                    Double.parseDouble(data[7]),
                    Double.parseDouble(data[8]));
            this.location.setAsMainStoryLocation(true);
        }
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeStringArray(new String[] {
                this.id.toString(),
                this.title,
                this.summary,
                this.thumbnail,
                (this.location != null) ? this.location.latitude.toString() : "",
                (this.location != null) ? this.location.longitude.toString() : "",
                (this.location != null) ? this.location.name : "",
                (this.location != null) ? this.location.radius.toString() : "",
                (this.location != null) ? this.location.zoom.toString() : "",
                this.locationName,
                this.author.getId().toString(),
                this.author.getFullName(),
                this.author.getAvatarUrl(),
                this.content,
                this.articleTitle,
                this.articleDescription,
                this.articleImage,
                this.articleLink,
                (this.noOfLikes != null) ? this.noOfLikes.toString() : "0",
                (this.userCanEdit != null) ? this.userCanEdit.toString() : "true",
                new Gson().toJson(this.locations),
                (this.published != null) ? this.published.toString() : "0",
                (this.noOfSaves != null) ? this.noOfSaves.toString() : "0",
                (this.currentUserLikesStory != null) ? this.currentUserLikesStory.toString() : "false",
                (this.currentUserSavedStory != null) ? this.currentUserSavedStory.toString() : "false",
                this.getFormat().toString(),
                (this.noViews != null) ? this.noViews.toString() : "0"
            });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Story createFromParcel(Parcel in) {
            return new Story(in);
        }

        public Story[] newArray(int size) {
            return new Story[size];
        }
    };

    public JSONObject convertToJSON() {
        String jsonStoryString = new Gson().toJson(this);
        JSONObject jsonStoryObj = null;
        try {
            jsonStoryObj = new JSONObject(jsonStoryString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonStoryObj;
    }

    public void createStory(Context ctx) {
        createInServer(ctx, CREATE_STORY_API_REQUEST_CODE);
    }

    public void saveStory(Context ctx) {
        saveStoryOnDevice(ctx);
        if (this.getId() < TEMP_DEVICE_STORY_ID_INDEX) {
            submitDataToServer(ctx,SUBMIT_STORY_DATA_API_REQUEST_CODE);
        } else {
            createStory(ctx);
        }
    }

    private void uploadNotUploadedPictures(Context ctx) {
        List<String> notUploadedFiles = new ArrayList<>();
        List<ContentSection> content = getContent();
        if (content == null) return;
        for (ContentSection section : content) {
            if (section.getType() == Constants.HEADER_SECTION && !section.isLinkUploaded()) {
                notUploadedFiles.add(loadImageFilePathFromDevice(ctx, section.getLink()));
            } else {
                for (ContentItem item : section.getContent()) {
                    if (item.getType() == Constants.PICTURE_CONTAINER && !item.isLinkUploaded())
                        notUploadedFiles.add(loadImageFilePathFromDevice(ctx, item.getLink()));
                }
            }
        }
        if (notUploadedFiles.size() > 0)
            uploadPictures(ctx, notUploadedFiles);
    }

    public void loadStory(Context ctx,Boolean download) {
        Boolean result = loadStoryFromDevice(ctx);
        if (false) {
            if (ctx instanceof ReadStoryActivity) {
                ((ReadStoryActivity) ctx).onStoryLoadFinished();
            } else if (ctx instanceof CreateOpenStoryActivity) {
                ((CreateOpenStoryActivity) ctx).onStoryLoadFinished();
            } else if (ctx instanceof CreateSingleStoryActivity) {
                ((CreateSingleStoryActivity) ctx).onStoryLoadFinished();
            } else if (ctx instanceof ReadSingleStoryActivity) {
                ((ReadSingleStoryActivity) ctx).onStoryLoadFinished();
            }
        } else {
            loadFromServer(ctx, download);
        }
    }

    public void publishStory(Context ctx,Integer publish) {
        setPublished(publish);
        saveStoryOnDevice(ctx);
        if (this.getId() < TEMP_DEVICE_STORY_ID_INDEX) {
            submitDataToServer(ctx,PUBLISH_STORY_API_REQUEST_CODE);
        } else {
            createInServer(ctx, PUBLISH_STORY_API_REQUEST_CODE);
        }
    }

    public void deleteStory(Context ctx) {
        if (this.getId() > TEMP_DEVICE_STORY_ID_INDEX) {
            deleteOnDevice(ctx,true);
            ((CreateOpenStoryActivity) ctx).finish();
        } else
            deleteOnServer(ctx);
        }

    public void savePicture(Context ctx, String link, ImageView imgview) {
        File file = saveImageFileToDevice(ctx,link);
        LinearLayout picturelayout = (LinearLayout) imgview.getParent().getParent();
        picturelayout.setTag(R.id.item_link, file.getName());
        picturelayout.setTag(R.id.item_link_uploaded_state, false);
        Glide.with(ctx).load(file.getPath()).into(imgview);
        uploadPicture(ctx, file.getPath(), imgview);
    }

    public void saveThumbnail(Context ctx, String link, ImageView imgview) {
        File file = saveImageFileToDevice(ctx, link);
        addStoryThumbnail(file.getName());
        Glide.with(ctx).load(file.getPath()).into(imgview);
        uploadStoryThumbnail(ctx, link, this);
    }

    private void saveStoryOnDevice(Context ctx) {
        SharedPreferences storiesdata = ctx.getSharedPreferences("stories_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor storiesdataeditor = storiesdata.edit();
        if (this.getId() == null) {
            SharedPreferences appdata = ctx.getSharedPreferences("app_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor appdataeditor = appdata.edit();
            Long storycounter = appdata.getLong("story_counter", TEMP_DEVICE_STORY_ID_INDEX);
            this.setId(++storycounter);
            User currrentuser = User.getCurrentUser(ctx);
            this.setAuthor(new Author(currrentuser.getNumberId(),currrentuser.getFullName(),currrentuser.getAvatarUrl()));
            appdataeditor.putLong("story_counter", storycounter);
            appdataeditor.commit();
        }
        storiesdataeditor.putString(this.getId().toString(), new Gson().toJson(this));
        storiesdataeditor.commit();
    }

    public Boolean loadStoryFromDevice(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("stories_data", Context.MODE_PRIVATE);
        Story loadedstory = new Gson().fromJson(sharedPref.getString(this.getId().toString(), null), Story.class);
        if (loadedstory != null) {
            this.setStory(loadedstory);
            return true;
        }
        return false;
    }

    private void deleteOnDevice(Context ctx, Boolean deletepictures) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("stories_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(this.getId().toString());
        editor.apply();

        if (deletepictures) {
            for (String picname : getAllPictures()) {
                deleteImageFile(ctx,picname);
            }
        }
    }

    public Boolean isStoredOnDevice(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences("stories_data", Context.MODE_PRIVATE);
        String storedstory = sharedPref.getString(this.getId().toString(), null);
        return storedstory != null;
    }

    private void createInServer(Context ctx,int requestcode) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();;
        String url = Constants.CREATE_STORY_API_ENTRY + this.getFormat();
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        JSONObject jsonstory = null;
        JsonObjectRequest createStoryDataRequest = new JsonObjectRequest(Request.Method.POST,
            url,
            this.convertToJSON(),
            new StoryListnerSuccess(ctx,requestcode,this),
            new StoryListnerError(ctx,requestcode, this));
        queue.add(createStoryDataRequest);
    }

    private void submitDataToServer(Context ctx, int requestcode) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();;
        String url = Constants.SUBMIT_STORY_DATA_API_ENTRY + this.getId();
        JsonObjectRequest submitStoryDataRequest = new JsonObjectRequest(Request.Method.PUT,
                url,
                this.convertToJSON(),
                new StoryListnerSuccess(ctx,requestcode,this),
                new StoryListnerError(ctx,requestcode,this));
        queue.add(submitStoryDataRequest);
    }

    private void loadFromServer(Context ctx, Boolean download) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url = Constants.LOAD_STORY_DATA_API_ENTRY + this.getId();
        int requestcode;
        if (download)
            requestcode = DOWNLOAD_STORY_API_REQUEST_CODE;
        else
            requestcode = LOAD_STORY_API_REQUEST_CODE;
        JsonObjectRequest loadStoryDataRequest = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new StoryListnerSuccess(ctx,requestcode,this),
                new StoryListnerError(ctx,requestcode,this));
        queue.add(loadStoryDataRequest);
    }

    public void sendLike(Context ctx, Object viewobj) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url = Constants.LIKE_STORY_API_ENTRY + this.getId();
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Content-Type", "text/plain");
        int requestcode = LIKE_STORY_API_REQUEST_CODE;
        JsonObjectRequest sendLikeRequest = new JsonObjectRequest(Request.Method.PUT,
            url,
            null,
            new StoryListnerSuccess(ctx,requestcode,viewobj),
            new StoryListnerError(ctx,requestcode,this)) {
                public Map<String, String> getHeaders() {
                    return mHeaders;
                }
        };
        queue.add(sendLikeRequest);
    }

    public void sendBookmark(Context ctx, Object viewobj) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url = Constants.BOOKMARK_STORY_API_ENTRY + this.getId();
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Content-Type", "text/plain");
        int requestcode = BOOKMARK_STORY_API_REQUEST_CODE;
        JsonObjectRequest sendBookmarkRequest = new JsonObjectRequest(Request.Method.PUT,
                url,
                null,
                new StoryListnerSuccess(ctx,requestcode,viewobj),
                new StoryListnerError(ctx,requestcode,this)) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }
        };
        queue.add(sendBookmarkRequest);
    }

    private void uploadPicture(Context ctx, String imagepath, Object viewobj) {
        File imageFile = new File(imagepath);
        FileBody fileBody = new FileBody(imageFile);
        String url = Constants.UPLOAD_IMAGE_API_ENTRY + this.getId();
        int requestcode = UPLOAD_IMAGE_API_REQUEST_CODE;
        MultipartRequest multipartRequest = new MultipartRequest(
                url,
                imageFile.getName(),
                fileBody,
                new StoryListnerSuccess(ctx,requestcode,viewobj),
                new StoryListnerError(ctx,requestcode,this));
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        queue.add(multipartRequest);
    }

    private void uploadPictures(Context ctx, List<String> imagepaths) {
        List<FileBody> fblist = new ArrayList<>();
        for (String imagepath : imagepaths) {
            if (imagepath != null) {
                File imageFile = new File(imagepath);
                FileBody fb = new FileBody(imageFile);
                fblist.add(fb);
            }
        }
        if (fblist.size() == 0) return;
        String url = Constants.UPLOAD_IMAGE_API_ENTRY + this.getId();
        int requestcode = UPLOAD_ALL_IMAGES_API_REQUEST_CODE;
        MultipartRequest multipartRequest = new MultipartRequest(
                url,
                fblist,
                new StoryListnerSuccess(ctx,requestcode,this),
                new StoryListnerError(ctx,requestcode,this));
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        queue.add(multipartRequest);
    }

    private void uploadStoryThumbnail(Context ctx, String imagepath, Object viewobj) {
        File imageFile = new File(imagepath);
        FileBody fileBody = new FileBody(imageFile);
        String url = Constants.UPLOAD_STORY_THUMBNAIL_API_ENTRY + this.getId();
        int requestcode = UPLOAD_STORY_THUMBNAIL_API_REQUEST_CODE;
        MultipartRequest multipartRequest = new MultipartRequest(
                url,
                imageFile.getName(),
                fileBody,
                new StoryListnerSuccess(ctx,requestcode,viewobj),
                new StoryListnerError(ctx,requestcode,this));
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        queue.add(multipartRequest);
    }

    private void publishOnFacebook(Context ctx) {
        AccessToken at = new AccessToken(
                "EAACEdEose0cBAKMZCpz6firZABraxvBF6uFlzR4ZBbP10aK7wG0LZCbkmb1NLOpsi2cGcZBsLU6d6rfEO8N0y4r293QPJOo7PU6iuiqynZAbZA0mO73lOh7OFDP3y1ywRsd7PVpZCmY7CUU6hjQAHM8fAtXHZBujPxs7f9ijZC5XDWMgZDZD",
                "1494692634127358",
                "10204457433503469",
                null,
                null,
                AccessTokenSource.CLIENT_TOKEN,
                null,
                null);
        AccessToken.setCurrentAccessToken(at);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(Constants.READ_STORY_API_ENTRY + this.getId()))
                .build();
        ShareApi.share(content, null);
    }

    private void deleteOnServer(Context ctx) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url = Constants.DELETE_STORY_API_ENTRY + this.getId();
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Content-Type", "text/plain");
        int requestcode = DELETE_STORY_API_REQUEST_CODE;
        JsonObjectRequest sendDeleteRequest = new JsonObjectRequest(Request.Method.DELETE,
                url,
                null,
                new StoryListnerSuccess(ctx,requestcode,this),
                new StoryListnerError(ctx,requestcode,this)) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }
        };
        queue.add(sendDeleteRequest);
    }

    public void sendReadTrigger(Context ctx) {
        RequestQueue queue = RequestsSingleton.getInstance(ctx).getRequestQueue();
        String url = Constants.READ_STORY_TRIGGER_API_ENTRY + this.getId();
        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Content-Type", "text/plain");
        int requestcode = READ_STORY_TRIGGER_REQUEST_CODE;
        JsonObjectRequest sendReadTrigger = new JsonObjectRequest(Request.Method.PUT,
                url,
                null,
                new StoryListnerSuccess(ctx,requestcode,this),
                new StoryListnerError(ctx,requestcode,this)) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }
        };
        queue.add(sendReadTrigger);
    }


    private static class StoryListnerSuccess implements Response.Listener<JSONObject> {
        private Context context;
        private int requestcode;
        private Object object;

        public StoryListnerSuccess(Context ctx, int rqcode, Object obj) {
            context = ctx;
            requestcode = rqcode;
            object = obj;
        }

        @Override
        public void onResponse(JSONObject response) {
            Story story;
            switch (requestcode) {
                case SUBMIT_STORY_DATA_API_REQUEST_CODE:
                    story = (Story) object;
                    Story storyresponsesubmit = new Gson().fromJson(response.toString(), Story.class);
                    story.setStory(storyresponsesubmit);
                    story.saveStoryOnDevice(context);
                    story.uploadNotUploadedPictures(context);
                    Toast.makeText(context, "Story saved", Toast.LENGTH_LONG).show();
                    break;
                case CREATE_STORY_API_REQUEST_CODE:
                    story = (Story) object;
                    Story storyresponsecreate = new Gson().fromJson(response.toString(), Story.class);
                    if (story.getId() != null && story.getId() > TEMP_DEVICE_STORY_ID_INDEX) {
                        story.deleteOnDevice(context,false);
                    }
                    story.setStory(storyresponsecreate);
                    story.saveStoryOnDevice(context);
                    story.uploadNotUploadedPictures(context);
                    Toast.makeText(context, "Story created", Toast.LENGTH_LONG).show();
                    break;
                case LOAD_STORY_API_REQUEST_CODE:
                    story = (Story) object;
                    Story storyresponseload = new Gson().fromJson(response.toString(), Story.class);
                    story.setStory(storyresponseload);
                    if (context instanceof ReadStoryActivity) {
                        ((ReadStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateOpenStoryActivity) {
                        ((CreateOpenStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateSingleStoryActivity) {
                        ((CreateSingleStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof ReadSingleStoryActivity) {
                        ((ReadSingleStoryActivity) context).onStoryLoadFinished();
                    }
                    break;
                case DOWNLOAD_STORY_API_REQUEST_CODE:
                    story = (Story) object;
                    Story storyresponsedownload = new Gson().fromJson(response.toString(), Story.class);
                    story.setStory(storyresponsedownload);
                    story.downloadAllStoryPictures(context);
                    story.saveStoryOnDevice(context);
                    if (context instanceof ReadStoryActivity) {
                        ((ReadStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof ReadSingleStoryActivity) {
                        ((ReadSingleStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateOpenStoryActivity) {
                        ((CreateOpenStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateSingleStoryActivity) {
                        ((CreateSingleStoryActivity) context).onStoryLoadFinished();
                    }
                    break;
                case LIKE_STORY_API_REQUEST_CODE:
                    LikeResponse likeresponse = new Gson().fromJson(response.toString(), LikeResponse.class);
                    if (context instanceof ReadStoryActivity)
                        ((ReadStoryActivity) context).setNoLikesLabelView(likeresponse.noOfLikes, likeresponse.currentUserLikesStory);
                    else if (context instanceof ReadSingleStoryActivity)
                        ((ReadSingleStoryActivity) context).setNoLikesLabelView(likeresponse.noOfLikes, likeresponse.currentUserLikesStory);
                    else if (context instanceof StoryListActivity) {
                        ((StoryListActivity) context).onStoryLiked(likeresponse.noOfLikes, likeresponse.currentUserLikesStory,(StoryListAdapter.StoryViewHolder) object);
                    }
                    break;
                case BOOKMARK_STORY_API_REQUEST_CODE:
                    BookmarkResponse bookmarkresponse = new Gson().fromJson(response.toString(), BookmarkResponse.class);
                    if (context instanceof ReadStoryActivity)
                        ((ReadStoryActivity) context).setNoBookmarksLabelView(bookmarkresponse.noOfSaves, bookmarkresponse.currentUserSavedStory);
                    else if (context instanceof ReadSingleStoryActivity)
                        ((ReadSingleStoryActivity) context).setNoBookmarksLabelView(bookmarkresponse.noOfSaves, bookmarkresponse.currentUserSavedStory);
                    else if (context instanceof StoryListActivity) {
                        ((StoryListActivity) context).onStoryBookmarked(bookmarkresponse.noOfSaves, bookmarkresponse.currentUserSavedStory,(StoryListAdapter.StoryViewHolder) object);
                    }
                    break;
                case PUBLISH_STORY_API_REQUEST_CODE:
                    story = (Story) object;
                    Story publishresponse = new Gson().fromJson(response.toString(), Story.class);
                    story.setStory(publishresponse);
                    story.uploadNotUploadedPictures(context);
                    Toast.makeText(context, "Story published", Toast.LENGTH_LONG).show();
                    if (context instanceof PostActivity)
                        ((PostActivity) context).onStoryPublishFinished();
                    else if (context instanceof CreateSingleStoryActivity)
                        ((CreateSingleStoryActivity) context).onStoryPublishFinished();
                    break;
                case UPLOAD_IMAGE_API_REQUEST_CODE:
                    ImageView pictureview = (ImageView) object;
                    LinearLayout picturelayout = (LinearLayout) pictureview.getParent().getParent();
                    picturelayout.setTag(R.id.item_link_uploaded_state,true);
                    break;
                case UPLOAD_STORY_THUMBNAIL_API_REQUEST_CODE:
                    story = (Story) object;
                    List<ContentSection> content = story.getContent();
                    for (ContentSection section : content) {
                        if (section.getType() == Constants.HEADER_SECTION) {
                            section.setLinkUploadedState(true);
                        }
                    }
                    story.setContent(content);
                    break;
                case UPLOAD_ALL_IMAGES_API_REQUEST_CODE:
                    story = (Story) object;
                    List<ContentSection> content_ = story.getContent();
                    for (ContentSection section : content_) {
                        if (section.getType() == Constants.HEADER_SECTION) {
                            section.setLinkUploadedState(true);
                        } else {
                            for (ContentItem item : section.getContent()) {
                                if (item.getType() == Constants.PICTURE_CONTAINER)
                                    item.setLinkUploadedState(true);
                            }
                        }
                    }
                    story.setContent(content_);
                    break;
                case DELETE_STORY_API_REQUEST_CODE:
                    story = (Story) object;
                    story.deleteOnDevice(context,true);
                    if (context instanceof ReadStoryActivity) {
                        ((ReadStoryActivity) context).onStoryDeleteFinished();
                    } else if (context instanceof CreateOpenStoryActivity) {
                        ((CreateOpenStoryActivity) context).onStoryDeleteFinished();
                    } else if (context instanceof CreateSingleStoryActivity) {
                        ((CreateSingleStoryActivity) context).onStoryDeleteFinished();
                    } else if (context instanceof ReadSingleStoryActivity) {
                        ((ReadSingleStoryActivity) context).onStoryDeleteFinished();
                    }
                    break;
            }
        }

        private class LikeResponse {
            private Boolean currentUserLikesStory;
            private Integer noOfLikes;
        }

        private class BookmarkResponse {
            private Long storyId;
            private Boolean currentUserSavedStory;
            private Integer noOfSaves;
        }

        private class UploadImageResponse {
            private Long storyId;
            private String imageUrl;
        }
    }

    private static class StoryListnerError implements Response.ErrorListener {
        private Context context;
        private int requestcode;
        private  Object object;

        public StoryListnerError(Context ctx, int rqcode, Object obj) {
            context = ctx;
            requestcode = rqcode;
            object = obj;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            Story story;
            if(!(error instanceof NoConnectionError)) {
                Utils.reattemptlogin(context);
            }
            switch (requestcode) {
                case SUBMIT_STORY_DATA_API_REQUEST_CODE:
                    Toast.makeText(context, "You are offline. Story is saved on device.", Toast.LENGTH_LONG).show();
                    break;
                case CREATE_STORY_API_REQUEST_CODE:
                    Toast.makeText(context, "You are offline. Story is saved on device.", Toast.LENGTH_LONG).show();
                    break;
                case LOAD_STORY_API_REQUEST_CODE:
                    story = (Story) object;
                    story.loadStoryFromDevice(context);
                    Toast.makeText(context, "Loaded from device!", Toast.LENGTH_LONG).show();
                    if (context instanceof ReadStoryActivity) {
                        ((ReadStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof ReadSingleStoryActivity) {
                        ((ReadSingleStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateOpenStoryActivity) {
                        ((CreateOpenStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateSingleStoryActivity) {
                        ((CreateSingleStoryActivity) context).onStoryLoadFinished();
                    }
                    break;
                case DOWNLOAD_STORY_API_REQUEST_CODE:
                    story = (Story) object;
                    story.loadStoryFromDevice(context);
                    Toast.makeText(context, "Loaded from device!", Toast.LENGTH_LONG).show();
                    if (context instanceof ReadStoryActivity) {
                        ((ReadStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof ReadSingleStoryActivity) {
                        ((ReadSingleStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateOpenStoryActivity) {
                        ((CreateOpenStoryActivity) context).onStoryLoadFinished();
                    } else if (context instanceof CreateSingleStoryActivity) {
                        ((CreateSingleStoryActivity) context).onStoryLoadFinished();
                    }
                    break;
                case LIKE_STORY_API_REQUEST_CODE:
                    break;
                case UPLOAD_IMAGE_API_REQUEST_CODE:
                    break;
                case UPLOAD_STORY_THUMBNAIL_API_REQUEST_CODE:
                    break;
                case PUBLISH_STORY_API_REQUEST_CODE:
                    Toast.makeText(context, "Publishing should be done while online.", Toast.LENGTH_LONG).show();
                    ((PostActivity) context).onStoryPublishFailed();
                    break;
                case DELETE_STORY_API_REQUEST_CODE:
                    Toast.makeText(context, "You can't delete this story while offline.", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private File saveImageFileToDevice(Context ctx, String origfilepath) {
        File origfile = new File(origfilepath);
        String[] filenameparts = origfilepath.split("\\.");
        String fileextension = filenameparts[filenameparts.length-1];
        Long time= new java.util.Date().getTime();
        String filename = "story_image_" + this.getId() + "_" + time + "." + fileextension;
        File file = new File(ctx.getFilesDir(), filename);
        Utils.copyFile(origfile, file);
        return file;
    }

    public static String getImagePath(Context ctx, String filename) {
        String path = loadImageFilePathFromDevice(ctx, filename);
        if (path != null) {
            return path;
        } else {
            return Constants.PICTURES_SERVER_PATH + filename;
        }
    }

    private static String loadImageFilePathFromDevice(Context ctx, String filename) {
        if (filename == null) return null;
        File file = new File(ctx.getFilesDir(), filename);
        if (file.exists())
            return file.getPath();
        return null;
    }

    private void deleteImageFile(Context ctx, String filename) {
        File file = new File(ctx.getFilesDir(), filename);
        file.delete();
    }

    public static List<Story> getUserDraftStoriesStoredOnDevice(Context ctx, User user) {
        List<Story> result = new ArrayList<>();
        Map<String,?> keys = ctx.getSharedPreferences("stories_data", Context.MODE_PRIVATE).getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            if (Long.valueOf(entry.getKey()) > TEMP_DEVICE_STORY_ID_INDEX) {
                Story story = new Gson().fromJson((String) entry.getValue(), Story.class);
                if (story.getAuthor().getId() == user.getNumberId())
                    result.add(story);
            }
        }
        return result;
    }

    public static List<Story> getAllUserStoriesStoredOnDevice(Context ctx, User user) {
        List<Story> result = new ArrayList<>();
        Map<String,?> keys = ctx.getSharedPreferences("stories_data", Context.MODE_PRIVATE).getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Story story = new Gson().fromJson((String) entry.getValue(), Story.class);
            if (story.getAuthor().getId() == user.getNumberId())
                result.add(story);

        }
        return result;
    }

    private List<String> getAllPictures() {
        List<String> result = new ArrayList<>();
        if (this.getThumbnail() != null)
            result.add(this.getThumbnail());
        for (ContentSection section : getContent()) {
            if (section.getType() == Constants.HEADER_SECTION) {
                if (section.getLink() != null && section.getLink().length() > 0 && !result.contains(section.getLink())) {
                    result.add(section.getLink());
                }
            } else {
                for (ContentItem item : section.getContent()) {
                    if (item.getType() == Constants.PICTURE_CONTAINER) {
                        if (item.getLink() != null && item.getLink().length() > 0 && !result.contains(section.getLink())) {
                            result.add(item.getLink());
                        }
                    }
                }
            }
        }
        return result;
    }

    public void downloadAllStoryPictures(final Context ctx) {
        ImageLoader loader = RequestsSingleton.getInstance(ctx).getImageLoader();
        List<String> imagefilenames = getAllPictures();
        for (final String filename : imagefilenames) {
            File file = new File(ctx.getFilesDir(), filename);
            if (!file.exists()) {
                loader.get(getImagePath(ctx, filename), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        if (response.getBitmap() != null) {
                            File outputFile;
                            Bitmap bitmap = response.getBitmap();
                            FileOutputStream outputStream;
                            try {
                                outputFile = new File(ctx.getFilesDir(), filename);
                                outputStream = new FileOutputStream(outputFile);
                                try {
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                                    //outputStream = new BufferedOutputStream(new FileOutputStream(file));
                                } finally {
                                    outputStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
            }
        }
    }

    public static class ContentSection {
        private long id;
        private int type;
        private String link;
        private Boolean linkUploadState;
        private Location location;
        private ContentItem[] content;

        public ContentSection(int type, Location l) {
            this.id = new Date().getTime();
            this.type = type;
            this.location = l;
        }

        public ContentSection() {
            this.id = new Date().getTime();
        }

        public int getType() { return type; }

        public String getLink() {
            return link;
        }

        public Location getLocation() { return location; }

        public ContentItem[] getContent() {
            if (content==null)
                return new ContentItem[0];
            return content;
        }

        public Boolean isLinkUploaded() {
            return linkUploadState != null && linkUploadState;
        }

        public void setType(int t) { this.type = t; }

        public void setLocation(Location l) { this.location = l; }

        public void setLink( String lnk) {
            this.linkUploadState = false;
            this.link = lnk;
        }

        public void setContent(ContentItem[] cnt) { this.content = cnt; }

        public void setLinkUploadedState( Boolean nup) {
            this.linkUploadState = nup;
        }
    }

    public static class ContentItem {
        private int type;
        private String text;
        private String link;
        private String position;
        private Boolean linkUploadState;

        public int getType() { return type; }

        public String getText() { return text; }

        public String getPosition() {
            return position;
        }

        public String getLink() {
            return link;
        }

        public Boolean isLinkUploaded() {
            return linkUploadState != null && linkUploadState;
        }

        public void setType(int t) { this.type = t; }

        public void setText(String txt) { this.text = txt; }

        public void setPosition(String pstn) {
            this.position = pstn;
        }

        public void setLink( String lnk) {
            this.linkUploadState = false;
            this.link = lnk;
        }

        public void setLinkUploadedState( Boolean nup) {
            this.linkUploadState = nup;
        }
    }
}
