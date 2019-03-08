package de.opti4apps.timelytest;

import android.app.Application;

import java.util.List;

import de.opti4apps.timelytest.data.MyObjectBox;
import de.opti4apps.timelytest.data.User;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();


    Box<User> usersBox;
    private BoxStore mBoxStore;

    @Override
    public void onCreate() {
        super.onCreate();
        mBoxStore = MyObjectBox.builder().androidContext(App.this).build();

        usersBox = getBoxStore().boxFor(User.class);
        addDefaultUsers();


    }


    public BoxStore getBoxStore() {
        return mBoxStore;
    }

    private void addDefaultUsers() {
        List<User> users = usersBox.query().build().find();

        if (users == null || users.size() == 0) {

            User user = new User(0, "kaesyn.trindon@cowstore.net", "mwgcf2Q?", false, "User", "1");
            usersBox.put(user);

            user = new User(0, "jaidan.alistar@cowstore.net", "xuckc6X?", false, "User", "2");
            usersBox.put(user);

            user = new User(0, "travontae.caid@cowstore.net", " yjjhg7D$", false, "User", "3");
            usersBox.put(user);

            user = new User(0, "kateryna.sergieieva@hs-heilbronn.de", "test123", false, "Kateryna", "Sergieieva");
            usersBox.put(user);

            user = new User(0, "opti4apps.test@heilbronn.de", "test123", false, "Test", "User");
            usersBox.put(user);
        }
    }


}
