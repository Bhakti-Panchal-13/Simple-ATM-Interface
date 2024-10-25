package atm_interface;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SimpleATM extends JFrame {
    private JPanel panel;
    private JButton checkBalanceButton, depositButton, withdrawButton, exitButton, loginButton;
    private JTextField pinField;
    private JLabel welcomeLabel, balanceLabel;
    private Connection conn;
    private String pin = "";
    private int accountNumber; // Account number will be retrieved dynamically after login

    public SimpleATM() {
        // Set up the frame
        setTitle("ATM Interface");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new JPanel();
        add(panel);
        panel.setLayout(null);
        
        // Welcome screen
        welcomeLabel = new JLabel("Welcome to the ATM");
        welcomeLabel.setBounds(120, 30, 150, 25);
        panel.add(welcomeLabel);

        // Pin input field
        pinField = new JTextField(20);
        pinField.setBounds(140, 80, 120, 25);
        panel.add(pinField);
        
        // Login button
        loginButton = new JButton("Login");
        loginButton.setBounds(150, 120, 100, 25);
        panel.add(loginButton);

        // Login button action
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pin = pinField.getText();
                if (authenticate(pin)) {
                    loadMainMenu();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Pin. Try Again.");
                    pinField.setText("");
                }
            }
        });

        connectDatabase();
    }

    // Main menu after login
    private void loadMainMenu() {
        panel.removeAll();
        panel.repaint();

        balanceLabel = new JLabel("Balance: ");
        balanceLabel.setBounds(120, 30, 200, 25);
        panel.add(balanceLabel);

        checkBalanceButton = new JButton("Check Balance");
        checkBalanceButton.setBounds(120, 60, 150, 25);
        panel.add(checkBalanceButton);

        depositButton = new JButton("Deposit");
        depositButton.setBounds(120, 100, 150, 25);
        panel.add(depositButton);

        withdrawButton = new JButton("Withdraw");
        withdrawButton.setBounds(120, 140, 150, 25);
        panel.add(withdrawButton);

        exitButton = new JButton("Exit");
        exitButton.setBounds(120, 180, 150, 25);
        panel.add(exitButton);

        // Check balance action
        checkBalanceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                double balance = getBalance(accountNumber);
                balanceLabel.setText("Balance: " + balance);
            }
        });

        // Deposit action
        depositButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String depositAmountStr = JOptionPane.showInputDialog("Enter amount to deposit:");
                double depositAmount = Double.parseDouble(depositAmountStr);
                updateBalance(accountNumber, depositAmount, true);
                JOptionPane.showMessageDialog(null, "Deposited: " + depositAmount);
            }
        });

        // Withdraw action
        withdrawButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String withdrawAmountStr = JOptionPane.showInputDialog("Enter amount to withdraw:");
                double withdrawAmount = Double.parseDouble(withdrawAmountStr);
                if (withdrawAmount <= getBalance(accountNumber)) {
                    updateBalance(accountNumber, withdrawAmount, false);
                    JOptionPane.showMessageDialog(null, "Withdrew: " + withdrawAmount);
                } else {
                    JOptionPane.showMessageDialog(null, "Insufficient balance.");
                }
            }
        });

        // Exit action
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                panel.removeAll();
                panel.repaint();
                loadWelcomeScreen();
            }
        });
    }

    // Database connection
    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/atm_db", "root", "Krishna@1210");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Authenticate the pin and retrieve the account number dynamically
    private boolean authenticate(String pin) {
        try {
            String query = "SELECT account_number FROM accounts WHERE pin = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                accountNumber = rs.getInt("account_number"); // Get the account number associated with the pin
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get balance from the database
    private double getBalance(int accountNumber) {
        double balance = 0.0;
        try {
            String query = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    // Update balance in the database
    private void updateBalance(int accountNumber, double amount, boolean isDeposit) {
        double currentBalance = getBalance(accountNumber);
        double newBalance = isDeposit ? currentBalance + amount : currentBalance - amount;
        try {
            String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, accountNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load the welcome screen
    private void loadWelcomeScreen() {
        welcomeLabel.setText("Welcome to the ATM");
        pinField.setText("");
        panel.add(welcomeLabel);
        panel.add(pinField);
        panel.add(loginButton);
    }

    public static void main(String[] args) {
        SimpleATM atm = new SimpleATM();
        atm.setVisible(true);
    }
}
