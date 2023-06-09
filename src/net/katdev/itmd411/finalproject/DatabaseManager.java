package net.katdev.itmd411.finalproject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {

    public static Connection connection;
    // Code database URL
    private static String DB_URL = null;
    // Database credentials
    private static String USER = null, PASS = null;

    public static void load_credentials() {
        try (InputStream input = new FileInputStream("db_creds.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            DB_URL = prop.getProperty("db_url");
            USER = prop.getProperty("user");
            PASS = prop.getProperty("pass");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if(DB_URL == null || USER == null || PASS == null) {
            System.out.println("Failed to read database credentials! Add a db_creds.properties file with the following entries:");
            System.out.println("db_url=...");
            System.out.println("user=...");
            System.out.println("pass=...");
            JOptionPane.showConfirmDialog(new JFrame(), "Failed to read database credentials! Add a db_creds.properties file!");
        }
    }

    public static void connect() throws SQLException {
        if(DB_URL == null || USER == null || PASS == null) {
            load_credentials();
        }
        connection = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * For creation of database schema only. Run once.
     */
    public static void main(String[] args) {
        create_db_schema();
        create_users();
    }

    /**
     * Creates necessary tables in the database.
     */
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

    /**
     * Creates some default users in the database.
     */
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

    /**
     * Gets all User objects from the database.
     * @return A list of User objects.
     */
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

    /**
     * Gets all ticket objects from the database.
     * @return A list Ticket objects.
     */
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
                int userId = mes_res.getInt(3);
                String content = mes_res.getString(4);
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

    /**
     * Creates a ticket in the database, if possible. It is up to the UI to handle any updating (including the local cache) after this.
     * @param author The creator of this ticket.
     * @param subject The subject line for this ticket.
     * @return True if the ticket was successfully created, false if not.
     */
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

    /**
     * Sets a ticket's state in the database, if possible. It is up to the UI to handle any updating (including the local cache) after this.
     * @param ticket The target ticket to set the state of.
     * @param target The target state to set.
     * @return True if a ticket was successfully updated, false if not.
     * @return
     */
    public static boolean setTicketState(Ticket ticket, Ticket.TicketState target) {
        boolean success = false;
        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement("UPDATE `kstev_tickets` SET `state`=? WHERE `id`=?");
            stmt.setInt(1, target.ordinal());
            stmt.setInt(2, ticket.getId());
            int amount = stmt.executeUpdate();
            if(amount > 1) {
                System.out.println("FATAL: Somehow a ticket update statement edited more that one ticket with ID: " + ticket.getId());
            }
            success = amount > 0;
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return success;
    }

    /**
     * Deletes a ticket from the database, if possible. It is up to the UI to handle any updating (including the local cache) after this.
     * @param ticket The target ticket to delete.
     * @return True if a ticket was successfully deleted, false if not.
     * @return
     */
    public static boolean deleteTicket(Ticket ticket) {
        boolean success = false;
        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM `kstev_tickets` WHERE id=?");
            stmt.setInt(1, ticket.getId());
            int amount = stmt.executeUpdate();
            if(amount > 1) {
                System.out.println("FATAL: Somehow a delete statement deleted more that one ticket with ID: " + ticket.getId());
            }
            success = amount > 0;
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return success;
    }

    /**
     * Sends a message attached to a ticket, if possible. It is up to the UI to handle any updating (including the local cache) after this.
     * @param ticket The ticket to add the message to.
     * @param sender The User who sent the message.
     * @param text The message to send.
     * @return True if the message was successfully created, false if not.
     */
    public static boolean sendMessage(Ticket ticket, User sender, String text) {
        boolean success = false;
        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO `kstev_ticket_messages` (ticket_id,sentAt,author,content) VALUES (?,NOW(),(SELECT id FROM `kstev_users` WHERE username=?),?)");
            stmt.setInt(1, ticket.getId());
            stmt.setString(2, sender.getUsername());
            stmt.setString(3, text);
            stmt.executeUpdate();
            success = true;
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return success;
    }

    /**
     * Sets a ticket's subject in the database, if possible. It is up to the UI to handle any updating (including the local cache) after this.
     * @param ticket The target ticket to set the state of.
     * @param subject The subject to set.
     * @return True if a ticket was successfully updated, false if not.
     * @return
     */
    public static boolean setTicketSubject(Ticket ticket, String subject) {
        boolean success = false;
        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement("UPDATE `kstev_tickets` SET `subject`=? WHERE `id`=?");
            stmt.setString(1, subject);
            stmt.setInt(2, ticket.getId());
            int amount = stmt.executeUpdate();
            if(amount > 1) {
                System.out.println("FATAL: Somehow a ticket update statement edited more that one ticket with ID: " + ticket.getId());
            }
            success = amount > 0;
            connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return success;
    }

}
