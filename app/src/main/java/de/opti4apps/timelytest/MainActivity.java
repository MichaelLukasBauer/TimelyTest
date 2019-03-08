package de.opti4apps.timelytest;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.opti4apps.timelytest.data.Day;
import de.opti4apps.timelytest.data.User;
import de.opti4apps.timelytest.data.UserManager;
import de.opti4apps.timelytest.data.WorkProfile;
import de.opti4apps.timelytest.event.DaySelectedEvent;
import de.opti4apps.timelytest.shared.TimelyHelper;
import de.opti4apps.timelytest.shared.TrackerHelper;
import de.opti4apps.tracker.appInfo.AppInfoTracker;
import de.opti4apps.tracker.deviceInfo.DeviceInfoTracker;
import de.opti4apps.tracker.gesture.GestureTracker;
import de.opti4apps.tracker.interaction.InteractionTracker;
import de.opti4apps.tracker.screen.ScreenTracker;
import de.opti4apps.trackerclient.CommonConfig;
import de.opti4apps.trackerclient.TrackingService;
import io.objectbox.Box;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION = 0;
    private Intent intent ;
    DayListFragment mDayListFragment;
    DayFragment mDayFragment;
    private TrackerHelper tracker;
    WorkProfileFragment mWorkProfileFragment;

    User currentUser;
    Box<User> usersBox;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    TextView mUserNameTextView;
    TextView mUserEmailTextView;
    ImageView mUserIcon;
    private Box<WorkProfile> mWorkProfileBox;
    private Box<Day> mDayBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDiaryMessageForEvaluation();
        String languageToLoad  = "english"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.activity_main);


        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        checkAndRequestPermissions();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        mDayListFragment = (DayListFragment) getSupportFragmentManager().findFragmentByTag(DayListFragment.TAG);
        mDayFragment = (DayFragment) getSupportFragmentManager().findFragmentByTag(DayFragment.TAG);

        Intent intent = getIntent();
        String currentUserEmail = intent.getStringExtra("userEmail");
        usersBox = ((App) getApplication()).getBoxStore().boxFor(User.class);

        currentUser = UserManager.getUserByEmail(usersBox, currentUserEmail);
        tracker = new TrackerHelper(TAG,this,currentUser.getId());
        if (mDayListFragment == null) {
            mDayListFragment = DayListFragment.newInstance(currentUser.getId());
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, mDayListFragment, DayListFragment.TAG);
//            transaction.addToBackStack(null);
            transaction.commit();
        }




        //TotalExtraHoursBox = ((App) getApplication()).getBoxStore().boxFor(TotalExtraHours.class);


        mUserNameTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tv_user_name);
        mUserNameTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tracker.interactionTrack(mNavigationView.getHeaderView(0).findViewById(R.id.tv_user_name), tracker.getInteractionClicID(),"","","");
            }
        });
        mUserNameTextView.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

        mUserEmailTextView = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tv_user_email);
        mUserEmailTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tracker.interactionTrack(mNavigationView.getHeaderView(0).findViewById(R.id.tv_user_email), tracker.getInteractionClicID(),"","","");
            }
        });
        mUserEmailTextView.setText(currentUser.getEmail());

        mUserIcon = (ImageView)  mNavigationView.getHeaderView(0).findViewById(R.id.tv_user_icon);
        mUserIcon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tracker.interactionTrack(mNavigationView.getHeaderView(0).findViewById(R.id.tv_user_icon), tracker.getInteractionClicID(),"","","");
            }
        });
        mWorkProfileBox = ((App) getApplication()).getBoxStore().boxFor(WorkProfile.class);
        mDayBox = ((App) getApplication()).getBoxStore().boxFor(Day.class);
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        tracker.onStartTrack("","","");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing()) {
            //showDiaryMessageForEvaluation();
            // we will not need this fragment anymore, this may also be a good place to signal
            // to the retained fragment object to perform its own cleanup.
            //getSupportFragmentManager().beginTransaction().remove(mDayListFragment).commit();
        }
    }

