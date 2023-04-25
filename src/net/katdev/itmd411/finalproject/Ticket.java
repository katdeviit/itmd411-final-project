package net.katdev.itmd411.finalproject;

import java.util.Date;
import java.util.List;

public class Ticket {

    public int id;
    public String subject;
    public User author;
    public Date createdAt;
    public TicketState state;
    public List<TicketMessage> messages;

    public Ticket(int id, String subject, User author, Date createdAt, TicketState state, List<TicketMessage> messages) {
        this.id = id;
        this.subject = subject;
        this.author = author;
        this.createdAt = createdAt;
        this.state = state;
        this.messages = messages;
    }

    public static class TicketMessage {

        public Date sentAt;
        public User author;
        public String content;

        public TicketMessage(Date sentAt, User author, String content) {
            this.sentAt = sentAt;
            this.author = author;
            this.content = content;
        }

    }

    public enum TicketState {
        OPEN,
        CLOSED;
    }
}
