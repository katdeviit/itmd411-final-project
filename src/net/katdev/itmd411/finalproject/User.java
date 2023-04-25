package net.katdev.itmd411.finalproject;

import java.awt.*;

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

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return admin;
    }

    public Color getUserColor() {
        return userColor;
    }
}
