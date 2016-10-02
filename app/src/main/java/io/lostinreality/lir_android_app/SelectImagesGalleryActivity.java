package io.lostinreality.lir_android_app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * Created by jose on 06/06/16.
 */
public class SelectImagesGalleryActivity extends AppCompatActivity {

    /** The images. */
    private ArrayList<String> images;
    private GridView gallery;
    private ImageAdapter galleryAdapter;
    private Button submitButton;
    private ArrayList<String> submitImagesList = new ArrayList<String>();
    private Boolean submitWithLocation = false;
    private int[] cursorposition;
    private SelectImagesGalleryActivity _this = this;
    private Boolean isPostActivity = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_images_gallery);
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {
            cursorposition = getIntent().getExtras().getIntArray("cursor_position");
            isPostActivity = getIntent().getExtras().getBoolean("single_pick", false);
        }

        if (isPostActivity)
            findViewById(R.id.select_picture_options_view).setVisibility(View.GONE);

        gallery = (GridView) findViewById(R.id.images_gallery_grid_view);

        if (Utils.mayRequestMedia(_this,gallery)) {
            galleryAdapter = new ImageAdapter(this);
            gallery.setAdapter(galleryAdapter);


            CheckBox submitWithLocationCheckBox = (CheckBox) findViewById(R.id.picture_location_checkBox);
            if (submitWithLocationCheckBox != null) {
                submitWithLocationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        submitWithLocation = isChecked;
                        galleryAdapter.resetRadioAndImageViews();
                        resetSubmitImagesList();
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_images_ab_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                Intent resultIntent = new Intent();
                resultIntent.putExtra("imagelinks",getSubmitImagesList());
                resultIntent.putExtra("add_pictures_with_location",submitWithLocation);
                resultIntent.putExtra("cursor_position",cursorposition);
                setResult(RESULT_OK,resultIntent);
                finish();
                return true;
            case android.R.id.home:
                Intent upIntent = new Intent();
                setResult(RESULT_CANCELED, upIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gallery.setAdapter(new ImageAdapter(this));
            }
        }
    }

    /**
     * The Class ImageAdapter.
     */
    private class ImageAdapter extends BaseAdapter {

        /**
         * The context.
         */
        private Activity context;
        private List<RadioButton> selectedRB = new ArrayList<>();
        private List<View> selectedIV = new ArrayList<>();

        /**
         * Instantiates a new image adapter.
         *
         * @param localContext the local context
         */
        public ImageAdapter(Activity localContext) {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public void resetRadioAndImageViews() {
            for (RadioButton rb : selectedRB) {
                rb.setChecked(false);
            }
            for (View iv : selectedIV) {
                iv.setBackground(null);;
            }
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View galleryItemView = convertView;
            if (galleryItemView == null) {
                galleryItemView = inflater.inflate(R.layout.gallery_picture_item, null, false);
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                galleryItemView.setLayoutParams(new GridView.LayoutParams(metrics.widthPixels/2, metrics.widthPixels/2));

            }
            final ImageView imageView = (ImageView) galleryItemView.findViewById(R.id.image_view);
            final LinearLayout locationindicator = (LinearLayout) galleryItemView.findViewById(R.id.location_indicator_view);
            Glide.with(context).load(images.get(position))
                    .placeholder(R.drawable.placeholder_picture)
                    .into(imageView);

            try {
                ExifInterface exif = new ExifInterface(images.get(position));
                if (exif.getLatLong(new float[2])) {
                    locationindicator.setVisibility(View.VISIBLE);
                } else {
                    locationindicator.setVisibility(View.GONE);
                }
            } catch (IOException e) {
                locationindicator.setVisibility(View.GONE);
            }

            final RadioButton selectImageButton = (RadioButton) galleryItemView.findViewById(R.id.select_image_radio_button);
            if (isImageSelected(images.get(position))) {
                selectImageButton.setChecked(true);
                galleryItemView.setBackground(getResources().getDrawable(R.drawable.picture_rectangle));
            } else {
                selectImageButton.setChecked(false);
                galleryItemView.setBackground(null);
            }

            //EVENT LISTENERS
            galleryItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!submitWithLocation) {
                        resetRadioAndImageViews();
                        resetSubmitImagesList();
                    }
                    if (!isImageSelected(images.get(position))) {
                        addImageToSubmitImagesList(images.get(position));
                        selectImageButton.setChecked(true);
                        v.setBackground(getResources().getDrawable(R.drawable.picture_rectangle));
                        selectedRB.add(selectImageButton);
                        selectedIV.add(v);
                    } else {
                        removeImageToSubmitImagesList(images.get(position));
                        selectImageButton.setChecked(false);
                        v.setBackground(null);
                    }

                }
            });
            return galleryItemView;
        }
    }
    /**
     * Getting All Images Path.
     *
     * @param activity
     *            the activity
     * @return ArrayList with images Path
     */
    private static ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN};

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }


    public void addImageToSubmitImagesList(String link) {
        submitImagesList.add(link);
    }

    public void removeImageToSubmitImagesList(String link) {
        submitImagesList.remove(submitImagesList.indexOf(link));
    }

    public void resetSubmitImagesList() {
        submitImagesList.clear();
    }

    public ArrayList<String> getSubmitImagesList() {
        return submitImagesList;
    }

    public Boolean isImageSelected(String image) {
        if (submitImagesList.contains(image))
            return true;
        return false;
    }


}
