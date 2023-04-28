package net.katdev.itmd411.finalproject;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TicketListUI {

    public static JFrame ticket_list_ui;
    public static JTable tickets_table;
    public static DefaultTableModel tickets_table_model;

    public static final Object[] HEADERS = new Object[] {"Last Updated", "#", "Author", "Subject", "State", "Created At", ""};
    public static final Object[] HEADERS_ADMIN = new Object[] {"Last Updated", "#", "Author", "Subject", "State", "Created At", "", "", ""};
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static void createTicketListUI() {
        if(ticket_list_ui != null) {
            ticket_list_ui.dispose();
            tickets_table = null;
            tickets_table_model = null;
        }
        ticket_list_ui = new JFrame();
        User loggedIn = LocalCache.getLoggedInUser();
        // Admins need extra width for buttons
        ticket_list_ui.setSize(loggedIn.isAdmin() ? 950 : 800, 400);
        ticket_list_ui.setLocationRelativeTo(null); // center the window
        ticket_list_ui.setTitle("Ticket Manager - Tickets");
        ticket_list_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Header containing user info and logout button
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new GridLayout(2, 0));
        JPanel loginTextPanel = new JPanel();
        // Center horizontally
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalGlue());
        box.add(loginTextPanel);
        box.add(Box.createHorizontalGlue());

        // Create top panel actions
        JPanel ticketActionPanel = new JPanel();
        ticketActionPanel.setLayout(new BoxLayout(ticketActionPanel, BoxLayout.LINE_AXIS));
        JButton openTicketButton = new JButton("Create Ticket");
        openTicketButton.addActionListener((ActionEvent e) -> {
            System.out.println("Creating ticket.");
            String subject = JOptionPane.showInputDialog(ticket_list_ui, "Enter ticket subject.");
            if(subject != null) {
                subject = subject.trim(); // remove whitespace at start and end. prevents making a ticket that is just whitespace.
            }
            if(subject == null || subject.length() <= 0) {
                System.out.println("Ticket creation cancelled.");
                return;
            }
            // Create ticket and reload UI
            if(DatabaseManager.createTicket(LocalCache.getLoggedInUser(), subject)) {
                LocalCache.reload_tickets();
                fill_tickets_table();
                System.out.println("Ticket created.");
            } else {
                JOptionPane.showMessageDialog(ticket_list_ui, "Ticket creation failed! See console for additional error information.");
                System.out.println("Ticket creation failed.");
            }
        });
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener((ActionEvent e) -> {
            System.out.println("Reloading tickets.");
            LocalCache.reload_tickets();
            fill_tickets_table();
            System.out.println("Tickets reloaded.");
        });
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener((ActionEvent e) -> {
            System.out.println("Logging out.");
            LocalCache.logout();
            ticket_list_ui.dispose();
            LoginUI.createLoginUI();
        });
        ticketActionPanel.add(openTicketButton);
        ticketActionPanel.add(refreshButton);
        ticketActionPanel.add(logoutButton);

        // Show currently logged in user
        JLabel login = new JLabel("Welcome"  + (loggedIn.isAdmin() ? " Administrator" : "") + ",");
        Font f = login.getFont();
        login.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        JLabel username = new JLabel(loggedIn.getUsername());
        username.setForeground(loggedIn.getUserColor());
        if(loggedIn.isAdmin()) {
            username.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
        }
        loginTextPanel.add(login);
        loginTextPanel.add(username);

        // Center horizontally
        headerPanel.add(box);
        Box box3 = new Box(BoxLayout.X_AXIS);
        box3.add(Box.createHorizontalGlue());
        box3.add(ticketActionPanel);
        box3.add(Box.createHorizontalGlue());
        headerPanel.add(box3);

        // Create tickets table and panel
        JPanel ticketsPanel = new JPanel(new BorderLayout());

        tickets_table = new JTable();
        // Disable editing
        tickets_table_model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        };
        tickets_table.setModel(tickets_table_model);
        // Insert data
        fill_tickets_table();
        // Add support for clicking button
        tickets_table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int column = tickets_table.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / tickets_table.getRowHeight();
                if (row < tickets_table.getRowCount() && row >= 0 && column < tickets_table.getColumnCount() && column >= 0) {
                    Object value = tickets_table.getValueAt(row, column);
                    if (value instanceof JButton) {
                        ((JButton) value).doClick();
                    }
                }
            }
        });

        ticketsPanel.add(new JScrollPane(tickets_table), BorderLayout.CENTER);

        ticket_list_ui.add(headerPanel, BorderLayout.PAGE_START);
        ticket_list_ui.add(ticketsPanel, BorderLayout.CENTER);

        ticket_list_ui.setVisible(true);
    }

    public static void fill_tickets_table() {
        User loggedIn = LocalCache.getLoggedInUser();
        tickets_table_model.setDataVector(getTicketData(), loggedIn.isAdmin() ? HEADERS_ADMIN : HEADERS);
        // Decrease width of dates
        tickets_table.getColumnModel().getColumn(0).setPreferredWidth(120);
        tickets_table.getColumnModel().getColumn(5).setPreferredWidth(120);
        tickets_table.getColumnModel().getColumn(0).setMaxWidth(120);
        tickets_table.getColumnModel().getColumn(5).setMaxWidth(120);
        // Decrease width of number
        tickets_table.getColumnModel().getColumn(1).setMaxWidth(35);
        tickets_table.getColumnModel().getColumn(1).setPreferredWidth(35);
        // Decrease username width
        tickets_table.getColumnModel().getColumn(2).setMaxWidth(80);
        // Decrease width of state
        tickets_table.getColumnModel().getColumn(4).setMaxWidth(60);
        tickets_table.getColumnModel().getColumn(4).setPreferredWidth(60);
        // Decrease width of button
        tickets_table.getColumnModel().getColumn(6).setMaxWidth(80);
        tickets_table.getColumnModel().getColumn(6).setPreferredWidth(80);
        if(loggedIn.isAdmin()) {
            tickets_table.getColumnModel().getColumn(7).setMaxWidth(70);
            tickets_table.getColumnModel().getColumn(7).setPreferredWidth(70);
            tickets_table.getColumnModel().getColumn(8).setMaxWidth(80);
            tickets_table.getColumnModel().getColumn(8).setPreferredWidth(80);
        }

        // Render dates properly
        TableCellRenderer dateTimeRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Date) {
                    value = DATE_FORMAT.format(value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        tickets_table.getColumnModel().getColumn(0).setCellRenderer(dateTimeRenderer);
        tickets_table.getColumnModel().getColumn(5).setCellRenderer(dateTimeRenderer);
        // Render the buttons and username labels properly
        TableCellRenderer tableRenderer = tickets_table.getDefaultRenderer(JButton.class);
        TableCellRenderer componentRenderer = (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) -> {
            if (value instanceof Component)
                return (Component) value;
            return tableRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        };
        tickets_table.getColumnModel().getColumn(6).setCellRenderer(componentRenderer);
        if(loggedIn.isAdmin()) {
            tickets_table.getColumnModel().getColumn(7).setCellRenderer(componentRenderer);
            tickets_table.getColumnModel().getColumn(8).setCellRenderer(componentRenderer);
        }
        tickets_table.getColumnModel().getColumn(2).setCellRenderer(componentRenderer);

        // Sort by last update by default
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tickets_table.getModel());
        tickets_table.setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    public static Object[][] getTicketData() {
        User loggedIn = LocalCache.getLoggedInUser();
        // Sort by last update
        Collection<Ticket> tickets = LocalCache.getTicketsForUser(loggedIn);
        Object[][] ticketData = new Object[tickets.size()][loggedIn.isAdmin() ? 8 : 6];
        int ticketIndex = 0;
        for(Ticket ticket : tickets) {
            JButton button = new JButton("View");
            button.addActionListener((ActionEvent e) -> {
                System.out.println("Viewing ticket " + ticket.getId() + ".");
                TicketMessageUI.view(ticket);
            });
            JLabel usernameLabel = new JLabel(ticket.getAuthor().getUsername());
            usernameLabel.setForeground(ticket.getAuthor().getUserColor());
            List<Object> data = new ArrayList<>();
            data.add(ticket.getMessages().stream().map(t -> t.getSentAt()).sorted((t1, t2) -> Long.valueOf(t1.getTime()).compareTo(t2.getTime())).findFirst().orElse(ticket.getCreatedAt()));
            data.add(ticket.getId());
            data.add(usernameLabel);
            data.add(ticket.getSubject());
            data.add(ticket.getState().name());
            data.add(ticket.getCreatedAt());
            data.add(button);
            if(loggedIn.isAdmin()) {
                JButton stateButton = new JButton(ticket.getState() == Ticket.TicketState.OPEN ? "Close" : "Open");
                stateButton.addActionListener((ActionEvent e) -> {
                    System.out.println("Opening/closing ticket " + ticket.getId() + ".");
                    Ticket.TicketState target = ticket.getState() == Ticket.TicketState.OPEN ? Ticket.TicketState.CLOSED : Ticket.TicketState.OPEN;
                    if(DatabaseManager.setTicketState(ticket, target)) {
                        LocalCache.reload_tickets();
                        fill_tickets_table();
                        System.out.println("Ticket state is now: " + target.name() + ".");
                    } else {
                        JOptionPane.showMessageDialog(ticket_list_ui, "Ticket state alteration failed! See console for additional error information.");
                        System.out.println("Ticket state alteration failed.");
                    }
                });
                data.add(stateButton);
                JButton deleteButton = new JButton("Delete");
                deleteButton.addActionListener((ActionEvent e) -> {
                    System.out.println("Deleting ticket " + ticket.getId() + ".");
                    int result = JOptionPane.showConfirmDialog(ticket_list_ui, "Are you sure you want to delete ticket " + ticket.getId() + "?");
                    if(result != 0) {
                        System.out.println("Deletion cancelled.");
                        return;
                    }
                    if(DatabaseManager.deleteTicket(ticket)) {
                        LocalCache.reload_tickets();
                        fill_tickets_table();
                        System.out.println("Ticket deleted.");
                    } else {
                        JOptionPane.showMessageDialog(ticket_list_ui, "Ticket deletion failed! See console for additional error information.");
                        System.out.println("Ticket deletion failed.");
                    }
                });
                data.add(deleteButton);
            }
            ticketData[ticketIndex] = data.toArray(Object[]::new);
            ticketIndex++;
        }
        return ticketData;
    }

}
