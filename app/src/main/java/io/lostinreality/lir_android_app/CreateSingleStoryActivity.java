package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by jose on 06/06/16.
 */
public class CreateSingleStoryActivity extends AppCompatActivity implements DeleteStoryDialogFragment.NoticeDialogListener {
    private Story story;
    private EditText storyEditTextView;
    private TextView locationNameTextView;
    private LinearLayout addPicturesButton, chooseLocationButton, storyContentLayout;
    private RelativeLayout pictureContainerView;
    private ImageView removePictureButton;
    private ImageView storyPictureView;
    private Location currentLocation;
    private Activity _this = this;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_single_story);

        sharedPref = getSharedPreferences("app_data", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        currentLocation = (Location) getIntent().getParcelableExtra("current_location");

        storyContentLayout = (LinearLayout) findViewById(R.id.story_content_layout);
        storyEditTextView = (EditText) findViewById(R.id.story_text);
        addPicturesButton = (LinearLayout) findViewById(R.id.add_picture_btn);
        chooseLocationButton = (LinearLayout) findViewById(R.id.choose_location_btn);
        storyPictureView = (ImageView) findViewById(R.id.story_picture);
        locationNameTextView = (TextView) findViewById(R.id.location_name);
        removePictureButton = (ImageView) findViewById(R.id.remove_picture_btn);
        pictureContainerView = (RelativeLayout) findViewById(R.id.picture_container_view);


        chooseLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SelectLocationActivity.class);
                if (story.getLocation() != null)
                    i.putExtra("story_location", (Parcelable) story.getLocation());
                i.putExtra("current_location", (Parcelable) currentLocation);
                startActivityForResult(i, Constants.RESULT_STORY_LOCATION_SUBMITED);
            }
        });

        addPicturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SelectImagesGalleryActivity.class);
                i.putExtra("single_pick", true);
                startActivityForResult(i, Constants.RESULT_STORY_THUMBNAIL_CHOOSEN);
            }
        });

        removePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                story.setThumbnail(null);
                pictureContainerView.setVisibility(View.GONE);
                addPicturesButton.setVisibility(View.VISIBLE);
                storyPictureView.setImageURI(null);
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.post_share_scope_options_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.post_story_share_scope_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Long storyid = (Long) getIntent().getLongExtra("storyid", -1);
        story = new Story();

        if (savedInstanceState != null) {
            storyContentLayout.setVisibility(View.INVISIBLE);
            story.setId((Long) savedInstanceState.getLong("storyid"));
            story.loadStory(_this, true);
        } else if (storyid != -1) {
            storyContentLayout.setVisibility(View.INVISIBLE);
            story.setId(storyid);
            story.loadStory(_this,true);
        } else {
            story.setFormat(Constants.STORY_FORMAT_SINGLE);
            story.createStory(_this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (story != null) {
            story.setSummary(storyEditTextView.getText().toString());
            story.saveStory(_this);
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong("storyid", story.getId());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_single_story_ab_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                story.setSummary(storyEditTextView.getText().toString());
                if (story.getLocation() == null) {
                    Toast.makeText(getBaseContext(), R.string.no_location_selected_warning, Toast.LENGTH_LONG).show();
                    return true;
                }
                Spinner publishoptionsspinner =(Spinner) findViewById(R.id.post_share_scope_options_spinner);
                String opt = publishoptionsspinner.getSelectedItem().toString();
                if (opt.equals(getResources().getStringArray(R.array.post_story_share_scope_options)[0]))
                    story.publishStory(_this, Constants.PUBLISH_WITH_FOLLOWERS);
                else if (opt.equals(getResources().getStringArray(R.array.post_story_share_scope_options)[1]))
                    story.publishStory(_this,Constants.PUBLISH_WITH_EVERYONE);
                else if (opt.equals(getResources().getStringArray(R.array.post_story_share_scope_options)[2]))
                    story.publishStory(_this,Constants.PUBLISH_PRIVATELY);
                return true;
            case R.id.action_save:
                story.setSummary(storyEditTextView.getText().toString());
                story.saveStory(_this);
                return true;
            case R.id.action_delete:
                DialogFragment dialog = new DeleteStoryDialogFragment();
                dialog.show(getSupportFragmentManager(), "DeleteStoryDialogFragment");
                return true;
            case android.R.id.home:
                story.setSummary(storyEditTextView.getText().toString());
                story.saveStory(_this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        story.deleteStory(_this);
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        dialog.dismiss();
    }

    public void onStoryLoadFinished() {
        storyEditTextView.setText(story.getSummary());
        if (story.getLocation()!=null) {
            locationNameTextView.setText(story.getLocation().name);
        }
        if (story.getThumbnail()!=null) {
            Glide.with(this).load(Story.getImagePath(_this, story.getThumbnail())).into(storyPictureView);
            pictureContainerView.setVisibility(View.VISIBLE);
            addPicturesButton.setVisibility(View.GONE);
        }
        storyContentLayout.setVisibility(View.VISIBLE);
    }

    public void onStoryCreateFinished() {}

    public void onConnectionRestablished() {}

    public void onStoryDeleteFinished() {
        story = null;
        finish();
    }

    public void onStoryPublishFinished() {
        Intent intent = new Intent(getBaseContext(), ReadSingleStoryActivity.class);
        intent.putExtra("story_item", (Parcelable) story);
        startActivity(intent);
        finish();
    }

    public void onStoryPublishFailed() {
        story.setPublished(0);
        story.saveStory(_this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.RESULT_STORY_THUMBNAIL_CHOOSEN:
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        ArrayList<String> submitedimages = data.getStringArrayListExtra("imagelinks");
                        story.saveThumbnail(_this, submitedimages.get(0),storyPictureView);
                        pictureContainerView.setVisibility(View.VISIBLE);
                        addPicturesButton.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, "You haven't picked Image",
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case Constants.RESULT_STORY_LOCATION_SUBMITED:
                if (resultCode == RESULT_OK) {
                    Location l = (Location) data.getExtras().getParcelable("location");
                    if (l != null) {
                        locationNameTextView.setText(l.name);
                        story.addStoryLocation(l);
                    }
                }
                break;
        }
    }

}
