package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.ExifInterface;
import android.os.Build;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
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
public class CreateOpenStoryActivity extends AppCompatActivity implements DeleteStoryDialogFragment.NoticeDialogListener {
    private Story story;
    private LinearLayout storyContentLayout;
    private ImageView newSectionButton;
    private ImageView addPicturesButton;
    private ImageView addLocationButton,formatTextButton;
    private RequestQueue queue;
    private Random r = new Random();
    private Activity _this = this;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_open_story);

        sharedPref = getSharedPreferences("app_data", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        storyContentLayout = (LinearLayout) findViewById(R.id.story_content_layout);

        newSectionButton = (ImageView) findViewById(R.id.new_section_button);
        addPicturesButton = (ImageView) findViewById(R.id.add_picture_button);
        addLocationButton = (ImageView) findViewById(R.id.add_location_button);
        formatTextButton = (ImageView) findViewById(R.id.format_text_button);

        //EVENT LISTENERS
        newSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSectionAfterCursorPosition();
            }
        });

        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SelectLocationActivity.class);
                int[] cursorPosition = getCursorPosition();
                if (cursorPosition != null) {
                    i.putExtra("cursor_position", cursorPosition);
                    ViewGroup currentsection = (ViewGroup) storyContentLayout.getChildAt(cursorPosition[0]);
                    Story.ContentSection sectionobj = (Story.ContentSection) currentsection.getTag(R.id.section_object);
                    if (sectionobj != null && sectionobj.getLocation() != null) {
                        i.putExtra("section_location", (Parcelable) sectionobj.getLocation());
                        i.putExtra("update_location", true);
                        startActivityForResult(i, Constants.REQUEST_CODE_LOCATION_UPDATED);
                    } else {
                        i.putExtra("current_location", (Parcelable) Utils.getLastLocation(_this));
                        startActivityForResult(i, Constants.RESULT_LOCATION_SUBMITED);
                    }
                }
            }
        });

        addPicturesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SelectImagesGalleryActivity.class);
                int[] cursorPosition = getCursorPosition();
                if (cursorPosition != null) {
                    i.putExtra("cursor_position", cursorPosition);
                    startActivityForResult(i, Constants.RESULT_IMAGES_SUBMITED);
                }
            }
        });

        formatTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTextSizeOfCurrentSelectedTextView();
            }
        });

        Long storyid = (Long) getIntent().getLongExtra("storyid", -1);
        story = new Story();

        if (savedInstanceState != null) {
            story.setId((Long) savedInstanceState.getLong("storyid"));
            story.loadStory(_this, true);
        } else if (storyid != -1) {
            story.setId(storyid);
            story.loadStory(_this,true);
        } else {
            story.setFormat(Constants.STORY_FORMAT_OPEN);
            story.createStory(_this);
            addSection(null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (story != null) {
            story.setContent(compileStoryContent());
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
        getMenuInflater().inflate(R.menu.create_story_ab_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                Intent intent = new Intent(getBaseContext(), PostActivity.class);
                story.setContent(compileStoryContent());
                intent.putExtra("story", (Parcelable) story);
                intent.putExtra("current_location", (Parcelable) Utils.getLastLocation(_this));
                startActivityForResult(intent, Constants.REQUEST_CODE_STORY_POST);
                return true;
            case R.id.action_save:
                story.setContent(compileStoryContent());
                story.saveStory(_this);
                return true;
            case R.id.action_delete:
                DialogFragment dialog = new DeleteStoryDialogFragment();
                dialog.show(getSupportFragmentManager(), "DeleteStoryDialogFragment");
                return true;
            case android.R.id.home:
                story.setContent(compileStoryContent());
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
        List<Story.ContentSection> contentSections = story.getContent();
        buildStoryContentLayout(contentSections);
    }

    public void onStoryCreateFinished() {}

    public void onConnectionRestablished() {}

    public void onStoryDeleteFinished() {
        story = null;
        finish();
    }

    private void splitTextEditAtCursorPosition(int[] cursorposition) {
        if (cursorposition == null) {
            cursorposition = getCursorPosition();
        }
        int sectionindex = cursorposition[0];
        int itemindex = cursorposition[1];
        int selstart = cursorposition[1];
        int selend = cursorposition[1];
        ViewGroup focusedsection = (ViewGroup) storyContentLayout.getChildAt(sectionindex);
        View focusedItem = focusedsection.getChildAt(itemindex);
        if (focusedItem instanceof EditText) {
            EditText oldEditText = ((EditText) focusedItem);
            int selStart = oldEditText.getSelectionStart();
            int selEnd = oldEditText.getSelectionEnd();
            CharSequence oldText = oldEditText.getText();
            if (selEnd == selStart) {
                EditText newChildEditText = buildStoryEditText();
                newChildEditText.setBackground(oldEditText.getBackground());
                oldEditText.setText(oldText.subSequence(0, selStart));
                newChildEditText.setText(oldText.subSequence(selStart, oldText.length()).toString());
                focusedsection.addView(newChildEditText, focusedsection.indexOfChild(oldEditText) + 1);
                newChildEditText.requestFocus();
                newChildEditText.setSelection(0);
            } else {
                EditText newFirstChildEditText = buildStoryEditText();
                EditText newLastChildEditText = buildStoryEditText();
                newFirstChildEditText.setBackground(oldEditText.getBackground());
                newLastChildEditText.setBackground(oldEditText.getBackground());
                oldEditText.setText(oldText.subSequence(0, selStart));
                newFirstChildEditText.setText(oldText.subSequence(selStart, selEnd));
                newLastChildEditText.setText(oldText.subSequence(selEnd, oldText.length()));
                focusedsection.addView(newFirstChildEditText, focusedsection.indexOfChild(oldEditText) + 1);
                focusedsection.addView(newLastChildEditText, focusedsection.indexOfChild(newFirstChildEditText) + 1);
                newLastChildEditText.requestFocus();
                newLastChildEditText.setSelection(0);
            }
        }
    }

    private int[] getCursorPosition() {
        int sectionindex, itemindex,selstart,selend;
        ViewGroup focusedsection = (ViewGroup) storyContentLayout.getFocusedChild();
        if (focusedsection == null) return null;
        sectionindex = storyContentLayout.indexOfChild(focusedsection);
        View focuseditem = (View) focusedsection.getFocusedChild();
        itemindex = focusedsection.indexOfChild(focuseditem);
        if (focuseditem instanceof EditText) {
            EditText editText = ((EditText) focuseditem);
            selstart = editText.getSelectionStart();
            selend = editText.getSelectionEnd();
            return new int[]{sectionindex,itemindex,selstart,selend};
        } else {
            return new int[]{sectionindex,itemindex};
        }
    }

    private void joinAllSubsequentEditTextViews() {
        int sectionscount = storyContentLayout.getChildCount();
        for (int i=0; i < sectionscount; i++){
            ViewGroup sectionlayout = (ViewGroup) storyContentLayout.getChildAt(i);
            int itemscount = sectionlayout.getChildCount();
            for (int j=0; j < itemscount; j++){
                View itemview = sectionlayout.getChildAt(j);
                if (itemview instanceof EditText) {
                    int count = 1;
                    View nextitemview = sectionlayout.getChildAt(j+count);
                    while (nextitemview != null && nextitemview instanceof EditText) {
                        CharSequence itemviewtext = ((EditText) itemview).getText();
                        CharSequence nextitemviewtext = ((EditText) nextitemview).getText();
                        ((EditText) itemview).setText(itemviewtext.toString() +  nextitemviewtext.toString());
                        sectionlayout.removeView(nextitemview);
                        ++count;
                        nextitemview = sectionlayout.getChildAt(j+count);
                    }
                }
            }
        }
    }

    private void joinAllSubsequentNoLocationSection() {
        int sectionscount = storyContentLayout.getChildCount();
        for (int i = 0; i < sectionscount; i++) {
            ViewGroup sectionlayout = (ViewGroup) storyContentLayout.getChildAt(i);
            if (sectionlayout.getTag(R.id.section_type) == Constants.SECTION) {
                int count = 1;
                ViewGroup nextsection = (ViewGroup) sectionlayout.getChildAt(i+count);
                while (nextsection.getTag(R.id.section_type) == Constants.SECTION) {
                    int childrencount = nextsection.getChildCount();
                    for (int j=0; j < childrencount; j++) {
                        sectionlayout.addView(nextsection.getChildAt(j));
                    }
                    ++count;
                }
            }
        }
    }

    private void updateLayoutView() {
        //joinAllSubsequentEditTextViews();
        //joinAllSubsequentNoLocationSection();
    }


    private EditText buildStoryEditText() {
        final CustomEditText newET = new CustomEditText(this, null, R.attr.storyTextEditStyle);
        newET.setTag(R.id.item_type, Constants.STORY_TEXT);
        //newET.setBackgroundColor(Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
        newET.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    ViewGroup sectionparent = (ViewGroup) v.getParent();
                    int sectionchildrencount = sectionparent.getChildCount();
                    CharSequence etext = ((EditText) v).getText();
                    if (sectionchildrencount == 1 && etext.length() == 0) {
                        int sectionindex = storyContentLayout.indexOfChild(sectionparent);
                        if (storyContentLayout.getChildCount() > 1) {
                            removeSection(sectionindex);
                        }
                    } else if (((EditText) v).getSelectionStart() == 0) {
                        int etindex = sectionparent.indexOfChild(v);
                        if (etindex > 0) {
                            View newFocusedView = sectionparent.getChildAt(etindex - 1);
                            if (newFocusedView instanceof EditText) {
                                EditText newFocusedET = (EditText) newFocusedView;
                                int cursorposition = newFocusedET.getText().length();
                                newFocusedET.setText(newFocusedET.getText().append(etext));
                                sectionparent.removeView(v);
                                newFocusedET.requestFocus();
                                newFocusedET.setSelection(cursorposition);
                            }
                        }
                    }
                }
                return false;
            }
        });
        newET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 1 && s.toString().contains("\n")) {
                    int index = s.toString().indexOf("\n");
                    EditText et = buildStoryEditText();
                    ViewGroup sectionparent = (ViewGroup) newET.getParent();
                    sectionparent.addView(et, sectionparent.indexOfChild(newET) + 1);
                    et.requestFocus();
                    newET.setText(s.subSequence(0, index));
                    if (index < s.length() - 1)
                        et.setText(s.subSequence(index + 1, s.length()));
                }
            }
        });
        return newET;
    }

    private void toggleTextSizeOfCurrentSelectedTextView() {
        int[] positionindex = getCursorPosition();
        if (positionindex == null) return;
        int sectionindex = positionindex[0];
        int itemindex = positionindex[1];
        View selectedview = ((ViewGroup) storyContentLayout.getChildAt(sectionindex)).getChildAt(itemindex);
        if (selectedview instanceof EditText && selectedview.getTag(R.id.item_type) != null) {
            int itemtype = (int) selectedview.getTag(R.id.item_type);
            if (itemtype == Constants.STORY_TEXT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((EditText) selectedview).setTextAppearance(R.style.storySubtitleStyle);
                } else {
                    ((EditText) selectedview).setTextAppearance(this,R.style.storySubtitleStyle);
                }
                selectedview.setTag(R.id.item_type, Constants.STORY_SUBTITLE);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ((EditText) selectedview).setTextAppearance(R.style.storyTextEditStyle);
                } else {
                    ((EditText) selectedview).setTextAppearance(this,R.style.storyTextEditStyle);
                }
                selectedview.setTag(R.id.item_type, Constants.STORY_TEXT);
            }
        }
    }

    private View buildPictureLayoutView(String link, String caption, Boolean save) {
        LinearLayout pictureLayout = new LinearLayout(this,null,R.attr.pictureLayoutStyle);
        pictureLayout.setTag(R.id.item_type, Constants.PICTURE_CONTAINER);
        LayoutParams pictureLayoutparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        pictureLayout.setLayoutParams(pictureLayoutparams);
        pictureLayout.setOrientation(LinearLayout.VERTICAL);
        RelativeLayout pictureInnerLayout = new RelativeLayout(this);
        int heightDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        pictureLayout.addView(pictureInnerLayout, new LayoutParams(LayoutParams.WRAP_CONTENT, heightDP));
        ImageView pictureView = new ImageView(this);
        pictureView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        pictureView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pictureView.setBackgroundColor(getColor(R.color.colorTerciary));
        } else {
            pictureView.setBackgroundColor(getResources().getColor(R.color.colorTerciary));
        }
        pictureInnerLayout.addView(pictureView);
        RelativeLayout pictureOverlayLayout = new RelativeLayout(this);
        pictureOverlayLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        pictureOverlayLayout.setBackgroundResource(R.drawable.picture_rectangle);
        pictureOverlayLayout.setVisibility(View.INVISIBLE);
        pictureInnerLayout.addView(pictureOverlayLayout);
        pictureInnerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View pictoverlay = ((ViewGroup) v).getChildAt(1);
                if(pictoverlay.getVisibility() == View.VISIBLE)
                    pictoverlay.setVisibility(View.INVISIBLE);
                else
                    pictoverlay.setVisibility(View.VISIBLE);
            }
        });
        ImageView removePictureButton = new ImageView(this,null,R.attr.pictureRemoveButtonStyle);
        int sideDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        LayoutParams pictureButtonparams = new LayoutParams(sideDP,sideDP);
        pictureButtonparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        removePictureButton.setLayoutParams(pictureButtonparams);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            removePictureButton.setBackgroundColor(getColor(R.color.colorAccent));
        } else {
            removePictureButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
        pictureOverlayLayout.addView(removePictureButton);
        removePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup piclayout = (ViewGroup) v.getParent().getParent().getParent();
                ViewGroup sectionview = (ViewGroup) piclayout.getParent();
                sectionview.removeView(piclayout);
                updateLayoutView();
            }
        });
        EditText picCaptionEditText = new EditText(this, null, R.attr.pictureCaptionEditStyle);
        picCaptionEditText.setGravity(Gravity.CENTER_HORIZONTAL);
        if (caption != null)
            picCaptionEditText.setText(caption);
        pictureLayout.addView(picCaptionEditText);
        if (save) {
            story.savePicture(_this, link, pictureView);
        } else {
            Glide.with(this).load(Story.getImagePath(_this, link)).into(pictureView);
            pictureLayout.setTag(R.id.item_link, link);
            pictureLayout.setTag(R.id.item_link_uploaded_state, true);
        }
        return pictureLayout;
    }

    public LinearLayout buildLocationBannerLayout(Story.ContentSection section) {
        LinearLayout.LayoutParams outerparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        outerparams.setMargins(0, 0, 0, 40);
        LinearLayout locationBannerOuterLayout = new LinearLayout(this);
        locationBannerOuterLayout.setLayoutParams(outerparams);
        locationBannerOuterLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout locationBannerLayout = new LinearLayout(this,null,R.attr.locationBannerStyle);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        locationBannerLayout.setLayoutParams(params);
        locationBannerLayout.setGravity(Gravity.CENTER_VERTICAL);

        ImageView locationicon = new ImageView(this, null, R.attr.locationIconStyle);
        int iconsize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        locationicon.setLayoutParams(new LinearLayout.LayoutParams(iconsize, iconsize));
        locationBannerLayout.addView(locationicon);
        TextView locationNameTV = new TextView(this,null,R.attr.storyLocationNameStyle);
        locationNameTV.setText(section.getLocation().name);
        locationBannerLayout.addView(locationNameTV);
        locationBannerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup sectionview = (ViewGroup) v.getParent().getParent();
                Story.ContentSection sectionobj = (Story.ContentSection) sectionview.getTag(R.id.section_object);
                Intent i = new Intent(getBaseContext(), SelectLocationActivity.class);
                int sectionindex = storyContentLayout.indexOfChild(sectionview);
                i.putExtra("cursor_position", new int[]{sectionindex});
                i.putExtra("section_location", (Parcelable) sectionobj.getLocation());
                i.putExtra("update_location", true);
                startActivityForResult(i, Constants.REQUEST_CODE_LOCATION_UPDATED);
            }
        });
        locationBannerOuterLayout.addView(locationBannerLayout);
        return locationBannerOuterLayout;
    }

    private LinearLayout addSection(Integer sectionindex) {
        LinearLayout section = new LinearLayout(this,null,R.attr.sectionEditLayoutStyle);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setTag(R.id.section_type, Constants.SECTION);
        Story.ContentSection sectionobject = new Story.ContentSection();
        section.setTag(R.id.section_object, sectionobject);
        EditText newEditText = buildStoryEditText();
        section.addView(newEditText);
        if (sectionindex != null)
            storyContentLayout.addView(section,sectionindex+1);
        else
            storyContentLayout.addView(section);
        newEditText.requestFocus();
        updateLayoutView();
        return section;
    }

    private View addSectionAfterCursorPosition() {
        View focusedsection = (View) storyContentLayout.getFocusedChild();
        return addSection(storyContentLayout.indexOfChild(focusedsection));
    }

    private View addLocationToSection(int sectionindex, Location l, Boolean newsection) {
        LinearLayout section;
        if (newsection)
            section = addSection(sectionindex);
        else
            section = (LinearLayout) storyContentLayout.getChildAt(sectionindex);
        return addLocationToSection(section,l);
    }

    private LinearLayout addLocationToSection(LinearLayout section, Location l) {
        Story.ContentSection sectionobj = (Story.ContentSection) section.getTag(R.id.section_object);
        if (sectionobj == null) {
            sectionobj = new Story.ContentSection();
            section.setTag(R.id.section_object,sectionobj);
        }
        sectionobj.setType(Constants.LOCATION_SECTION);
        sectionobj.setLocation(l);
        section.setTag(R.id.section_type, Constants.LOCATION_SECTION);

        LinearLayout locationbanner = buildLocationBannerLayout(sectionobj);
        section.addView(locationbanner, 0);
        updateLayoutView();
        return section;
    }

    private void addPictureToStory(int[] positionindex, ArrayList<String> imagelinks, Boolean withlocation) {
        int sectionindex = positionindex[0];
        int itemindex = positionindex[1];

        if (withlocation) {
            for (String link : imagelinks) {
                Location l = getLocationFromPicture(link);
                LinearLayout newlocationsection = (LinearLayout) addLocationToSection(sectionindex, l, true);
                ViewGroup pictureLayout = (ViewGroup) buildPictureLayoutView(link,null,true);
                newlocationsection.addView(pictureLayout);
                EditText newEditText = buildStoryEditText();
                newlocationsection.addView(newEditText);
                ++sectionindex;
            }
        } else {
            for (String link : imagelinks) {
                ViewGroup sectionlayout = (ViewGroup) storyContentLayout.getChildAt(sectionindex);
                ViewGroup pictureLayout = (ViewGroup) buildPictureLayoutView(link,null,true);
                sectionlayout.addView(pictureLayout, ++itemindex);
                EditText newEditText = buildStoryEditText();
                sectionlayout.addView(newEditText, ++itemindex);
            }
        }
        updateLayoutView();
    }

    private void updateSectionLocation(int sectionindex, Location l) {
        ViewGroup section = (ViewGroup) storyContentLayout.getChildAt(sectionindex);
        Story.ContentSection sectionobj = (Story.ContentSection) section.getTag(R.id.section_object);
        sectionobj.setLocation(l);
        TextView locationnameTV = (TextView) ((LinearLayout) ((LinearLayout) section.getChildAt(0)).getChildAt(0)).getChildAt(1);
        locationnameTV.setText(l.name);
    }

    private void removeSectionLocation(int sectionindex) {
        ViewGroup section = (ViewGroup) storyContentLayout.getChildAt(sectionindex);
        section.setTag(R.id.section_type, Constants.SECTION);
        Story.ContentSection sectionobj = (Story.ContentSection) section.getTag(R.id.section_object);
        sectionobj.setType(Constants.SECTION);
        sectionobj.setLocation(null);
        section.removeViewAt(0);
        updateLayoutView();
    }

    private void removeSection(Integer sectionindex) {
        View removingsection = storyContentLayout.getChildAt(sectionindex);
        storyContentLayout.removeView(removingsection);
        ViewGroup newFocusedSection;
        if (sectionindex == 0) {
            newFocusedSection = (ViewGroup) storyContentLayout.getChildAt(sectionindex);
        } else {
            newFocusedSection = (ViewGroup) storyContentLayout.getChildAt(sectionindex - 1);
        }
        View newFocusedView = newFocusedSection.getChildAt(newFocusedSection.getChildCount()-1);
        if (newFocusedView instanceof EditText) {
            EditText newFocusedET = (EditText) newFocusedView;
            newFocusedET.requestFocus();
            newFocusedET.setSelection(newFocusedET.getText().length());
        }
    }

    private List<Story.ContentSection> compileStoryContent() {
        int sectionscount = storyContentLayout.getChildCount();
        List<Story.ContentSection> contents = new ArrayList<Story.ContentSection>();
        for (int i=0; i < sectionscount; i++){
            ViewGroup sectionlayout = (ViewGroup) storyContentLayout.getChildAt(i);
            Story.ContentSection sectionobj = (Story.ContentSection) sectionlayout.getTag(R.id.section_object);
            int itemscount = sectionlayout.getChildCount();
            List<Story.ContentItem> sectionitems = new ArrayList<Story.ContentItem>();
            for (int j=0; j < itemscount; j++){
                View itemview = sectionlayout.getChildAt(j);
                if (itemview.getTag(R.id.item_type) != null) {
                    Story.ContentItem itemcontent = new Story.ContentItem();
                    itemcontent.setType((int) itemview.getTag(R.id.item_type));
                    switch (itemcontent.getType()) {
                        case Constants.STORY_TEXT:
                            itemcontent.setText(((EditText) itemview).getText().toString());
                            break;
                        case Constants.PICTURE_CONTAINER:
                            itemcontent.setLink((String) itemview.getTag(R.id.item_link));
                            itemcontent.setLinkUploadedState((Boolean) itemview.getTag(R.id.item_link_uploaded_state));
                            String caption = ((EditText) ((ViewGroup) itemview).getChildAt(1)).getText().toString();
                            itemcontent.setText(caption);
                            break;
                        case Constants.STORY_SUBTITLE:
                            itemcontent.setText(((EditText) itemview).getText().toString());
                            break;
                    }
                    sectionitems.add(itemcontent);
                }
            }
            Story.ContentItem[] sectionitemsarray = new Story.ContentItem[sectionitems.size()];
            sectionitemsarray = sectionitems.toArray(sectionitemsarray);
            sectionobj.setContent(sectionitemsarray);
            contents.add(sectionobj);
        }
        return contents;
    }

    private void buildStoryContentLayout(List<Story.ContentSection> content) {
        storyContentLayout.removeAllViews();
        if (content == null) return;
        for (Story.ContentSection sectionobject : content) {
            if (sectionobject.getType() != Constants.HEADER_SECTION) {
                LinearLayout sectionlayout = new LinearLayout(this,null,R.attr.sectionEditLayoutStyle);
                sectionlayout.setOrientation(LinearLayout.VERTICAL);
                sectionlayout.setTag(R.id.section_type, Constants.SECTION);
                sectionlayout.setTag(R.id.section_object, sectionobject);
                storyContentLayout.addView(sectionlayout);
                if (sectionobject.getType() == Constants.LOCATION_SECTION)
                    addLocationToSection(sectionlayout, sectionobject.getLocation());
                for (int j = 0; j < sectionobject.getContent().length; j++) {
                    Story.ContentItem itemcontent = sectionobject.getContent()[j];
                    switch (itemcontent.getType()) {
                        case Constants.STORY_TEXT:
                            EditText newEditText = buildStoryEditText();
                            newEditText.setText(itemcontent.getText());
                            newEditText.setTag(R.id.item_type, Constants.STORY_TEXT);
                            sectionlayout.addView(newEditText);
                            break;
                        case Constants.PICTURE_CONTAINER:
                            ViewGroup pictureLayout = (ViewGroup) buildPictureLayoutView(itemcontent.getLink(), itemcontent.getText(), false);
                            pictureLayout.setTag(R.id.item_type, Constants.PICTURE_CONTAINER);
                            sectionlayout.addView(pictureLayout);
                            break;
                        case Constants.STORY_SUBTITLE:
                            EditText newEditSubtitle = buildStoryEditText();
                            newEditSubtitle.setText(itemcontent.getText());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                newEditSubtitle.setTextAppearance(R.style.storySubtitleStyle);
                            } else {
                                newEditSubtitle.setTextAppearance(this,R.style.storySubtitleStyle);
                            }
                            newEditSubtitle.setTag(R.id.item_type, Constants.STORY_SUBTITLE);
                            sectionlayout.addView(newEditSubtitle);
                            break;
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.RESULT_IMAGES_SUBMITED:
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        ArrayList<String> submitedimages = data.getStringArrayListExtra("imagelinks");
                        int[] cursorposition = data.getExtras().getIntArray("cursor_position");
                        if (data.getExtras().getBoolean("add_pictures_with_location")) {
                            addPictureToStory(cursorposition, submitedimages, true);
                        } else {
                            //splitTextEditAtCursorPosition(cursorposition);
                            addPictureToStory(cursorposition, submitedimages, false);
                        }
                    } else {
                        Toast.makeText(this, "You haven't picked Image",
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case Constants.RESULT_LOCATION_SUBMITED:
                if (resultCode == RESULT_OK) {
                    Location l = (Location) data.getExtras().getParcelable("location");
                    int[] cursorposition = data.getExtras().getIntArray("cursor_position");
                    addLocationToSection(cursorposition[0], l, false);
                }
                break;
            case Constants.REQUEST_CODE_LOCATION_UPDATED:
                if (resultCode == RESULT_OK) {
                    int[] cursorpositionup = data.getExtras().getIntArray("cursor_position");
                    Location lup = (Location) data.getExtras().getParcelable("location");
                    if (lup == null)
                        removeSectionLocation(cursorpositionup[0]);
                    else
                        updateSectionLocation(cursorpositionup[0], lup);
                }
                break;
            case Constants.REQUEST_CODE_STORY_POST:
                if (resultCode == RESULT_OK) {
                    storyContentLayout.removeAllViews();
                    story.loadStory(_this,false);
                }
                break;
        }
    }

    private Location getLocationFromPicture(String link) {
        try {
            ExifInterface exif = new ExifInterface(link);
            float[] latlngdata = new float[2];
            if (exif.getLatLong(latlngdata)) {
                Double lat = ((Float) latlngdata[0]).doubleValue();
                Double lng = ((Float) latlngdata[1]).doubleValue();
                return new Location(
                        lat,
                        lng,
                        lat + ", " + lng,
                        Constants.DEFAULT_RADIUS,
                        Constants.DEFAULT_ZOOM);
            }
        } catch (IOException e) {
            // do something
        }
        return new Location(
                Constants.DEFAULT_LATITUDE,
                Constants.DEFAULT_LONGITUDE,
                Constants.DEFAULT_LOCATION_NAME,
                Constants.DEFAULT_RADIUS,
                Constants.DEFAULT_ZOOM);
    }


    //TODO: add drawables: location pin, e para os botões

    //TODO: método para aumentar/reduzir o tamanho do texto

    //TODO: método para eliminar fotos da galeria

    //TODO: método para adicionar/eliminar caption nas fotos

    //TODO: método para fullscreen das fotos

    //TODO: método para converter todo o conteúdo em xml

    //TODO: método para enviar para servidor (gravar em story content, zippar tudo e enviar)

    //TODO: método para autosave (gravar em draft, TextWatcher class???)

}
