package de.opti4apps.timelytest;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.itextpdf.text.DocumentException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.PDFGenerator;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.event.DatePickedEvent;
import de.opti4apps.timelytest.shared.DurationPickerFragment;
import de.opti4apps.timelytest.shared.MonthYearPickerFragment;
import de.opti4apps.timelytest.shared.TimelyHelper;
import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.gesture.GestureTracker;
import io.objectbox.Box;


/**
 * Created by Kateryna Sergieieva on 02.08.2017.
 */

public class PdfGenerationFragment extends Fragment {

    @BindView(R.id.generatePdfMonthText)
    TextView mGeneratePdfText;

    @BindView(R.id.total_reported_days_text)
    TextView mTotalReportedDayText;

    @BindView(R.id.total_working_days_text)
    TextView mTotalWorkingDayText;

    @BindView(R.id.total_days_business_trip_text)
    TextView mTotalBusinessTripDayText;

    @BindView(R.id.total_days_illness_text)
    TextView mTotalIllnessDayText;

    @BindView(R.id.total_days_further_education_text)
    TextView mTotalFurtherEducationDayText;

    @BindView(R.id.total_days_off_in_lieu_text)
    TextView mTotalDayOffInLieuText;

    @BindView(R.id.total_days_others_text)
    TextView mTotalOtherDayText;

    @BindView(R.id.Total_days_on_vacation_text)
    TextView mTotalVacationDayText;

    @BindView(R.id.Total_doc_appointments_text)
    TextView mTotalDocAppointmentDayText;

    @BindView(R.id.total_overtime_text)
    TextView mTotalOvertimeText;

    private Box<Day> mDayBox;
    private Box<WorkProfile> mWorkProfileBox;

    private static final String ARG_USER_ID = "userID";
    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String FIRST_DAY_OF_MONTH = "1";
    public static final String TAG = PdfGenerationFragment.class.getSimpleName();
    long userID ;
    private Calendar reportSelectedDate = null;
    private TrackerHelper tracker;
//    @BindView(R.id.reportList)
//    ListView list;

    private File dir;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private ArrayList<String> filenames;
    private ArrayList<File> filesList;


