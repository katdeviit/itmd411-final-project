package net.katdev.itmd411.finalproject;

import java.util.Date;
import java.util.List;

/**
 * An internal data storage object for tickets.
 */
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

    /**
     * @return The internal ID and number of this ticket. Globally unique identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * @return The headline/subject of this ticket.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return The author of this ticket. This should be the same as other User objects, however if you wish to compare users check their ID or username,
     * rather than the object itself.
     */
    public User getAuthor() {
        return author;
    }

    /**
     * @return The date and time this ticket was created at.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The state (open or closed) this ticket is in.
     */
    public TicketState getState() {
        return state;
    }

    /**
     * @return A list of messages on this ticket.
     */
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

        /**
         * @return The date and time this message was sent at.
         */
        public Date getSentAt() {
            return sentAt;
        }

        /**
         * @return The sender of this message.
         */
        public User getAuthor() {
            return author;
        }

        /**
         * @return The content of this message.
         */
        public String getContent() {
            return content;
        }
    }

    public enum TicketState {
        OPEN,
        CLOSED;
    }
}
