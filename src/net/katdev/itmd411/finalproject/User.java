package net.katdev.itmd411.finalproject;

import java.awt.*;

public class User {

    public int id;
    public String username;
    public boolean admin;
    public Color userColor = Color.BLACK;

    public User(int id, String username, boolean admin) {
        this.id = id;
        this.username = username;
        this.admin = admin;
    }

    public User(int id, String username, boolean admin, Color color) {
        this(id, username, admin);
        this.userColor = color;
    }

}
