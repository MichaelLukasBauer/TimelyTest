package de.opti4apps.timelytest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.opti4apps.timelytest.event.DaySelectedEvent;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    DayListFragment mDayListFragment;
    DayFragment mDayFragment;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);



        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        mDayListFragment = (DayListFragment) getSupportFragmentManager().findFragmentByTag(DayListFragment.TAG);
        mDayFragment = (DayFragment) getSupportFragmentManager().findFragmentByTag(DayFragment.TAG);

        if (mDayListFragment == null) {
            mDayListFragment = DayListFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, mDayListFragment, DayListFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFinishing()) {
            // we will not need this fragment anymore, this may also be a good place to signal
            // to the retained fragment object to perform its own cleanup.
            getSupportFragmentManager().beginTransaction().remove(mDayListFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
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


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, DayFragment.newInstance(event.dayID), DayFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (mDayFragment.getDay().getId() != event.dayID && event.dayID > 0) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, DayFragment.newInstance(event.dayID), DayFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (event.dayID <= 0) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, DayFragment.newInstance(0), DayFragment.TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
