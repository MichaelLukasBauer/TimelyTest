package de.opti4apps.timelytest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.opti4apps.timelytest.data.User;
import de.opti4apps.timelytest.data.UserManager;
import de.opti4apps.timelytest.data.User_;
import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.gesture.GestureTracker;
import io.objectbox.Box;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();
    @BindView(R.id.email)
    AutoCompleteTextView mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    @BindView(R.id.email_sign_in_button)
    Button sendButton;
    private Box<User> usersBox;
    private User currentUser;

    private TrackerHelper tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tracker = new TrackerHelper(TAG,this);
        tracker.onStartTrack(TrackerHelper.SIGN_IN,"","");
        usersBox = ((App) getApplication()).getBoxStore().boxFor(User.class);
        if (UserManager.checkIsUserSignedIn(usersBox)) {
            currentUser = UserManager.getSignedInUser(usersBox);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userEmail", currentUser.getEmail());
            LoginActivity.this.startActivity(intent);
        } else {
            mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
            mPasswordView = (EditText) findViewById(R.id.password);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);
        }
    }


    private void attemptLogin() {

        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            if (UserManager.checkUserCredentials( usersBox, email, password)) {
                currentUser = UserManager.getUserByEmail(usersBox, email);
                UserManager.changeUserSignedInStatus(currentUser,usersBox);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("userEmail", email);
                tracker.onStopTrack("","","");
                LoginActivity.this.startActivity(intent);
            } else {
                showProgress(false);
                mPasswordView.setError(getString(R.string.error_login_failed));
                mPasswordView.requestFocus();
            }
        }
    }

    private boolean isEmailValid(String email) {

        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {

        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @OnClick({R.id.email,R.id.password,R.id.email_sign_in_button})
    public void clickElements(View v) {
        int mSelectedText;
        mSelectedText = v.getId();
        if (mSelectedText == R.id.email)
        {
            tracker.interactionTrack(this.findViewById(R.id.email), tracker.getInteractionClicID(),"","","");
        }
        else if (mSelectedText == R.id.password)
        {
            tracker.interactionTrack(this.findViewById(R.id.password), tracker.getInteractionClicID(),"","","");
        }
        else if (mSelectedText == R.id.email_sign_in_button)
        {
            tracker.interactionTrack(this.findViewById(R.id.email_sign_in_button), tracker.getInteractionClicID(),"",TrackerHelper.SIGN_IN,"");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        GestureTracker.trackGesture(this,event,(ViewGroup)findViewById(android.R.id.content));
        return true;
    }
}

