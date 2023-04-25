package net.katdev.itmd411.finalproject;

import javax.swing.*;

public class TicketMessageUI {

    public static void view(Ticket ticket) {
        JOptionPane.showConfirmDialog(TicketListUI.ticket_list_ui, "Ticket " + ticket.id);
    }

}
