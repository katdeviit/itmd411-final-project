package net.katdev.itmd411.finalproject;

import java.awt.*;

/**
 * An internal data storage object for users of the app.
 */
public class User {

    private int id;
    private String username;
    private boolean admin;
    private Color userColor = Color.BLACK;

    public User(int id, String username, boolean admin) {
        this.id = id;
        this.username = username;
        this.admin = admin;
    }

    public User(int id, String username, boolean admin, Color color) {
        this(id, username, admin);
        this.userColor = color;
    }

    /**
     * @return The internal user ID, can be used to verify this user's identity
     */
    public int getId() {
        return id;
    }

    /**
     * @return The username, can be used to verify this user's identity as it is globally unique
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return Whether this user is an admin or not
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * @return The color used to display this user's username
     */
    public Color getUserColor() {
        return userColor;
    }
}