//    @Override
//    public void onResume(){
//        super.onResume();
//        this.startService(intent);
//    }
//
//    @Override
//    public void onRestart(){
//        super.onRestart();
//        this.startService(intent);
//    }

    @Override
    public void onBackPressed() {
        //showDiaryMessageForEvaluation();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                return false;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_capture_time) {
            tracker.interactionTrack(item, tracker.getInteractionClicID(),TrackerHelper.CREATE_DAY,"","");
            if (TimelyHelper.getWorkProfileByMonth(DateTime.now(),mWorkProfileBox,currentUser.getId()) != null) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, DayFragment.newInstance(0, currentUser.getId()), DayFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                showWPCreateMessage();
            }
        } else if (id == R.id.nav_month_overview) {
            tracker.interactionTrack(item , tracker.getInteractionClicID(),TrackerHelper.MONTH_OVERVIEW,"","");
            if (!mDayListFragment.isAdded()) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, mDayListFragment, DayListFragment.TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        } else if (id == R.id.nav_work_profile) {
            tracker.interactionTrack(item, tracker.getInteractionClicID(),TrackerHelper.WORk_PROFILE,"","");
            //we need to get the current user ID and use it to create the working profile instance
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, WorkProfileFragment.newInstance(currentUser.getId()), WorkProfileFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_signout) {
            tracker.interactionTrack(item, tracker.getInteractionClicID(),"","","");
            UserManager.changeUserSignedInStatus(currentUser, usersBox);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finishAffinity();
            MainActivity.this.startActivity(intent);
            finish();
            this.startService(intent);
        } else if (id == R.id.nav_time_sheet) {
            tracker.interactionTrack(item, tracker.getInteractionClicID(),TrackerHelper.SEND_GENERATE_REPORT,"","");
            if ( mDayBox.count() > 0) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, PdfGenerationFragment.newInstance(currentUser.getId()), PdfGenerationFragment.TAG);
                //transaction.addToBackStack(null); TimelyHelper.getTotalReportedDayForMonth(DateTime.now(),mDayBox,currentUser.getId())
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                showDayreateMessage();
            }
        }
        else if (id == R.id.nav_help) {
            tracker.interactionTrack(item, tracker.getInteractionClicID(),TrackerHelper.HELP,"","");
            //we need to get the current user ID and use it to create the working profile instance
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, HelpFragment.newInstance(currentUser.getId()), HelpFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Subscribe
    public void onDaySelected(DaySelectedEvent event) {
        Log.d(TAG, "onDaySelected: received DaySelectedEvent with id of day = " + event.dayID);
        mDayListFragment = (DayListFragment) getSupportFragmentManager().findFragmentByTag(DayListFragment.TAG);
        if (mDayFragment == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, DayFragment.newInstance(event.dayID, currentUser.getId()), DayFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (mDayFragment.getDay().getId() != event.dayID && event.dayID > 0) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, DayFragment.newInstance(event.dayID, currentUser.getId()), DayFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (event.dayID <= 0) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, DayFragment.newInstance(0, currentUser.getId()), DayFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        tracker.onStopTrack("","","");
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(intent != null){
            this.stopService(intent);
        }

    }
    private void showWPCreateMessage() {
        String message = getResources().getString(R.string.no_working_profile);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showDayreateMessage() {
        String message = getResources().getString(R.string.no_days);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void checkAndRequestPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE)) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)) == PackageManager.PERMISSION_GRANTED
                //&& (ContextCompat.checkSelfPermission(this, Manifest.permission.BATTERY_STATS)) == PackageManager.PERMISSION_GRANTED
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)) == PackageManager.PERMISSION_GRANTED
                ) {
            startTracking();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_NETWORK_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    // Manifest.permission.BATTERY_STATS,
                    Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> usedTrackers = new ArrayList<>();
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if ((grantResults.length > 0)) {
                    boolean granted = false;
                    for (int i = 0; i < grantResults.length; i++) {
                        granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                        if (!granted) return;
                    }
                    if (granted) startTracking()
                         ;

                }
            }
        }
    }

    private void startTracking() {
        intent = new Intent(this, TrackingService.class);
        intent.putExtra(CommonConfig.SENSOR_LIST, new String[]{
//                WifiTracker.name,
//                LightTracker.name,
//                SignalStrengthTracker.name,
                ScreenTracker.name,
                GestureTracker.name,
//                PressureTracker.name,
//                ProximityTracker.name,
//                StepCounterTracker.name,
//                UncaughtExceptionTracker.name,
//                InteractionWithLogTracker.name,
                InteractionTracker.name,
                DeviceInfoTracker.name,
//                DeviceOrientationTracker.name,
                AppInfoTracker.name,
//                BatteryTracker.name,
//                BluetoothTracker.name,
//                NetworkInfoTracker.name,
//                LocationTracker.name,
//                CellInfoTracker.name,
//                GyroscopeTracker.name,
//                MagneticFieldTracker.name,
//                AccelerometerTracker.name,

        });
        //intent.putExtra(CommonConfig.UPLOAD_URL, "https://hookbin.com/bin/EzgA3lDW/");
        intent.putExtra(CommonConfig.UPLOAD_URL, "http://opti4apps-timely.iap.hs-heilbronn.de/");
        //intent.putExtra(CommonConfig.UPLOAD_URL, "http://172.20.10.6:8383/");
        //intent.putExtra(CommonConfig.UPLOAD_URL, "http://10.70.28.234:8383/");
        //intent.putExtra(CommonConfig.UPLOAD_URL, "http://10.0.2.2:8080/events/process/");
        intent.putExtra(CommonConfig.STORAGE_MODE, CommonConfig.STORAGE_MODE_DATABASE);
        intent.putExtra(CommonConfig.UPLOAD_MODE, CommonConfig.UPLOAD_MODE_PERIODICALLY);
        intent.putExtra(CommonConfig.UPLOAD_COLLECTION, CommonConfig.UPLOAD_COLLECTION_DEFAULT);
        intent.putExtra(CommonConfig.UPLOAD_INTERVAL, 10l);
        this.startService(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        GestureTracker.trackGesture(this,event,(ViewGroup)findViewById(android.R.id.content));
        return true;
    }

    public void showDiaryMessageForEvaluation(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Please remember to update your diary after using TIMELY!")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show ();
    }
    }
