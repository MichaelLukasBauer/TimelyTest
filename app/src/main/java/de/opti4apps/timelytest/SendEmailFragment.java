package de.opti4apps.timelytest;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import io.objectbox.Box;
import io.objectbox.query.Query;

/**
 * Created by TCHATCHO on 20.09.2017.
 */

public class SendEmailFragment extends DialogFragment {
    public static final String TAG = SendEmailFragment.class.getSimpleName();
    private static final String ARG_USER_ID = "userID";
    private static final String ARG_MONTH_YEAR = "monthYear";

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

        int style = DialogFragment.STYLE_NORMAL, theme = android.R.style.Theme_Holo_Light_Dialog;
        setStyle(style, theme);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragent_mail_send, container, false);

        ButterKnife.bind(this, view);
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
    }

    @Override
    public void onStop() {
        super.onStop();
        //EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.sendReportButton})
    public void SendReport(View v) {
        sendEmail(mEmail.getText().toString());

    }

    protected void sendEmail(String receiver) {
        String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/UniTyLab/PDF Files";
        String filename="UniTyLabEmployeesTimesheet_" + getArguments().getString(ARG_MONTH_YEAR) + ".pdf";
        File filelocation = new File(storagePath, filename);
        Uri path = Uri.fromFile(filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");
        String to[] = {receiver};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Employee Timesheet " +  getArguments().getString(ARG_MONTH_YEAR));

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
