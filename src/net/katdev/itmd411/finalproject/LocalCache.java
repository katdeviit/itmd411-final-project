package net.katdev.itmd411.finalproject;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalCache {

    protected static Map<Integer, User> users;
    protected static Map<Integer, Ticket> tickets;
    protected static User loggedInUser;

    /**
     * Gets a user by its ID. Note this will only use the local cache. Use reload_users() to get the latest data.
     * @param id User ID to find.
     * @return A User object, or null if no such object was found.
     */
    public static User getUser(int id) {
        ensure_users();
        return users.getOrDefault(id, null);
    }

    /**
     * Gets a ticket by its ID. Note this will only use the local cache. Use reload_tickets() to get the latest data.
     * @param id Ticket ID to find.
     * @return A Ticket object, or null if no such object was found.
     */
    public static Ticket getTicket(int id) {
        ensure_tickets();
        return tickets.getOrDefault(id, null);
    }

    /**
     * Caches user data from the database if the user list does not exist.
     * If fresh data is needed, use reload_users()
     */
    public static void ensure_users() {
        if(tickets != null) {
            return;
        }
        users = DatabaseManager.getUsers().stream().collect(Collectors.toMap(user -> user.getId(), Function.identity()));
    }

    /**
     * Caches ticket data from the database if the ticket list does not exist.
     * If fresh data is needed, use reload_tickets()
     */
    public static void ensure_tickets() {
        if(tickets != null) {
            return;
        }
        tickets = DatabaseManager.getTickets().stream().collect(Collectors.toMap(ticket -> ticket.getId(), Function.identity()));
    }

    /**
     * Nulls the local user cache, meaning it will reload from the database next time it is requested.
     */
    public static void reload_users() {
        users = null;
    }

    /**
     * Nulls the local ticket cache, meaning it will reload from the database next time it is requested.
     */
    public static void reload_tickets() {
        tickets = null;
    }

    /**
     * Gets all tickets.
     * @return A list of tickets.
     */
    public static Collection<Ticket> getAllTickets() {
        ensure_tickets();
        return tickets.values();
    }

    /**
     * Gets all the tickets a user can see.
     * @param user The user to check tickets for
     * @return A list of tickets
     */
    public static Collection<Ticket> getTicketsForUser(User user) {
        ensure_tickets();
        return tickets.values().stream().filter(ticket -> user.isAdmin() || ticket.getAuthor().getId() == user.getId()).collect(Collectors.toList());
    }

    /**
     * Loads all data from the database, if it is not present.
     */
    public static void initCache() {
        ensure_users();
        ensure_tickets();
    }

    /**
     * @return The currently logged in user
     */
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Checks that a user exists with the given credentials, and sets the logged in user to it.
     * @param username The username to check against
     * @param password The password to verify
     * @return true if the user exists with those credentials, false if the credentials are wrong or there is no such user.
     */
    public static boolean login(String username, String password) {
        int userId = DatabaseManager.verifyUserCredentials(username, password);
        if(userId == -1) {
            return false;
        }
        loggedInUser = getUser(userId);
        return loggedInUser != null;
    }

    /**
     * Clears the cached logged in user. Does nothing else, it is up to the UI to handle this.
     */
    public static void logout() {
        loggedInUser = null;
    }
}
