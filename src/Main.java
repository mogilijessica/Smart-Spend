import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class Main {

    // Theme colors
    private static final Color NAVY = new Color(30, 48, 80);
    private static final Color GREEN = new Color(34, 160, 107);
    private static final Color RED = new Color(230, 100, 90);
    private static final Color BACKGROUND = new Color(243, 246, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(45, 55, 72);
    private static final Color GRAY = new Color(120, 130, 145);

    // Stores all transactions
   static ArrayList<Object[]> transactions = new ArrayList<>();
   static DefaultTableModel transactionModel;
   static JLabel incomeAmountLabel;
   static JLabel expenseAmountLabel;
   static JLabel balanceAmountLabel;
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> createGUI());
    }
    public static void createGUI() {
        // Main window
        JFrame frame = new JFrame("SmartSpend - Personal Expense Tracker");
        frame.setSize(1100, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);

        // Sidebar
        JPanel sidebar = createSidebar();

        // Main dashboard area
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BACKGROUND);
        dashboard.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header
        JPanel header = createHeader(frame);

        // Dashboard content
        JPanel content = createDashboardContent();

        dashboard.add(header, BorderLayout.NORTH);
        dashboard.add(content, BorderLayout.CENTER);

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(dashboard, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Create sidebar
    public static JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel logo = new JLabel("  SmartSpend");
        logo.setFont(new Font("Arial", Font.BOLD, 22));
        logo.setForeground(NAVY);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(40));

        sidebar.add(createNavButton("Dashboard", true));
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(createNavButton("Transactions", false));
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(createNavButton("Budget", false));
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(createNavButton("Analytics", false));
        sidebar.add(Box.createVerticalStrut(12));

        sidebar.add(createNavButton("Settings", false));

        return sidebar;
    }

    // Create navigation button
    public static JButton createNavButton(String text, boolean selected) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(170, 45));

        if (selected) {
            button.setBackground(new Color(230, 240, 255));
            button.setForeground(new Color(35, 115, 220));
        } else {
            button.setBackground(WHITE);
            button.setForeground(TEXT);
        }

        button.setOpaque(true);

        return button;
    }

    // Create header
    public static JPanel createHeader(JFrame frame) {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        header.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel welcome = new JLabel("Welcome to SmartSpend!");
        welcome.setFont(new Font("Arial", Font.BOLD, 24));
        welcome.setForeground(NAVY);

        JLabel subtitle = new JLabel("Here's your financial overview.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(BACKGROUND);

        textPanel.add(welcome);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        JButton addButton = new JButton("+ Add Transaction");
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setBackground(GREEN);
        addButton.setForeground(WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> showTransactionDialog(frame));
        header.add(textPanel, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        return header;
    }

    // Create dashboard content
    public static JPanel createDashboardContent() {

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);

        // Summary cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 0));
        cards.setBackground(BACKGROUND);

        cards.add(createSummaryCard("Total Income", GREEN));
        cards.add(createSummaryCard("Total Expenses", RED));
        cards.add(createSummaryCard("Available Balance", NAVY));

        content.add(cards);
        content.add(Box.createVerticalStrut(20));

        // Budget card
        JPanel budgetCard = createCard("Monthly Budget", "₹5,000", GREEN);
        content.add(budgetCard);
        content.add(Box.createVerticalStrut(20));

        // Recent transactions
        JPanel transactions = createTransactionsPanel();
        content.add(transactions);

        return content;
    }

    // Create summary card
    public static JPanel createCard(String title, String amount, Color amountColor) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        titleLabel.setForeground(GRAY);

        JLabel amountLabel = new JLabel(amount);
        amountLabel.setFont(new Font("Arial", Font.BOLD, 25));
        amountLabel.setForeground(amountColor);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(amountLabel);

        return card;
    }

    // Create dynamic summary card
public static JPanel createSummaryCard(String title, Color amountColor) {

    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(WHITE);
    card.setBorder(new EmptyBorder(20, 20, 20, 20));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(new Font("Arial", Font.PLAIN, 15));
    titleLabel.setForeground(GRAY);

    JLabel amountLabel = new JLabel("₹0.00");
    amountLabel.setFont(new Font("Arial", Font.BOLD, 25));
    amountLabel.setForeground(amountColor);

    card.add(titleLabel);
    card.add(Box.createVerticalStrut(15));
    card.add(amountLabel);

    // Store the amount labels for updating
    if (title.equals("Total Income")) {
        incomeAmountLabel = amountLabel;
    }
    else if (title.equals("Total Expenses")) {
        expenseAmountLabel = amountLabel;
    }
    else if (title.equals("Available Balance")) {
        balanceAmountLabel = amountLabel;
    }

    return card;
}

