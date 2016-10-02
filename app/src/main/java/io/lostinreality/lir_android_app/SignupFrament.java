package io.lostinreality.lir_android_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Created by jose on 31/08/16.
 */
public class SignupFrament extends Fragment {
    private LauncherActivity launcheractivity;
    private LinearLayout startsignuplayout;
    private LinearLayout finishsignuplayout;
    private Button submitemailbutton, submitdetailsbutton;
    private EditText signupemailview, fnameview, lnameview, passview, cpassview;
    private TextView signupmessage;
    private Boolean finishlayout;
    private ProgressBar signupprogressview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        launcheractivity = (LauncherActivity) getActivity();
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        startsignuplayout = (LinearLayout) getActivity().findViewById(R.id.start_sign_up_layout);
        finishsignuplayout = (LinearLayout) getActivity().findViewById(R.id.finish_sign_up_layout);
        submitemailbutton = (Button) getActivity().findViewById(R.id.start_sign_up_submit_email_button);
        signupemailview = (EditText) getActivity().findViewById(R.id.start_sign_up_email);
        signupmessage = (TextView) getActivity().findViewById(R.id.start_sign_up_message);
        fnameview = (EditText) getActivity().findViewById(R.id.first_name_view);
        lnameview = (EditText) getActivity().findViewById(R.id.last_name_view);
        passview = (EditText) getActivity().findViewById(R.id.password_view);
        cpassview = (EditText) getActivity().findViewById(R.id.confirm_password_view);
        submitdetailsbutton = (Button) getActivity().findViewById(R.id.submit_details_button);
        signupprogressview = (ProgressBar) getActivity().findViewById(R.id.sign_up_progress_view);

        signupmessage.setVisibility(View.INVISIBLE);

        finishlayout = getArguments().getBoolean("finishlayout");
        if (finishlayout) {
            startsignuplayout.setVisibility(View.GONE);
            finishsignuplayout.setVisibility(View.VISIBLE);
        } else {
            startsignuplayout.setVisibility(View.VISIBLE);
            finishsignuplayout.setVisibility(View.GONE);
        }

        submitemailbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Store values at the time of the login attempt.
                String email = signupemailview.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    signupemailview.setError(getString(R.string.error_field_required));
                    focusView = signupemailview;
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    signupemailview.setError(getString(R.string.error_invalid_email));
                    focusView = signupemailview;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    showProgress(true,startsignuplayout);
                    launcheractivity.startSignupInServer(email);
                }
            }
        });

        submitdetailsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Reset errors.
                fnameview.setError(null);
                lnameview.setError(null);
                passview.setError(null);
                cpassview.setError(null);

                String fname = fnameview.getText().toString();
                String lname = lnameview.getText().toString();
                String pass = passview.getText().toString();
                String cpass = cpassview.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid password, if the user entered one.
                if (!TextUtils.isEmpty(pass) && !isPasswordValid(pass)) {
                    passview.setError(getString(R.string.error_invalid_password));
                    focusView = passview;
                    cancel = true;
                } else  if (!TextUtils.isEmpty(cpass) && !isPasswordConfirmationValidValid(pass, cpass)) {
                    // Check for a valid password confirmation, if the user entered one.
                    cpassview.setError(getString(R.string.error_incorrect_password_confirmation));
                    focusView = cpassview;
                    cancel = true;
                }

                // Check for a valid name address.
                if (TextUtils.isEmpty(fname)) {
                    fnameview.setError(getString(R.string.error_field_required));
                    focusView = fnameview;
                    cancel = true;
                } else if (TextUtils.isEmpty(lname)) {
                    lnameview.setError(getString(R.string.error_field_required));
                    focusView = lnameview;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    showProgress(true,finishsignuplayout);
                    launcheractivity.finishSignupInServer(fname, lname, pass, cpass);
                }
            }
        });

    }

    public void onStartSignUpFinished() {
        signupmessage.setAlpha(0f);
        signupmessage.setVisibility(View.VISIBLE);
        signupmessage.animate().alpha(1f).setDuration(300).start();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show, final View signupformview) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            signupformview.setVisibility(show ? View.GONE : View.VISIBLE);
            signupformview.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    signupformview.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            signupprogressview.setVisibility(show ? View.VISIBLE : View.GONE);
            signupprogressview.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    signupprogressview.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            signupprogressview.setVisibility(show ? View.VISIBLE : View.GONE);
            signupformview.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 8;
    }

    private boolean isPasswordConfirmationValidValid(String password, String pconfirmation) {
        //TODO: Replace this with your own logic
        return password.equals(pconfirmation);
    }

}
