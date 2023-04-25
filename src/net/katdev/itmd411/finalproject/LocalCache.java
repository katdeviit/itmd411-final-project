package net.katdev.itmd411.finalproject;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalCache {

    protected static Map<Integer, User> users;
    protected static Map<Integer, Ticket> tickets;
    protected static User loggedInUser;

    public static User getUser(int id) {
        ensure_users();
        return users.getOrDefault(id, null);
    }

    public static Ticket getTicket(int id) {
        ensure_tickets();
        return tickets.getOrDefault(id, null);
    }

    public static void ensure_users() {
        if(tickets != null) {
            return;
        }
        users = DatabaseManager.getUsers().stream().collect(Collectors.toMap(user -> user.id, Function.identity()));
    }

    public static void ensure_tickets() {
        if(tickets != null) {
            return;
        }
        tickets = DatabaseManager.getTickets().stream().collect(Collectors.toMap(ticket -> ticket.id, Function.identity()));
    }

    public static void reload_tickets() {
        tickets = null; // tickets will be grabbed again next time ensure_tickets is called
    }

    public static Collection<Ticket> getAllTickets() {
        ensure_tickets();
        return tickets.values();
    }

    public static Collection<Ticket> getTicketsForUser(User user) {
        ensure_tickets();
        return tickets.values().stream().filter(ticket -> user.admin || ticket.author.id == user.id).collect(Collectors.toList());
    }

    public static void initCache() {
        ensure_users();
        ensure_tickets();
    }

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static boolean login(String username, String password) {
        int userId = DatabaseManager.verifyUserCredentials(username, password);
        if(userId == -1) {
            return false;
        }
        loggedInUser = getUser(userId);
        return loggedInUser != null;
    }

    public static void logout() {
        loggedInUser = null;
    }
}
