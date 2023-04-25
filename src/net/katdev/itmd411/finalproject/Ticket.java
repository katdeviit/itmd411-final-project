package net.katdev.itmd411.finalproject;

import java.util.Date;
import java.util.List;

public class Ticket {

    private int id;
    private String subject;
    private User author;
    private Date createdAt;
    private TicketState state;
    private List<TicketMessage> messages;

    public Ticket(int id, String subject, User author, Date createdAt, TicketState state, List<TicketMessage> messages) {
        this.id = id;
        this.subject = subject;
        this.author = author;
        this.createdAt = createdAt;
        this.state = state;
        this.messages = messages;
    }

    public int getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public User getAuthor() {
        return author;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public TicketState getState() {
        return state;
    }

    public List<TicketMessage> getMessages() {
        return messages;
    }

    public static class TicketMessage {

        private Date sentAt;
        private User author;
        private String content;

        public TicketMessage(Date sentAt, User author, String content) {
            this.sentAt = sentAt;
            this.author = author;
            this.content = content;
        }

        public Date getSentAt() {
            return sentAt;
        }

        public User getAuthor() {
            return author;
        }

        public String getContent() {
            return content;
        }
    }

    public enum TicketState {
        OPEN,
        CLOSED;
    }
}