// Calculate income, expenses and balance
public static void updateSummaryCards() {

    double totalIncome = 0;
    double totalExpenses = 0;

    // Visit every transaction
    for (Object[] transaction : transactions) {

        String type = (String) transaction[0];
        double amount = (double) transaction[1];

        if (type.equals("Income")) {
            totalIncome += amount;
        }
        else if (type.equals("Expense")) {
            totalExpenses += amount;
        }
    }

    // Calculate balance
    double balance = totalIncome - totalExpenses;

    // Update dashboard cards
    incomeAmountLabel.setText(
        String.format("₹%.2f", totalIncome)
    );

    expenseAmountLabel.setText(
        String.format("₹%.2f", totalExpenses)
    );

    balanceAmountLabel.setText(
        String.format("₹%.2f", balance)
    );
}

    // Create transactions section
 // Create transactions section
public static JPanel createTransactionsPanel() {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(WHITE);
    panel.setBorder(new EmptyBorder(20, 20, 20, 20));

    JLabel heading = new JLabel("Recent Transactions");
    heading.setFont(new Font("Arial", Font.BOLD, 20));
    heading.setForeground(NAVY);

    // Table columns
    String[] columns = {
        "Category", "Description", "Amount", "Type"
    };

    // Create an empty table model
    transactionModel = new DefaultTableModel(columns, 0);

    // Create table
    JTable table = new JTable(transactionModel);

    table.setRowHeight(35);
    table.setFont(new Font("Arial", Font.PLAIN, 14));

    table.getTableHeader().setFont(
        new Font("Arial", Font.BOLD, 14)
    );

    JScrollPane scrollPane = new JScrollPane(table);

    panel.add(heading, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
}
    // Show Add Transaction form
public static void showTransactionDialog(JFrame frame) {

    // Transaction type
    String[] types = {"Expense", "Income"};
    JComboBox<String> typeBox = new JComboBox<>(types);

    // Categories
    String[] categories = {
        "Food", "Travel", "Shopping",
        "Education", "Entertainment",
        "Bills", "Health", "Other"
    };

    JComboBox<String> categoryBox =
            new JComboBox<>(categories);

    // Input fields
    JTextField amountField = new JTextField();
    JTextField descriptionField = new JTextField();

    JTextField dateField = new JTextField(
            java.time.LocalDate.now().toString()
    );

    // Form panel
    JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));

    form.add(new JLabel("Transaction Type:"));
    form.add(typeBox);

    form.add(new JLabel("Amount:"));
    form.add(amountField);

    form.add(new JLabel("Category:"));
    form.add(categoryBox);

    form.add(new JLabel("Description:"));
    form.add(descriptionField);

    form.add(new JLabel("Date (YYYY-MM-DD):"));
    form.add(dateField);

    // Show dialog
    int result = JOptionPane.showConfirmDialog(
            frame,
            form,
            "Add Transaction",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    // If user clicks OK
    if (result == JOptionPane.OK_OPTION) {

        try {

            double amount = Double.parseDouble(
                    amountField.getText()
            );

            if (amount <= 0) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Amount must be greater than zero!"
                );
                return;
            }

            String type = (String) typeBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();
            String description = descriptionField.getText();
            String date = dateField.getText();

            // Store transaction in our list
     transactions.add(new Object[]{
    type, amount, category, description, date
    });

// Add transaction to the dashboard table
     transactionModel.addRow(new Object[]{
    category,
    description,
    "₹" + amount,
    type
     });

     updateSummaryCards();

            JOptionPane.showMessageDialog(
                    frame,
                    "Transaction Details:\n\n"
                    + "Type: " + type + "\n"
                    + "Amount: ₹" + amount + "\n"
                    + "Category: " + category + "\n"
                    + "Description: " + description + "\n"
                    + "Date: " + date,
                    "Transaction Added!",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    frame,
                    "Please enter a valid amount!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
}