    public static PdfGenerationFragment newInstance(long userID) {
        PdfGenerationFragment fragment = new PdfGenerationFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userID);
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
//        if (getArguments() != null) {
//            long userID = getArguments().getLong(ARG_USER_ID);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tracker = new TrackerHelper(TAG,getContext());
        mDayBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(Day.class);
        mWorkProfileBox = ((App) getActivity().getApplication()).getBoxStore().boxFor(WorkProfile.class);
        View view = inflater.inflate(R.layout.fragment_pdf_creation, container, false);
        ButterKnife.bind(this, view);
        userID = getArguments().getLong(ARG_USER_ID);

        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                GestureTracker.trackGesture(getContext(),event,(ViewGroup) v);
                return true;
            }
        });

        reportSelectedDate = Calendar.getInstance();
        String currentMonthYearStr = reportSelectedDate.get(Calendar.YEAR) +"."+ (reportSelectedDate.get(Calendar.MONTH)+1) + "." + FIRST_DAY_OF_MONTH;
        try {
            Date currentMonthYear = new SimpleDateFormat("yyyy.MM.dd").parse(currentMonthYearStr);
            reportSelectedDate.setTime(currentMonthYear);
            updateSummary();
        } catch (ParseException e) {
            Log.w("Wrong Date", "Report date is wrong!");
        }

        String currentMonthYear = new SimpleDateFormat("MMM yyyy").format(reportSelectedDate.getTime());
        mGeneratePdfText.setText(currentMonthYear);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        tracker.onStartTrack("","",false,false,"");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        tracker.onStopTrack("","",false,false,"");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdf();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadListView();
    }

    @OnClick({R.id.generatePdfMonthText})
    public void selectReportDate(View v) {
        tracker.interactionTrack(getActivity().findViewById(R.id.generatePdfMonthText), tracker.getInteractionClicID(),TrackerHelper.SEND_GENERATE_REPORT,"",true,false,"");
        String currentDateStr = mGeneratePdfText.getText().toString();
//        try {
//            Date selectedMonthYear = new SimpleDateFormat("MMM yyyy").parse(currentDateStr);
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(selectedMonthYear);
            int year = reportSelectedDate.get(Calendar.YEAR);
            int month = reportSelectedDate.get(Calendar.MONTH) + 1;
            Bundle dateArgs = new Bundle();
            dateArgs.putInt(ARG_YEAR, year);
            dateArgs.putInt(ARG_MONTH, month);

            DialogFragment pd = new MonthYearPickerFragment();
            pd.setArguments(dateArgs);
            pd.show(getFragmentManager(), "monthYearPicker");

 //       } catch (ParseException e) {
        //         Log.w("Wrong Date", "Report date is wrong!");
     //   }
    }

    @Subscribe
    public void onDatePicked(DatePickedEvent event) {

            String selectedMonthYearStr = event.year + "." + event.month + "." + FIRST_DAY_OF_MONTH;
        try {
            Date selectedMonthYear = new SimpleDateFormat("yyyy.MM.dd").parse(selectedMonthYearStr);
            mGeneratePdfText.setText(new SimpleDateFormat("MMM yyyy").format(selectedMonthYear));
            reportSelectedDate.setTime(selectedMonthYear);
            updateSummary();
        }
        catch (ParseException ex)
        {
           Log.w("Wrong Date", "Report date is wrong!");
        }
    }

    @OnClick({R.id.generatePdfButton})
    public void generatePdfReport(View v) {
        tracker.interactionTrack(getActivity().findViewById(R.id.generatePdfButton), tracker.getInteractionClicID(),"",TrackerHelper.GENERATE_REPORT,false,true,"");
        if (isExternalStorageWritable()) {
            if (checkPermissions()) {
                generatePdf();
                SimpleDateFormat sdf = new SimpleDateFormat("MMMyyyy");
                String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/UniTyLab/PDF Files";
                String filename="UniTyLabEmployeesTimesheet_" + sdf.format(reportSelectedDate.getTime()) + ".pdf";
                openPDFFileWithIntent(storagePath,filename);
            }
        }
    }

    public void openPDFFileWithIntent(String fileLocation, String filename)
    {
        File filelocation = new File(fileLocation, filename);;
        Uri path = Uri.fromFile(filelocation);
        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenintent.setDataAndType(path, "application/pdf");
        try {
            startActivity(pdfOpenintent);
        } catch (ActivityNotFoundException e) {

        }
    }


    @OnClick({R.id.SendPerMailButton})
    public void SendReportPerMail(View v) {
        tracker.interactionTrack(getActivity().findViewById(R.id.SendPerMailButton), tracker.getInteractionClicID(),"","",false,false,"");
        if (isExternalStorageWritable()) {
            if (checkPermissions()) {
                generatePdf();
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMMyyyy");
        Long userID = getArguments().getLong(ARG_USER_ID);
        DialogFragment newFragment = SendEmailFragment.newInstance(userID,sdf.format(reportSelectedDate.getTime()));
        newFragment.show(getFragmentManager(), "startEmailDialogFragment");
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.compareToIgnoreCase(state) == 0) {
            return true;
        }
        return false;
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getActivity(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            this.requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    private void generatePdf() {
        try {
            String storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/UniTyLab/PDF Files";
            dir = new File(storagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            new PDFGenerator(getActivity()).createPDF(storagePath, reportSelectedDate);
            //loadListView();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }


    public void GetFiles(String DirectoryPath) {
        filenames = new ArrayList<>();
        filesList = new ArrayList<>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            filenames.add(files[i].getName());
            filesList.add(files[i]);
        }
    }

    public void updateSummary()
    {
        if (reportSelectedDate != null)
        {
            Day mDay = new Day(userID,Day.DAY_TYPE.WORKDAY, DateTime.now(), DateTime.now(), DateTime.now(), Duration.standardMinutes(0));
            int year = reportSelectedDate.get(Calendar.YEAR);
            int month = reportSelectedDate.get(Calendar.MONTH) + 1;
            DateTime currentMonth = new DateTime(year,month,1,0,0);
            mTotalReportedDayText.setText(String.valueOf(TimelyHelper.getTotalReportedDayForMonth(currentMonth,mDayBox,userID)));
            mTotalBusinessTripDayText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.BUSINESS_TRIP,currentMonth,mDayBox,userID)));
            mTotalDayOffInLieuText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.DAY_OFF_IN_LIEU,currentMonth,mDayBox,userID)));
            mTotalDocAppointmentDayText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.DOCTOR_APPOINTMENT,currentMonth,mDayBox,userID)));
            mTotalFurtherEducationDayText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.FURTHER_EDUCATION,currentMonth,mDayBox,userID)));
            mTotalIllnessDayText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.ILLNESS,currentMonth,mDayBox,userID)));
            mTotalOtherDayText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.OTHER,currentMonth,mDayBox,userID)));
            mTotalVacationDayText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.HOLIDAY,currentMonth,mDayBox,userID)));
            mTotalWorkingDayText.setText(String.valueOf(TimelyHelper.getTotalDayForDayType(Day.DAY_TYPE.WORKDAY,currentMonth,mDayBox,userID)));
            String totalOvertime = TimelyHelper.negativeTimePeriodFormatter(TimelyHelper.getMonthTotalOvertime(TimelyHelper.getWorkProfileByMonth(currentMonth,mWorkProfileBox,userID), mDayBox,userID).toPeriod(), Day.PERIOD_FORMATTER);
            mTotalOvertimeText.setText(totalOvertime);
        }
    }
        @OnClick({R.id.dateImageView, R.id.total_reported_days_label,R.id.total_reported_days_text, R.id.total_working_days_label,R.id.total_working_days_text,
                R.id.Total_days_on_vacation_label,R.id.Total_days_on_vacation_text,R.id.Total_doc_appointments_label,R.id.Total_doc_appointments_text,
                R.id.total_days_illness_label,R.id.total_days_illness_text,R.id.total_days_off_in_lieu_label,R.id.total_days_off_in_lieu_text,
                R.id.total_days_further_education_label,R.id.total_days_further_education_text,R.id.total_days_business_trip_label,
                R.id.total_days_business_trip_text,R.id.total_days_others_label,R.id.total_days_others_text,
                R.id.total_overtime_label,R.id.total_overtime_text})
        public void clickUnEditableLabelsImages(View v) {
        int mSelectedText = v.getId();
        if (mSelectedText == R.id.dateImageView)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.dateImageView), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if (mSelectedText == R.id.total_reported_days_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_reported_days_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if (mSelectedText == R.id.total_reported_days_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_reported_days_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if (mSelectedText == R.id.total_working_days_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_working_days_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if (mSelectedText == R.id.total_working_days_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_working_days_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if (mSelectedText == R.id.Total_days_on_vacation_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.Total_days_on_vacation_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.Total_days_on_vacation_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.Total_days_on_vacation_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.Total_doc_appointments_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.Total_doc_appointments_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.Total_doc_appointments_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.Total_doc_appointments_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_illness_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_illness_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_illness_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_illness_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_off_in_lieu_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_off_in_lieu_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_off_in_lieu_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_off_in_lieu_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_further_education_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_further_education_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_further_education_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_further_education_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_business_trip_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_business_trip_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_business_trip_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_business_trip_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_others_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_others_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_days_others_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_days_others_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_overtime_label)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_overtime_label), tracker.getInteractionClicID(),"","",false,false,"");
        }
        else if ( mSelectedText == R.id.total_overtime_text)
        {
            tracker.interactionTrack(getActivity().findViewById(R.id.total_overtime_text), tracker.getInteractionClicID(),"","",false,false,"");
        }
    }

}
