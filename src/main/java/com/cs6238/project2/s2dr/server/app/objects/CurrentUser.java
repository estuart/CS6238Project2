package com.cs6238.project2.s2dr.server.app.objects;

// TODO #35 this could probably be handled better
public class CurrentUser {

    private User currentUser;

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public String getUserName() {
        return currentUser.getUserName();
    }
}
