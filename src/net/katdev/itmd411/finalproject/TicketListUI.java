package net.katdev.itmd411.finalproject;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Collection;

public class TicketListUI {

    public static JFrame ticket_list_ui;
    public static JTable tickets_table;

    public static void createTicketListUI() {
        ticket_list_ui = new JFrame();
        ticket_list_ui.setSize(500, 400);
        ticket_list_ui.setLocationRelativeTo(null); // center the window
        ticket_list_ui.setTitle("Ticket Manager - Tickets");
        ticket_list_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        User loggedIn = LocalCache.getLoggedInUser();

        // Header containing user info and logout button
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
        JPanel loginTextPanel = new JPanel();
        // Center horizontally
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalGlue());
        box.add(loginTextPanel);
        box.add(Box.createHorizontalGlue());

        JLabel login = new JLabel("Welcome, ");
        Font f = login.getFont();
        login.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        JLabel username = new JLabel(loggedIn.username + (loggedIn.admin ? " (Admin)" : ""));
        username.setForeground(loggedIn.userColor);
        if(loggedIn.admin) {
            username.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        }
        loginTextPanel.add(login);
        loginTextPanel.add(username);

        headerPanel.add(box);

        // Create tickets table and panel
        JPanel ticketsPanel = new JPanel();
        ticketsPanel.setLayout(new BoxLayout(ticketsPanel, BoxLayout.PAGE_AXIS));

        tickets_table = updateTicketsTable();
        tickets_table.setPreferredSize(new Dimension(460, 80));
        tickets_table.setMaximumSize(new Dimension(460, 1000));

        // Center vertically
        Box vertBox = new Box(BoxLayout.Y_AXIS);
        vertBox.add(Box.createVerticalGlue());
        vertBox.add(new JScrollPane(tickets_table));
        vertBox.add(Box.createVerticalGlue());

        ticketsPanel.add(vertBox);

        ticket_list_ui.add(headerPanel, BorderLayout.PAGE_START);
        ticket_list_ui.add(ticketsPanel, BorderLayout.CENTER);

        ticket_list_ui.setVisible(true);
    }

    public static JTable updateTicketsTable() {
        Collection<Ticket> tickets = LocalCache.getTicketsForUser(LocalCache.getLoggedInUser());
        Object[][] ticketData = new Object[tickets.size()][6];
        int ticketIndex = 0;
        for(Ticket ticket : tickets) {
            ticketData[ticketIndex] = new Object[] {
                    ticket.id, ticket.author.username, ticket.subject, ticket.state.name(), ticket.createdAt,
                    ticket.messages.stream().map(t -> t.sentAt).sorted((t1, t2) -> Long.valueOf(t2.getTime()).compareTo(t1.getTime())).findFirst().orElse(ticket.createdAt)
            };
            ticketIndex++;
        }
        JTable table = new JTable(ticketData, new Object[] {"#", "Author", "Subject", "State", "Created At"});
        table.setBounds(500, 500, 700, 800);
        return table;
    }
}
