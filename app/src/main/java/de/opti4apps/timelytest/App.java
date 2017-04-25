package de.opti4apps.timelytest;

import android.app.Application;

import java.util.List;

import de.opti4apps.timelytest.data.MyObjectBox;
import de.opti4apps.timelytest.data.User;
import de.opti4apps.timelytest.data.User_;
import io.objectbox.Box;
import io.objectbox.BoxStore;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    private BoxStore mBoxStore;
    Box<User> usersBox;

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

    private void addDefaultUsers(){
        List<User> users = usersBox.query().build().find();

        if(users == null || users.size() == 0) {
            User user = new User(0, "kateryna.sergieieva@hs-heilbronn.de", "test123", false, "Kateryna", "Sergieieva");
            usersBox.put(user);

            user = new User(0, "test@123.com", "test123", false, "John", "Smith");
            usersBox.put(user);

            user = new User(0, "michael.bauer@hs-heilbronn.de", "test123", false, "Michael", "Bauer");
            usersBox.put(user);
        }
    }
}
