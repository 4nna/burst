package de.hacktival.burst;

/**
 * Singleton for user state
 */
public class UserState {

    // user Token
    public String userToken;

    // whether the user is matchable
    public boolean matchable;




    private static UserState instance;

    public static UserState getInstance () {
        if (UserState.instance == null) {
            UserState.instance = new UserState();
        }
        return UserState.instance;
    }

}
