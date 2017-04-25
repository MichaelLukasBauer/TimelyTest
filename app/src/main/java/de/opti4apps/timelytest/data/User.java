package de.opti4apps.timelytest.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Generated;
import io.objectbox.annotation.Id;

/**
 * Created by Kateryna Sergieieva on 25.04.2017.
 */
@Entity
public class User {

    @Id
    private long id;

    private String email;
    private String password;
    private boolean isSingedIn;
    private String firstName;
    private String lastName;

    @Generated(hash = 1552010810)
    public User(long id, String email, String password, boolean isSingedIn, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.isSingedIn = isSingedIn;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSingedIn() {
        return isSingedIn;
    }

    public void setSingedIn(boolean singedIn) {
        isSingedIn = singedIn;
    }

    public boolean getIsSingedIn() {
        return isSingedIn;
    }

    public void setIsSingedIn(boolean isSingedIn) {
        this.isSingedIn = isSingedIn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
