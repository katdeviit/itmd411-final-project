package net.katdev.itmd411.finalproject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.HashMap;

public class TicketMessageUI {

    private static final Color SHADE_COLOR = new Color(230, 230, 230);
    private static HashMap<Integer, JPanel> message_panels = new HashMap<>();

    public static void view(Ticket ticket) {
        if(message_panels.containsKey(ticket.getId())) {
            return; // there is already an open window for this ticket
        }
        JFrame ticket_message_ui = new JFrame() {
            @Override
            public void dispose() {
                super.dispose();
                message_panels.remove(ticket.getId());
            }
        };
        ticket_message_ui.setSize(550, 600);
        ticket_message_ui.setLocationRelativeTo(null); // center the window
        ticket_message_ui.setTitle("Ticket Manager - Ticket View");
        ticket_message_ui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ticket_message_ui.setLayout(new BorderLayout());

        JPanel pageContainerPanel = new JPanel();
        pageContainerPanel.setLayout(new BoxLayout(pageContainerPanel, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(0, 1));
        topPanel.setBackground(SHADE_COLOR);
        topPanel.setMaximumSize(new Dimension(550, 100));

        JPanel topPanelInner = new JPanel();
        topPanelInner.setLayout(new BoxLayout(topPanelInner, BoxLayout.Y_AXIS));
        topPanelInner.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanelInner.setBackground(SHADE_COLOR);
        topPanelInner.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel subjectLine = new JLabel("Ticket #" + ticket.getId() + ": " + ticket.getSubject());
        Font f = subjectLine.getFont();
        subjectLine.setFont(new Font(f.getFontName(), Font.BOLD, 20));
        subjectLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        topPanelInner.add(subjectLine);

        JPanel createdPanel = new JPanel();
        createdPanel.setLayout(new BoxLayout(createdPanel, BoxLayout.X_AXIS));
        createdPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        createdPanel.setBackground(SHADE_COLOR);

        JLabel dateLine = new JLabel("Created " + TicketListUI.DATE_FORMAT.format(ticket.getCreatedAt()) + " by ", SwingConstants.LEFT);

        JLabel usernameLabel = new JLabel(ticket.getAuthor().getUsername(), SwingConstants.LEFT);
        usernameLabel.setForeground(ticket.getAuthor().getUserColor());
        if(ticket.getAuthor().isAdmin()) {
            usernameLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        }

        createdPanel.add(dateLine);
        createdPanel.add(usernameLabel);

        topPanelInner.add(createdPanel);

        topPanel.add(topPanelInner);

        pageContainerPanel.add(topPanel);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        message_panels.put(ticket.getId(), messagePanel);

        buildMessagePanel(ticket, messagePanel, ticket_message_ui);

        JScrollPane scrollPane = new UpdatingScrollPane(messagePanel);
        pageContainerPanel.add(scrollPane);

        ticket_message_ui.add(pageContainerPanel, BorderLayout.CENTER);

        JPanel messengerPanel = new JPanel();
        messengerPanel.setLayout(new BorderLayout());

        JPanel messageTextPanel = new JPanel();
        messageTextPanel.setLayout(new BorderLayout());

        JTextField messageField = new JTextField();
        messageField.setName("message");
        messageField.setSize(520, 20);
        messageField.addActionListener((ActionEvent action) -> {
            sendMessage(ticket_message_ui, ticket, messageField.getText());
            messageField.setText("");
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener((ActionEvent action) -> {
            sendMessage(ticket_message_ui, ticket, messageField.getText());
            messageField.setText("");
        });

        messageTextPanel.add(messageField, BorderLayout.CENTER);
        messageTextPanel.add(sendButton, BorderLayout.LINE_END);

        messengerPanel.add(messageTextPanel, BorderLayout.CENTER);

        ticket_message_ui.add(messengerPanel, BorderLayout.PAGE_END);

        ticket_message_ui.setVisible(true);
    }

    public static void buildMessagePanel(Ticket ticket, JPanel messagePanel, JFrame frame) {
        for(Ticket.TicketMessage message : ticket.getMessages()) {
            JPanel messageWrapperPanel = new JPanel();
            messageWrapperPanel.setLayout(new BorderLayout());
            messageWrapperPanel.setMaximumSize(new Dimension(550, 60));
            messageWrapperPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
            messageWrapperPanel.setBackground(frame.getBackground()); // margin between messages

            JPanel messageContentPanel = new JPanel();
            messageContentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            messageContentPanel.setLayout(new BoxLayout(messageContentPanel, BoxLayout.Y_AXIS));
            // Align based on author status
            float align = ticket.getAuthor().getId() == message.getAuthor().getId() ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT;
            messageContentPanel.setAlignmentX(align);
            messageContentPanel.setBackground(SHADE_COLOR);
            JPanel messageTopLine = new JPanel();
            messageTopLine.setLayout(new BoxLayout(messageTopLine, BoxLayout.X_AXIS));
            messageTopLine.setAlignmentX(align);
            messageTopLine.setBackground(SHADE_COLOR);
            JLabel messageAuthorLabel = new JLabel(message.getAuthor().getUsername(), SwingConstants.LEFT);
            Font f = messageAuthorLabel.getFont();
            messageAuthorLabel.setForeground(message.getAuthor().getUserColor());
            if(message.getAuthor().isAdmin()) {
                messageAuthorLabel.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
            }
            JLabel sentAtLabel = new JLabel(" (" + TicketListUI.DATE_FORMAT.format(message.getSentAt()) + ")");
            JLabel messageContent = new JLabel(message.getContent());
            messageContent.setAlignmentX(align);
            messageTopLine.add(messageAuthorLabel);
            messageTopLine.add(sentAtLabel);
            messageContentPanel.add(messageTopLine);
            messageContentPanel.add(messageContent);
            messageWrapperPanel.add(messageContentPanel);
            messagePanel.add(messageWrapperPanel, ticket.getAuthor().getId() == message.getAuthor().getId() ? BorderLayout.LINE_START : BorderLayout.LINE_END);
        }
    }

    public static void sendMessage(JFrame frame, Ticket ticket, String text) {
        System.out.println("Sending message on ticket " + ticket.getId() + "...");
        if(DatabaseManager.sendMessage(ticket, LocalCache.getLoggedInUser(), text)) {
            LocalCache.reload_tickets();
            JPanel messagePanel = message_panels.get(ticket.getId());
            Ticket newTicket = LocalCache.getTicket(ticket.getId()); // the old ticket object is outdated, rebuild it
            if (messagePanel != null) {
                messagePanel.removeAll();
                buildMessagePanel(newTicket, messagePanel, frame);
                messagePanel.revalidate();
            }
            System.out.println("Message sent successfully: " + text);
        } else {
            JOptionPane.showConfirmDialog(frame, "Failed to send message!");
        }
    }

    private static class UpdatingScrollPane extends JScrollPane {
        protected int prevMax = 0;

        public UpdatingScrollPane(JPanel messagePanel) {
            super(messagePanel);
            this.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                public void adjustmentValueChanged(AdjustmentEvent e) {
                    int newMax = e.getAdjustable().getMaximum();
                    if (prevMax != newMax) {
                        e.getAdjustable().setValue(newMax);
                    }
                    prevMax = newMax;
                }
            });
        }
    }
}
