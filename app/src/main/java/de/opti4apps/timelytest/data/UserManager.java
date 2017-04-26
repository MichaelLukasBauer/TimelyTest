package de.opti4apps.timelytest.data;

import io.objectbox.Box;

/**
 * Created by Kateryna Sergieieva on 25.04.2017.
 */

public class UserManager {

    public boolean checkIsUserSignedIn(User user, Box<User> usersBox){
        user = usersBox.query().equal(User_.isSingedIn, true).build().findFirst();
        if(user == null){
            return false;
        }
        else{
            return true;
        }
    }

    public  void changeUserSignedInStatus(User user, Box<User> usersBox){
        boolean isCurrentUserSignedIn = user.getIsSingedIn();
        user.setIsSingedIn(!isCurrentUserSignedIn);
        usersBox.put(user);
    }

    public boolean checkUserCredentials(User user, Box<User> usersBox, String email, String password){

        user = usersBox.query().equal(User_.email, email).equal(User_.password, password).build().findFirst();
        if(user == null){
            return false;
        }
        else{
            return true;
        }
    }


}
