package de.opti4apps.timelytest;

import android.app.Application;

import de.opti4apps.timelytest.data.MyObjectBox;
import io.objectbox.BoxStore;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    private BoxStore mBoxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        mBoxStore = MyObjectBox.builder().androidContext(App.this).build();
    }


    public BoxStore getBoxStore() {
        return mBoxStore;
    }
}
