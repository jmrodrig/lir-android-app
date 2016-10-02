package io.lostinreality.lir_android_app;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by jose on 13/09/16.
 */
public class ChooseStoryFormatDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogview = inflater.inflate(R.layout.dialog_choose_story_format, null);

        LinearLayout openStoryBtn = (LinearLayout) dialogview.findViewById(R.id.open_story_button);
        openStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogOpenStoryClick(ChooseStoryFormatDialogFragment.this);
            }
        });
        LinearLayout singleStoryBtn = (LinearLayout) dialogview.findViewById(R.id.single_story_button);
        singleStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogSingleStoryClick(ChooseStoryFormatDialogFragment.this);
            }
        });
        builder.setView(dialogview);
        return builder.create();
    }

    public interface NoticeDialogListener {
        public void onDialogSingleStoryClick(DialogFragment dialog);
        public void onDialogOpenStoryClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

}
