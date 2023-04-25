package net.katdev.itmd411.finalproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginUI {

    public static JFrame login_ui;

    public static void main(String[] args) {
        createLoginUI();
    }

    public static void createLoginUI() {
        login_ui = new JFrame();
        login_ui.setSize(320, 150);
        login_ui.setLocationRelativeTo(null); // center the window
        login_ui.setTitle("Ticket Manager - Login");
        login_ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel loginPanel = new JPanel();
        createLoginPanel(loginPanel);
        // Center vertically
        Box box = new Box(BoxLayout.Y_AXIS);
        box.add(Box.createVerticalGlue());
        box.add(loginPanel);
        box.add(Box.createVerticalGlue());
        login_ui.add(box);
        login_ui.setVisible(true);
    }

    public static void createLoginPanel(JPanel loginPanel) {
        // Split space vertically between fields and login
        loginPanel.setMaximumSize(new Dimension(280, 100));
        loginPanel.setLayout(new GridLayout(2, 0));
        // Split fields in half horizontally
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new GridLayout(0, 2));

        // Username field
        JTextField usernameField = new JTextField();
        usernameField.setName("username");
        usernameField.setSize(250, 20);
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setLabelFor(usernameField);

        fieldsPanel.add(usernameLabel);
        fieldsPanel.add(usernameField);

        // Password field
        JPasswordField passwordField = new JPasswordField();
        passwordField.setName("password");
        passwordField.setSize(250, 20);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setLabelFor(passwordField);

        fieldsPanel.add(passwordLabel);
        fieldsPanel.add(passwordField);

        // Add button
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener((ActionEvent e) -> {
            System.out.println("Logging in...");
            boolean isValid = LocalCache.login(usernameField.getText(), String.valueOf(passwordField.getPassword()));
            System.out.println("Valid login: " + isValid);
            if (isValid) {
                login_ui.dispose();
                TicketListUI.createTicketListUI();
            } else {
                JOptionPane.showMessageDialog(login_ui, "Invalid login!");
            }
        });

        loginPanel.add(fieldsPanel);
        loginPanel.add(loginButton);
    }

}
