package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReadSingleStoryActivity extends AppCompatActivity implements DeleteStoryDialogFragment.NoticeDialogListener {
    private Story story;
    private LinearLayout authorDetailsViewGroup, locationBannerView, storyContentLayout;
    private TextView storyTextTV, storyLocationTV, authorFullNameTV, nolikeslabel, nobookmarkslabel, noviewslabel;
    private ImageView storyPicturelIV;
    private CircleImageView authorAvatarIV;
    private ImageView likebutton, bookmarkbutton;
    private MenuItem actionedititem, actiondeleteitem,actionshareitem,actionlike,actionbookmark;
    private Activity _this = this;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_single_story);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        storyContentLayout = (LinearLayout) findViewById(R.id.story_content_layout);
        authorDetailsViewGroup = (LinearLayout) findViewById(R.id.author_details_viewgroup);
        storyTextTV = (TextView) findViewById(R.id.story_text_view);
        storyLocationTV = (TextView) findViewById(R.id.location_name_label);
        authorFullNameTV = (TextView) findViewById(R.id.author_full_name_view);
        nolikeslabel = (TextView) findViewById(R.id.no_likes_label);
        nobookmarkslabel = (TextView) findViewById(R.id.no_bookmark_label);
        noviewslabel = (TextView) findViewById(R.id.no_views_label);
        storyPicturelIV = (ImageView) findViewById(R.id.story_picture_view);
        authorAvatarIV = (CircleImageView) findViewById(R.id.author_avatar_view);
        likebutton = (ImageView) findViewById(R.id.like_button);
        bookmarkbutton = (ImageView) findViewById(R.id.bookmark_button);
        locationBannerView = (LinearLayout) findViewById(R.id.location_banner_view);


        Long storyid = (Long) getIntent().getLongExtra("storyid", -1);
        if (storyid > -1) {
            story = new Story();
            story.setId(storyid);
            story.loadStory(_this,false);
        } else {
            story = (Story) getIntent().getParcelableExtra("story_item");
            story.sendReadTrigger(_this);
            readStoryAndBuildContentOnView();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onStoryLoadFinished() {
        readStoryAndBuildContentOnView();
    }

    public void onConnectionRestablished() {   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_story_ab_menu, menu);
        actionedititem = menu.findItem(R.id.action_edit);
        actiondeleteitem = menu.findItem(R.id.action_delete);
        actionshareitem = menu.findItem(R.id.action_share_link);
        actionlike = menu.findItem(R.id.action_like);
        actionbookmark = menu.findItem(R.id.action_bookmark);
        if (story != null && story.canCurrentUserEdit()) {
            actionedititem.setVisible(true);
            actiondeleteitem.setVisible(true);
            actionshareitem.setVisible(true);

        } else {
            actionlike.setVisible(true);
            actionbookmark.setVisible(true);
            setNoLikesLabelView(story.getNoLikes(),story.getCurrentUserLikesStory());
            setNoBookmarksLabelView(story.getNoOfSaves(), story.getCurrentUserSavedStory());
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(getBaseContext(), CreateSingleStoryActivity.class);
                intent.putExtra("storyid", story.getId());
                startActivity(intent);
                finish();
                return true;
            case R.id.action_delete:
                DialogFragment dialog = new DeleteStoryDialogFragment();
                dialog.show(getSupportFragmentManager(), "DeleteStoryDialogFragment");
                return true;
            case R.id.action_share_link:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Constants.READ_STORY_API_ENTRY + story.getId());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.dialog_share_link)));
                return true;
            case R.id.action_like:
                story.sendLike(_this,null);
                return true;
            case R.id.action_bookmark:
                story.sendBookmark(_this,null);
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
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


    private void readStoryAndBuildContentOnView() {

        if (story.getSummary() == null || story.getSummary().length() == 0)
            storyTextTV.setVisibility(View.GONE);
        else
            storyTextTV.setText(story.getSummary());

        if (story.getLocation() != null) {
            storyLocationTV.setText(story.getLocationName());
            setOnClickStartNavigationListner(locationBannerView,story.getLocation());
        } else {
            storyLocationTV.setText("no location");
        }


        authorFullNameTV.setText(story.getAuthor().getFullName());
        Glide.with(_this).load(story.getUserAvatar()).dontAnimate()
                .placeholder(R.drawable.placeholder_avatar)
                .into(authorAvatarIV);

        authorDetailsViewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                User user = User.getCurrentUser(_this);
                if (story.getAuthor().getId() == user.getNumberId())
                    startActivity(intent);
                else {
                    intent.putExtra("user_id", story.getAuthor().getId());
                    startActivity(intent);
                }
            }
        });

        if (story.getThumbnail() != null && story.getThumbnail().length() > 0) {
            Glide.with(_this).load(Story.getImagePath(_this, story.getThumbnail()))
                    .centerCrop()
                    .into(storyPicturelIV);
            storyPicturelIV.setVisibility(View.VISIBLE);
        } else {
            storyPicturelIV.setVisibility(View.GONE);
        }


        setNoLikesLabelView(story.getNoLikes(), story.getCurrentUserLikesStory());
        likebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                story.sendLike(_this,null);
            }
        });

        setNoBookmarksLabelView(story.getNoOfSaves(), story.getCurrentUserSavedStory());
        bookmarkbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                story.sendBookmark(_this,null);
            }
        });

        noviewslabel.setText(story.getNOViews().toString() + " " + getString(R.string.read_no_views_label));

        storyContentLayout.setVisibility(View.VISIBLE);
    }

    private void setOnClickStartNavigationListner(View v, final Location l) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), MapActivity.class);
                i.putExtra("location", (Parcelable) l);
                startActivity(i);
            }
        });
    }

    public void setNoLikesLabelView(Integer nolikes, Boolean liked) {
        if (liked) {
            if (actionlike != null) actionlike.setIcon(R.drawable.ic_favorite_white_48dp);
            likebutton.setImageResource(R.drawable.ic_favorite_black_48dp);
        } else {
            if (actionlike != null) actionlike.setIcon(R.drawable.ic_favorite_border_white_48dp);
            likebutton.setImageResource(R.drawable.ic_favorite_border_black_48dp);
        }
        nolikeslabel.setText(nolikes.toString() + " " + getString(R.string.read_no_likes_label));
    }

    public void setNoBookmarksLabelView(Integer nobookmarks, Boolean bookmarked) {
        if (bookmarked) {
            if (actionbookmark != null) actionbookmark.setIcon(R.drawable.ic_bookmark_white_48dp);
            bookmarkbutton.setImageResource(R.drawable.ic_bookmark_black_48dp);
        } else {
            if (actionbookmark != null) actionbookmark.setIcon(R.drawable.ic_bookmark_border_white_48dp);
            bookmarkbutton.setImageResource(R.drawable.ic_bookmark_border_black_48dp);
        }
        nobookmarkslabel.setText(nobookmarks.toString() + " " + getString(R.string.read_no_bookmarks_label));
    }

    public void onStoryDeleteFinished() {
        finish();
    }
}
