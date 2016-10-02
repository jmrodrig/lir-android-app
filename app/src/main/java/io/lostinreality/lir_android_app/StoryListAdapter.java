package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by jose on 11/07/16.
 */
public class StoryListAdapter extends RecyclerView.Adapter<StoryListAdapter.StoryViewHolder> {
    private List<Story> stories = new ArrayList<>();
    private Context context;


    public StoryListAdapter(Context ctx) {
        context = ctx;
    }

    public void setStoryList(List<Story> sts) {
        stories = sts;
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    @Override
    public void onBindViewHolder(final StoryViewHolder holder, int i) {
        final Story story = stories.get(i);

        if (story.isDummy()) {
            // Dummy Story row
            holder.distanceLabelTextView.setText("DISTANCE: " + story.getDistance() + "m");
            holder.distanceLabelTextView.setVisibility(View.VISIBLE);
            holder.storyDetailsLayout.setVisibility(View.GONE);
        } else {
            // Story row
            if (story.getFormat() == Constants.STORY_FORMAT_OPEN) {
                holder.storyPictureView.setVisibility(View.GONE);
                holder.storyThumbnailView.setVisibility(View.VISIBLE);
                holder.storyTitleTextView.setVisibility(View.VISIBLE);
                holder.storyTextTextView.setVisibility(View.GONE);
                holder.userNameTextView.setTextColor(context.getColor(R.color.colorContrast));

                if (story.getTitle() == null || story.getTitle().length() == 0) {
                    holder.storyTitleTextView.setText("no title");
                    holder.storyTitleTextView.setTextAppearance(context, R.style.italicTextStyle);
                } else
                    holder.storyTitleTextView.setText(story.getTitle());

                if (story.getSummary() == null || story.getSummary().length() == 0)
                    holder.storySummaryView.setVisibility(View.GONE);
                else {
                    holder.storySummaryView.setText(story.getSummary());
                    holder.storySummaryView.setVisibility(View.VISIBLE);
                }

                Glide.with(context).load(Story.getImagePath(context, story.getThumbnail()))
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_picture)
                        .into(holder.storyThumbnailView);

            } else if (story.getFormat() == Constants.STORY_FORMAT_SINGLE) {
                holder.storyTitleTextView.setVisibility(View.GONE);
                holder.storyThumbnailView.setVisibility(View.GONE);
                holder.storyPictureView.setVisibility(View.GONE);
                holder.storySummaryView.setVisibility(View.GONE);
                holder.userNameTextView.setTextColor(context.getColor(android.R.color.primary_text_light));

                if (story.getSummary() == null || story.getSummary().length() == 0)
                    holder.storyTextTextView.setVisibility(View.GONE);
                else {
                    holder.storyTextTextView.setText(story.getSummary());
                    holder.storyTextTextView.setVisibility(View.VISIBLE);
                }

                if (story.getThumbnail() != null && story.getThumbnail().length() > 0) {
                    Glide.with(context).load(Story.getImagePath(context, story.getThumbnail()))
                            .centerCrop()
                            .placeholder(R.drawable.placeholder_picture)
                            .into(holder.storyPictureView);
                    holder.storyPictureView.setVisibility(View.VISIBLE);
                }
            }

            holder.locationNameTextView.setText(story.getLocationName());
            TypedArray bannerbg = context.getResources().obtainTypedArray(R.array.location_banner_color_array);
            long index = story.getId() % bannerbg.length();
            holder.locationBannerLayout.setBackground(bannerbg.getDrawable((int) index));
            holder.userNameTextView.setText(story.getAuthorName());

            Glide.with(context).load(story.getAuthor().getAvatarUrl()).dontAnimate().placeholder(R.drawable.placeholder_avatar)
                    .into(holder.userImageView);

            //EVENT LISTENERS
            holder.storyLayoutBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i;
                    if (story.getFormat() == Constants.STORY_FORMAT_OPEN)
                        i = new Intent(context, ReadStoryActivity.class);
                    else
                        i = new Intent(context, ReadSingleStoryActivity.class);
                    i.putExtra("story_item", (Parcelable) story);
                    context.startActivity(i);
                }
            });

            holder.setNoLikesLabelView(story.getNoLikes(), story.getCurrentUserLikesStory());
            holder.likeButtonIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    story.sendLike(context, holder);
                }
            });
            holder.setNoBookmarksLabelView(story.getNoOfSaves(), story.getCurrentUserSavedStory());
            holder.bookmarkButtonIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    story.sendBookmark(context, holder);
                }
            });

            holder.noViewsLabel.setText(story.getNOViews()  + " views");

            holder.storyDetailsLayout.setVisibility(View.VISIBLE);
            holder.distanceLabelTextView.setVisibility(View.GONE);
        }


    }

    @Override
    public StoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.story_row, viewGroup, false);
        return new StoryViewHolder(itemView);
    }




    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout storyLayoutBody;
        protected LinearLayout storyDetailsLayout, locationBannerLayout, likeButton, bookmarkButton;
        protected TextView userNameTextView, storyTextTextView, storySummaryView, locationNameTextView, distanceLabelTextView, storyTitleTextView, noLikesLabel, noBookmarksLabel, noViewsLabel;
        protected ImageView storyThumbnailView, storyPictureView, likeButtonIcon, bookmarkButtonIcon;
        protected CircleImageView userImageView;
        protected ProgressBar storyImageProgress;
        protected Story story;

        public StoryViewHolder(View v) {
            super(v);
            storyLayoutBody = (LinearLayout) v.findViewById(R.id.story_card_body);
            distanceLabelTextView = (TextView) v.findViewById(R.id.distance_label);
            storyDetailsLayout = (LinearLayout) v.findViewById(R.id.story_details_layout);
            likeButton = (LinearLayout) v.findViewById(R.id.like_button);
            bookmarkButton = (LinearLayout) v.findViewById(R.id.bookmark_button);
            likeButtonIcon = (ImageView) v.findViewById(R.id.likes_icon);
            bookmarkButtonIcon = (ImageView) v.findViewById(R.id.bookmarks_icon);
            noLikesLabel = (TextView) v.findViewById(R.id.no_likes_label);
            noBookmarksLabel = (TextView) v.findViewById(R.id.no_bookmars_label);
            userNameTextView = (TextView) v.findViewById(R.id.user_name);
            storyTextTextView = (TextView) v.findViewById(R.id.story_text);
            storySummaryView = (TextView) v.findViewById(R.id.story_summary);
            storyThumbnailView = (ImageView) v.findViewById(R.id.story_thumbnail);
            storyPictureView = (ImageView) v.findViewById(R.id.story_picture);
            userImageView = (CircleImageView) v.findViewById(R.id.user_image);
            locationNameTextView = (TextView) v.findViewById(R.id.location_name);
            distanceLabelTextView = (TextView) v.findViewById(R.id.distance_label);
            storyTitleTextView = (TextView) v.findViewById(R.id.story_title_view);
            noViewsLabel = (TextView) v.findViewById(R.id.no_views_label);
            locationBannerLayout = (LinearLayout) v.findViewById(R.id.location_banner_view);
        }

        public void setNoLikesLabelView(Integer nolikes, Boolean liked) {
            if (liked) {
                likeButtonIcon.setImageResource(R.drawable.ic_favorite_black_48dp);
            } else {
                likeButtonIcon.setImageResource(R.drawable.ic_favorite_border_black_48dp);
            }
            noLikesLabel.setText(nolikes.toString() + " likes");
        }

        public void setNoBookmarksLabelView(Integer nobookmarks, Boolean bookmarked) {
            if (bookmarked) {
                bookmarkButtonIcon.setImageResource(R.drawable.ic_bookmark_black_48dp);
            } else {
                bookmarkButtonIcon.setImageResource(R.drawable.ic_bookmark_border_black_48dp);
            }
            noBookmarksLabel.setText(nobookmarks.toString() + " bookmarks");
        }
    }

}
