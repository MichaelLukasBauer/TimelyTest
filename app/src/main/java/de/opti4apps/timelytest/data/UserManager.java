package de.opti4apps.timelytest.data;

import io.objectbox.Box;

/**
 * Created by Kateryna Sergieieva on 25.04.2017.
 */

public class UserManager {

    static public boolean checkIsUserSignedIn(Box<User> usersBox) {
        User user = usersBox.query().equal(User_.isSingedIn, true).build().findFirst();
        return user != null;
    }

    static public void changeUserSignedInStatus(User user, Box<User> usersBox) {
        boolean isCurrentUserSignedIn = user.getIsSingedIn();
        user.setIsSingedIn(!isCurrentUserSignedIn);
        usersBox.put(user);
    }

    static public boolean checkUserCredentials(Box<User> usersBox, String email, String password) {

        User user = usersBox.query().equal(User_.email, email).equal(User_.password, password).build().findFirst();
        return user != null;
    }

    static public  User getUserByEmail(Box<User> usersBox, String email) {
        User user = usersBox.query().equal(User_.email, email).build().findFirst();
        return user;
    }

    static public  User getSignedInUser(Box<User> usersBox) {
        User user = usersBox.query().equal(User_.isSingedIn, true).build().findFirst();
        return user;
    }


}
