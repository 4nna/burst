package de.hacktival.burst;

/**
 * Singleton for user state
 */
public class UserState {

    // user Token
    private String userToken;

    // whether the user is matchable
    private boolean matchable;

    private static UserState instance;

    public static synchronized UserState getInstance () {
        if (UserState.instance == null) {
            UserState.instance = new UserState();
        }
        return UserState.instance;
    }

    public synchronized String getUserToken() {
        return userToken;
    }

    public synchronized void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public synchronized boolean isMatchable() {
        return matchable;
    }

    public synchronized void setMatchable(boolean matchable) {
        this.matchable = matchable;
    }
}
