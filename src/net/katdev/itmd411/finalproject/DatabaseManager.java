package net.katdev.itmd411.finalproject;

import java.awt.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseManager {

    public static Connection connection;
    // Code database URL
    static final String DB_URL = "REDACTED";
    // Database credentials
    static final String USER = "REDACTED", PASS = "REDACTED";

    public static void connect() throws SQLException {
        connection = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * For creation of database schema only. Run once.
     */
    public static void main(String[] args) {
        create_db_schema();
        create_users();
    }

    public static void create_db_schema() {
        System.out.println("Creating DB schema (this is a one time action)...");
        try {
            connect();
            String sql = "CREATE TABLE `kstev_users` (id INTEGER NOT NULL AUTO_INCREMENT,username VARCHAR(32) NOT NULL,password VARCHAR(32) NOT NULL,admin TINYINT NOT NULL,color INTEGER NOT NULL,UNIQUE KEY unique_username (username),PRIMARY KEY (id))";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        System.out.println("Created users table.");
        try {
            connect();
            String sql = "CREATE TABLE `kstev_tickets` (id INTEGER NOT NULL AUTO_INCREMENT,`subject` VARCHAR(128) NOT NULL,`author` INTEGER NOT NULL,`createdAt` TIMESTAMP NOT NULL,`state` INTEGER NOT NULL,FOREIGN KEY (author) REFERENCES kstev_users(id) ON DELETE CASCADE,PRIMARY KEY (id))";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        System.out.println("Created tickets table.");
        try {
            connect();
            String sql = "CREATE TABLE `kstev_ticket_messages` (id INTEGER NOT NULL AUTO_INCREMENT,`ticket_id` INTEGER NOT NULL,`sentAt` TIMESTAMP NOT NULL,`author` INTEGER NOT NULL,`content` VARCHAR(10000) NOT NULL,FOREIGN KEY (author) REFERENCES kstev_users(id) ON DELETE CASCADE,FOREIGN KEY (ticket_id) REFERENCES kstev_tickets(id) ON DELETE CASCADE,PRIMARY KEY (id))";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        System.out.println("Created ticket messages table.");
    }

    public static void create_users() {
        System.out.println("Creating users (this is a one time action)...");
        try {
            connect();
            Statement stmt = connection.createStatement();
            stmt.addBatch("INSERT INTO `kstev_users` (username,password,admin,color) VALUES ('kat','test123',0," + Color.PINK.getRGB() + ")");
            stmt.addBatch("INSERT INTO `kstev_users` (username,password,admin,color) VALUES ('admin','adminSecure!',1," + Color.RED.getRGB() + ")");
            stmt.addBatch("INSERT INTO `kstev_users` (username,password,admin,color) VALUES ('johnsmith','test123!@',0," + Color.GREEN.getRGB() + ")");
            stmt.addBatch("INSERT INTO `kstev_users` (username,password,admin,color) VALUES ('thedoctor','t@rdis',0," + Color.BLUE.getRGB() + ")");
            stmt.addBatch("INSERT INTO `kstev_users` (username,password,admin,color) VALUES ('jpapa','bestpr0f!',0," + Color.ORANGE.getRGB() + ")");
            stmt.addBatch("INSERT INTO `kstev_users` (username,password,admin,color) VALUES ('mrtest','testman',0," + Color.CYAN.getRGB() + ")");
            stmt.executeBatch();
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        System.out.println("Created users.");
    }

    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        try {
            connect();
            String sql = "SELECT `id`,`username`,`admin`,`color` FROM `kstev_users`";
            Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(sql);
            while(res.next()) {
                int id = res.getInt(1);
                String newUsername = res.getString(2);
                boolean isAdmin = res.getBoolean(3);
                Color newColor = new Color(res.getInt(4));
                User user = new User(id, newUsername, isAdmin, newColor);
                users.add(user);
            }
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return users;
    }

    /**
     * Returns the user ID of the logged in user, if the credentials presented match any user.
     * @param username Username to check
     * @param password Password to check
     * @return A user ID, or -1 if invalid
     */
    public static int verifyUserCredentials(String username, String password) {
        int id = -1;
        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement("SELECT `id`,`username`,`password` FROM `kstev_users` WHERE username=? AND password=?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet res = stmt.executeQuery();
            if(res.next()) {
                id = res.getInt(1);
            }
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return id;
    }

    public static List<Ticket> getTickets() {
        List<Ticket> tickets = new ArrayList<>();
        try {
            connect();
            HashMap<Integer, List<Ticket.TicketMessage>> ticketMessages = new HashMap<>();
            String sql = "SELECT `id`,`subject`,`author`,`createdAt`,`state` FROM `kstev_tickets`";
            String ticketMessagesSql = "SELECT `ticket_id`,`sentAt`,`author`,`content` FROM `kstev_ticket_messages`";
            Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(sql);
            Statement stmt2 = connection.createStatement();
            ResultSet mes_res = stmt2.executeQuery(ticketMessagesSql);
            while(mes_res.next()) {
                int ticketId = mes_res.getInt(1);
                Timestamp sentAt = mes_res.getTimestamp(2);
                int userId = res.getInt(3);
                String content = res.getString(4);
                Ticket.TicketMessage message = new Ticket.TicketMessage(sentAt, LocalCache.getUser(userId), content);
                List<Ticket.TicketMessage> ticketMessageList = ticketMessages.getOrDefault(ticketId, new ArrayList<>());
                ticketMessageList.add(message);
                ticketMessages.put(ticketId, ticketMessageList);
            }
            while(res.next()) {
                int ticketNum = res.getInt(1);
                String subject = res.getString(2);
                int userId = res.getInt(3);
                Timestamp createdAt = res.getTimestamp(4);
                Ticket.TicketState state = Ticket.TicketState.values()[res.getInt(5)];
                List<Ticket.TicketMessage> messages = ticketMessages.getOrDefault(ticketNum, new ArrayList<>());
                Ticket ticket = new Ticket(ticketNum, subject, LocalCache.getUser(userId), createdAt, state, messages);
                tickets.add(ticket);
            }
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return tickets;
    }

    public static boolean createTicket(User author, String subject) {
        boolean success = false;
        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO `kstev_tickets` (subject,author,createdAt,state) VALUES (?,(SELECT id FROM `kstev_users` WHERE username=?),NOW(),?)");
            stmt.setString(1, subject);
            stmt.setString(2, author.getUsername());
            stmt.setInt(3, Ticket.TicketState.OPEN.ordinal());
            stmt.executeUpdate();
            success = true;
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return success;
    }
}
