package de.opti4apps.timelytest;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.opti4apps.timelytest.data.User;
import de.opti4apps.timelytest.data.User_;
import de.opti4apps.timelytest.shared.GMailSender;
import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.gesture.GestureTracker;
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * Created by TCHATCHO on 20.09.2017.
 */

public class SendEmailFragment extends DialogFragment {
    public static final String TAG = SendEmailFragment.class.getSimpleName();
    private static final String ARG_USER_ID = "userID";
    private static final String ARG_MONTH_YEAR = "monthYear";
    private TrackerHelper tracker;

    @BindView(R.id.emailEditText)
    EditText mEmail;


    @BindView(R.id.sendReportButton)
    Button mSendReport;

    Box<User> usersBox;
    User currentUser;

    private Query<User> mUserQuery;
    public SendEmailFragment(){

    }

    public static SendEmailFragment newInstance(long userID,String month_year) {
        SendEmailFragment fragment = new SendEmailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userID);
        args.putString(ARG_MONTH_YEAR,month_year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(User.class);
        tracker = new TrackerHelper(TAG,getContext());

        int style = DialogFragment.STYLE_NORMAL, theme = android.R.style.Theme_Holo_Light_Dialog;
        setStyle(style, theme);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragent_mail_send, container, false);

        ButterKnife.bind(this, view);
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                GestureTracker.trackGesture(getContext(),event,(ViewGroup) v);
                return true;
            }
        });

        getDialog().setTitle("Send report per Email");
        Long userID = getArguments().getLong(ARG_USER_ID);
        mUserQuery = usersBox.query().equal(User_.id, userID).build();
        currentUser = mUserQuery.findUnique();
        mEmail.setText(currentUser.getEmail());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // EventBus.getDefault().register(this);
        tracker.onStartTrack("","",false,false,"");
    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
        tracker.onStopTrack("","",false,false,"");
    }

    @OnClick({R.id.sendReportButton})
    public void SendReport(View v) {
        tracker.interactionTrack(getActivity().findViewById(R.id.sendReportButton), tracker.getInteractionClicID(),"",TrackerHelper.SEND_REPORT,false,true,"");
        sendEmail(mEmail.getText().toString());

    }

    protected void sendEmail(final String receiver) {
        final String  storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/UniTyLab/PDF Files";
        final String filename="UniTyLabEmployeesTimesheet_" + getArguments().getString(ARG_MONTH_YEAR) + ".pdf";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {


//            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                    GMailSender sender = new GMailSender("timely.unitylab@gmail.com","TimelyUnityLab");
                    sender.addAttachment(storagePath +"/" + filename,filename);
                    sender.sendMail(getArguments().getString(ARG_MONTH_YEAR), getArguments().getString(ARG_MONTH_YEAR), "timely.unitylab@gmail.com", receiver);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Email: " + "There is no email client installed.");
                }
            }

        }).start();
        Toast.makeText(getActivity(), "The Email has been sent ", Toast.LENGTH_SHORT).show();
        getDialog().dismiss();
    }


    @OnClick({R.id.emailEditText,R.id.emailTextLabel})
    public void clickUnEditableLabelsImages(View v) {
        int mSelectedText = v.getId();
        if (mSelectedText == R.id.emailEditText) {
            tracker.interactionTrack(getActivity().findViewById(R.id.emailEditText), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if(mSelectedText == R.id.emailTextLabel)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.emailTextLabel), tracker.getInteractionClicID(),"","",false,false,"");
        }
    }
}
