package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by jose on 01/08/16.
 */
public class PostActivity extends AppCompatActivity {
    private Story story;
    private Location currentLocation;
    private EditText storyTitleET, storySummaryET;
    private CheckBox facebookShareCheckBox, twitterShareCheckBox;
    private ImageView storyThumbnailView;
    private LinearLayout locationView;
    private TextView locationLabel;
    private Activity _this = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: keep layout after changing orientation
        setContentView(R.layout.activity_post_story);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        story = (Story) getIntent().getParcelableExtra("story");
        currentLocation = (Location) getIntent().getParcelableExtra("current_location");

        storyThumbnailView = (ImageView) findViewById(R.id.story_thumbnail_view);
        Glide.with(this).load(Story.getImagePath(_this, story.getThumbnail())).placeholder(R.drawable.placeholder_picture).into(storyThumbnailView);
        storyThumbnailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SelectImagesGalleryActivity.class);
                i.putExtra("single_pick", true);
                startActivityForResult(i, Constants.RESULT_STORY_THUMBNAIL_CHOOSEN);
            }
        });

        storyTitleET = (EditText) findViewById(R.id.story_title_textedit);
        if (story.getTitle() != null)
            storyTitleET.setText(story.getTitle());

        storySummaryET = (EditText) findViewById(R.id.story_summary_textedit);
        if (story.getSummary() != null)
            storySummaryET.setText(story.getSummary());

        locationLabel = (TextView) findViewById(R.id.story_location_label);
        locationView = (LinearLayout) findViewById(R.id.story_location_view);
        String locationame = (story.getLocationName() != null) ? story.getLocationName() : getResources().getString(R.string.location_name_hint);
        locationLabel.setText(locationame);
        locationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SelectLocationActivity.class);
                if (story.getLocation() != null)
                    i.putExtra("story_location", (Parcelable) story.getLocation());
                i.putExtra("current_location", (Parcelable) currentLocation);
                startActivityForResult(i, Constants.RESULT_STORY_LOCATION_SUBMITED);
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.post_share_scope_options_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.post_story_share_scope_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    public void onStoryPublishFinished() {
        Intent intent = new Intent(getBaseContext(), ReadStoryActivity.class);
        intent.putExtra("story_item", (Parcelable) story);
        startActivity(intent);
        finish();
    }

    public void onStoryPublishFailed() {
        story.setPublished(0);
        story.saveStory(_this);
    }

    public void onConnectionRestablished() {

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_story_ab_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                story.setTitle(storyTitleET.getText().toString());
                story.setSummary(storySummaryET.getText().toString());
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
                story.setTitle(storyTitleET.getText().toString());
                story.setSummary(storySummaryET.getText().toString());
                story.saveStory(_this);
                return true;
            case android.R.id.home:
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.RESULT_STORY_THUMBNAIL_CHOOSEN:
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        ArrayList<String> submitedimages = data.getStringArrayListExtra("imagelinks");
                        story.saveThumbnail(_this, submitedimages.get(0),storyThumbnailView);
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
                        locationLabel.setText(l.name);
                        story.addStoryLocation(l);
                    }
                }
                break;
        }
    }
}
