import java.awt.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/*
 * SmartSpend - Personal Expense Tracker
 * Single-file Java Swing application.
 *
 * Features:
 * 1. Dashboard with income, expense, balance and budget overview
 * 2. Add, edit and delete transactions
 * 3. Search and filter transactions
 * 4. Monthly custom budgets and budget alerts
 * 5. Analytics by category and income/expense totals
 * 6. Save/load data using a CSV-style text file
 * 7. Export a report
 * 8. Background file operations using SwingWorker
 */
public class Main {

    // ---------------- COLORS / THEME ----------------
    private static final Color NAVY = new Color(30, 48, 80);
    private static final Color GREEN = new Color(34, 160, 107);
    private static final Color RED = new Color(230, 100, 90);
    private static final Color BACKGROUND = new Color(243, 246, 250);
    private static final Color WHITE = Color.WHITE;
    private static final Color TEXT = new Color(45, 55, 72);
    private static final Color GRAY = new Color(120, 130, 145);
    private static final Color BLUE = new Color(55, 115, 210);

    // ---------------- DATA ----------------
    // A transaction stores its type, amount, category, description and date.
    static class Transaction {
        String type;
        double amount;
        String category;
        String description;
        String date;

        Transaction(String type, double amount, String category,
                   String description, String date) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
        }
    }

    // Main data collections
    private static final ArrayList<Transaction> transactions = new ArrayList<>();
    // Budget is stored separately for each YYYY-MM month.
    private static final HashMap<String, Double> monthlyBudgets = new HashMap<>();

    // ---------------- GUI REFERENCES ----------------
    private static JFrame mainFrame;
    private static CardLayout cardLayout;
    private static JPanel mainContentPanel;

    private static DefaultTableModel dashboardTableModel;
    private static DefaultTableModel transactionsTableModel;
    private static JTable transactionsTable;
    private static TableRowSorter<DefaultTableModel> tableSorter;

    private static JLabel incomeLabel;
    private static JLabel expenseLabel;
    private static JLabel balanceLabel;
    private static JLabel budgetLabel;
    private static JLabel budgetProgressLabel;
    private static JProgressBar budgetProgressBar;

    private static JComboBox<String> monthBox;
    private static JLabel analyticsSummaryLabel;
    private static JPanel analyticsChartPanel;

    private static final String[] CATEGORIES = {
        "Food", "Travel", "Shopping", "Education", "Entertainment",
        "Bills", "Health", "Other"
    };
    private static final String DATA_FILE = "SmartSpendData.csv";

    // ---------------- START ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            loadDataInBackground();
            createGUI();
        });
    }

    // Builds the application's main window and all pages.
    private static void createGUI() {
        mainFrame = new JFrame("SmartSpend - Personal Expense Tracker");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1150, 760);
        mainFrame.setMinimumSize(new Dimension(950, 650));
        mainFrame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.add(createDashboardPage(), "Dashboard");
        mainContentPanel.add(createTransactionsPage(), "Transactions");
        mainContentPanel.add(createBudgetPage(), "Budget");
        mainContentPanel.add(createAnalyticsPage(), "Analytics");
        mainContentPanel.add(createReportsPage(), "Reports");
        mainContentPanel.add(createSettingsPage(), "Settings");

        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(mainContentPanel, BorderLayout.CENTER);
        mainFrame.setContentPane(root);

        refreshAllViews();
        mainFrame.setVisible(true);
    }

    // Sidebar navigation switches the CardLayout page.
    private static JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(WHITE);
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel logo = new JLabel("SmartSpend");
        logo.setFont(new Font("Arial", Font.BOLD, 22));
        logo.setForeground(NAVY);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(35));

        String[] pages = {"Dashboard", "Transactions", "Budget", "Analytics", "Reports", "Settings"};
        for (String page : pages) {
            JButton button = new JButton(page);
            button.setFont(new Font("Arial", Font.PLAIN, 15));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension(170, 44));
            button.setBackground(WHITE);
            button.setForeground(TEXT);
            button.setOpaque(true);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(e -> cardLayout.show(mainContentPanel, page));
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(9));
        }

        sidebar.add(Box.createVerticalGlue());
        JLabel version = new JLabel("Smart-Spend v1.0");
        version.setForeground(GRAY);
        version.setFont(new Font("Arial", Font.PLAIN, 11));
        sidebar.add(version);
        return sidebar;
    }

    // ---------------- DASHBOARD ----------------
    private static JPanel createDashboardPage() {
        JPanel page = pagePanel();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel welcome = new JPanel();
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        welcome.setBackground(BACKGROUND);
        JLabel title = heading("Welcome to SmartSpend!");
        JLabel subtitle = new JLabel("Here's your financial overview.");
        subtitle.setForeground(GRAY);
        welcome.add(title);
        welcome.add(Box.createVerticalStrut(5));
        welcome.add(subtitle);

        JButton addButton = primaryButton("+ Add Transaction");
        addButton.addActionListener(e -> showTransactionDialog(null));
        header.add(welcome, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BACKGROUND);

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setBackground(BACKGROUND);
        cards.add(summaryCard("Total Income", GREEN, true));
        cards.add(summaryCard("Total Expenses", RED, false));
        cards.add(summaryCard("Available Balance", NAVY, false));
        body.add(cards);
        body.add(Box.createVerticalStrut(18));

        JPanel budgetCard = whiteCard();
        budgetCard.setLayout(new BoxLayout(budgetCard, BoxLayout.Y_AXIS));
        JLabel budgetTitle = new JLabel("Current Month Budget");
        budgetTitle.setFont(new Font("Arial", Font.BOLD, 16));
        budgetTitle.setForeground(NAVY);
        budgetLabel = new JLabel("Budget: ₹0.00");
        budgetProgressLabel = new JLabel("Spent: ₹0.00 / ₹0.00");
        budgetProgressBar = new JProgressBar(0, 100);
        budgetProgressBar.setStringPainted(true);
        budgetCard.add(budgetTitle);
        budgetCard.add(Box.createVerticalStrut(8));
        budgetCard.add(budgetLabel);
        budgetCard.add(Box.createVerticalStrut(6));
        budgetCard.add(budgetProgressLabel);
        budgetCard.add(Box.createVerticalStrut(8));
        budgetCard.add(budgetProgressBar);
        body.add(budgetCard);
        body.add(Box.createVerticalStrut(18));

        JPanel recent = whiteCard();
        recent.setLayout(new BorderLayout(10, 10));
        JLabel recentTitle = new JLabel("Recent Transactions");
        recentTitle.setFont(new Font("Arial", Font.BOLD, 18));
        recentTitle.setForeground(NAVY);
        dashboardTableModel = nonEditableModel(new String[]{"Date", "Category", "Description", "Type", "Amount"});
        JTable recentTable = new JTable(dashboardTableModel);
        styleTable(recentTable);
        recent.add(recentTitle, BorderLayout.NORTH);
        recent.add(new JScrollPane(recentTable), BorderLayout.CENTER);
        body.add(recent);

        page.add(header, BorderLayout.NORTH);
        page.add(body, BorderLayout.CENTER);
        return page;
    }

    private static JPanel summaryCard(String title, Color color, boolean isIncome) {
        JPanel card = whiteCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setForeground(GRAY);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel amount = new JLabel("₹0.00");
        amount.setForeground(color);
        amount.setFont(new Font("Arial", Font.BOLD, 24));
        card.add(t);
        card.add(Box.createVerticalStrut(12));
        card.add(amount);
        if (title.equals("Total Income")) incomeLabel = amount;
        else if (title.equals("Total Expenses")) expenseLabel = amount;
        else balanceLabel = amount;
        return card;
    }

    // ---------------- TRANSACTIONS PAGE ----------------
    private static JPanel createTransactionsPage() {
        JPanel page = pagePanel();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        JPanel titleBlock = new JPanel();
        titleBlock.setBackground(BACKGROUND);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(heading("Transactions"));
        JLabel subtitle = new JLabel("Add, edit, delete, search and filter your records");
        subtitle.setForeground(GRAY);
        titleBlock.add(Box.createVerticalStrut(5));
        titleBlock.add(subtitle);

        JButton addButton = primaryButton("+ Add Transaction");
        addButton.addActionListener(e -> showTransactionDialog(null));
        header.add(titleBlock, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);

        // Search and filter controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBackground(BACKGROUND);
        JTextField searchField = new JTextField(18);
        JComboBox<String> filterBox = new JComboBox<>(
                new String[]{"All Transactions", "Income", "Expense"});
        controls.add(new JLabel("Search:"));
        controls.add(searchField);
        controls.add(new JLabel("Type:"));
        controls.add(filterBox);

        transactionsTableModel = nonEditableModel(
                new String[]{"Date", "Category", "Description", "Type", "Amount"});
        transactionsTable = new JTable(transactionsTableModel);
        styleTable(transactionsTable);
        tableSorter = new TableRowSorter<>(transactionsTableModel);
        transactionsTable.setRowSorter(tableSorter);

        // Search matches any visible text in the table.
        Runnable applyFilter = () -> {
            String text = searchField.getText().trim();
            String selectedType = (String) filterBox.getSelectedItem();
            ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
            if (!text.isEmpty()) filters.add(RowFilter.regexFilter("(?i)" + PatternQuote(text)));
            if (!"All Transactions".equals(selectedType)) {
                filters.add(RowFilter.regexFilter("^" + selectedType + "$", 3));
            }
            if (filters.isEmpty()) tableSorter.setRowFilter(null);
            else tableSorter.setRowFilter(RowFilter.andFilter(filters));
        };
        searchField.addActionListener(e -> applyFilter.run());
        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(applyFilter));
        filterBox.addActionListener(e -> applyFilter.run());

        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");
        editButton.addActionListener(e -> {
            int viewRow = transactionsTable.getSelectedRow();
            if (viewRow < 0) {
                showMessage("Please select a transaction to edit.");
                return;
            }
            int modelRow = transactionsTable.convertRowIndexToModel(viewRow);
            showTransactionDialog(transactions.get(modelRow));
        });
        deleteButton.addActionListener(e -> {
            int viewRow = transactionsTable.getSelectedRow();
            if (viewRow < 0) {
                showMessage("Please select a transaction to delete.");
                return;
            }
            int modelRow = transactionsTable.convertRowIndexToModel(viewRow);
            int answer = JOptionPane.showConfirmDialog(mainFrame,
                    "Delete this transaction?", "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                transactions.remove(modelRow);
                refreshAllViews();
                saveDataInBackground();
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(BACKGROUND);
        actions.add(editButton);
        actions.add(deleteButton);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(BACKGROUND);
        center.add(controls, BorderLayout.NORTH);
        center.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

        page.add(header, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.add(actions, BorderLayout.SOUTH);
        return page;
    }

    // ---------------- BUDGET PAGE ----------------
    private static JPanel createBudgetPage() {
        JPanel page = pagePanel();
        page.add(heading("Monthly Budget"), BorderLayout.NORTH);

        JPanel content = whiteCard();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel intro = new JLabel("Set a different budget for each month.");
        intro.setForeground(GRAY);
        JComboBox<String> monthSelector = new JComboBox<>(monthChoices());
        monthSelector.setSelectedItem(currentMonth());
        JTextField amountField = new JTextField(12);
        JButton saveBudget = primaryButton("Save Budget");

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(WHITE);
        row.add(new JLabel("Month:"));
        row.add(monthSelector);
        row.add(new JLabel("Budget amount (₹):"));
        row.add(amountField);
        row.add(saveBudget);

        JLabel budgetInfo = new JLabel("Choose a month to view its budget.");
        budgetInfo.setFont(new Font("Arial", Font.BOLD, 15));
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        JLabel spendingInfo = new JLabel();

        Runnable showSelectedMonth = () -> {
            String month = (String) monthSelector.getSelectedItem();
            double budget = monthlyBudgets.getOrDefault(month, 0.0);
            double spent = expensesForMonth(month);
            amountField.setText(budget == 0 ? "" : String.format("%.2f", budget));
            budgetInfo.setText("Budget for " + month + ": " + money(budget));
            spendingInfo.setText("Expenses: " + money(spent) + " | Remaining: " + money(budget-spent));
            int percent = budget > 0 ? (int)Math.min(100, (spent / budget) * 100) : 0;
            progress.setValue(percent);
            progress.setString(budget > 0 ? percent + "% used" : "No budget set");
        };
        monthSelector.addActionListener(e -> showSelectedMonth.run());
        saveBudget.addActionListener(e -> {
            String month = (String) monthSelector.getSelectedItem();
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (amount <= 0) throw new NumberFormatException();
                monthlyBudgets.put(month, amount);
                showSelectedMonth.run();
                refreshAllViews();
                saveDataInBackground();
                showBudgetAlert(month);
                showMessage("Budget saved for " + month + ".");
            } catch (NumberFormatException ex) {
                showMessage("Enter a valid budget greater than zero.");
            }
        });

        content.add(intro);
        content.add(Box.createVerticalStrut(18));
        content.add(row);
        content.add(Box.createVerticalStrut(20));
        content.add(budgetInfo);
        content.add(Box.createVerticalStrut(10));
        content.add(progress);
        content.add(Box.createVerticalStrut(10));
        content.add(spendingInfo);
        content.add(Box.createVerticalStrut(15));
        showSelectedMonth.run();

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    // ---------------- ANALYTICS PAGE ----------------
    private static JPanel createAnalyticsPage() {
        JPanel page = pagePanel();
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        header.add(heading("Analytics"), BorderLayout.WEST);
        monthBox = new JComboBox<>(monthChoices());
        monthBox.setSelectedItem(currentMonth());
        header.add(monthBox, BorderLayout.EAST);

        analyticsSummaryLabel = new JLabel();
        analyticsSummaryLabel.setFont(new Font("Arial", Font.BOLD, 16));
        analyticsSummaryLabel.setForeground(NAVY);

        analyticsChartPanel = new CategoryChartPanel();
        analyticsChartPanel.setBackground(WHITE);
        analyticsChartPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        monthBox.addActionListener(e -> refreshAnalytics());

        page.add(header, BorderLayout.NORTH);
        page.add(analyticsSummaryLabel, BorderLayout.SOUTH);
        page.add(analyticsChartPanel, BorderLayout.CENTER);
        return page;
    }

    // Simple custom-painted category bar chart (no external library needed).
    private static class CategoryChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String month = monthBox == null ? currentMonth() : (String) monthBox.getSelectedItem();
            HashMap<String, Double> totals = categoryTotals(month);
            double max = 0;
            for (double value : totals.values()) max = Math.max(max, value);
            int left = 130, top = 35, chartWidth = Math.max(100, getWidth() - left - 60);
            int rowHeight = 38;
            g2.setColor(TEXT);
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            int index = 0;
            for (String category : CATEGORIES) {
                double value = totals.getOrDefault(category, 0.0);
                int y = top + index * rowHeight;
                g2.drawString(category, 20, y + 18);
                int barWidth = max > 0 ? (int)((value / max) * chartWidth) : 0;
                g2.setColor(new Color(90, 150, 220));
                g2.fillRoundRect(left, y, barWidth, 22, 10, 10);
                g2.setColor(TEXT);
                g2.drawString(money(value), left + barWidth + 8, y + 17);
                index++;
            }
            g2.dispose();
        }
    }

    // ---------------- REPORTS PAGE ----------------
    private static JPanel createReportsPage() {
        JPanel page = pagePanel();
        page.add(heading("Reports"), BorderLayout.NORTH);
        JPanel content = whiteCard();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel info = new JLabel("Export your transaction records to a CSV report.");
        info.setForeground(GRAY);
        JButton export = primaryButton("Export Transactions Report");
        export.addActionListener(e -> exportReport());
        content.add(info);
        content.add(Box.createVerticalStrut(15));
        content.add(export);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    // ---------------- SETTINGS PAGE ----------------
    private static JPanel createSettingsPage() {
        JPanel page = pagePanel();
        page.add(heading("Settings"), BorderLayout.NORTH);
        JPanel content = whiteCard();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel dataLocation = new JLabel("Data is saved in: " + new File(DATA_FILE).getAbsolutePath());
        dataLocation.setForeground(GRAY);
        JButton saveNow = new JButton("Save Data Now");
        saveNow.addActionListener(e -> saveDataInBackground());
        JButton reset = new JButton("Reset All Data");
        reset.setForeground(RED);
        reset.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(mainFrame,
                    "This will permanently clear all transactions and budgets. Continue?",
                    "Reset Data", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                transactions.clear();
                monthlyBudgets.clear();
                refreshAllViews();
                saveDataInBackground();
            }
        });
        content.add(dataLocation);
        content.add(Box.createVerticalStrut(15));
        content.add(saveNow);
        content.add(Box.createVerticalStrut(10));
        content.add(reset);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    // ---------------- TRANSACTION FORM ----------------
    // Passing null means Add; passing a transaction means Edit.
    private static void showTransactionDialog(Transaction existing) {
        boolean editing = existing != null;
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Expense", "Income"});
        JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);
        JTextField amountField = new JTextField(editing ? String.valueOf(existing.amount) : "");
        JTextField descriptionField = new JTextField(editing ? existing.description : "");
        JTextField dateField = new JTextField(editing ? existing.date : LocalDate.now().toString());

        if (editing) {
            typeBox.setSelectedItem(existing.type);
            categoryBox.setSelectedItem(existing.category);
        }

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
        form.add(new JLabel("Transaction Type:")); form.add(typeBox);
        form.add(new JLabel("Amount:")); form.add(amountField);
        form.add(new JLabel("Category:")); form.add(categoryBox);
        form.add(new JLabel("Description:")); form.add(descriptionField);
        form.add(new JLabel("Date (YYYY-MM-DD):")); form.add(dateField);

        int result = JOptionPane.showConfirmDialog(mainFrame, form,
                editing ? "Edit Transaction" : "Add Transaction",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showMessage("Amount must be greater than zero.");
                return;
            }
            String date = dateField.getText().trim();
            LocalDate.parse(date); // Validates date format and date itself.

            String type = (String) typeBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();
            String description = descriptionField.getText().trim();

            if (editing) {
                existing.type = type;
                existing.amount = amount;
                existing.category = category;
                existing.description = description;
                existing.date = date;
            } else {
                transactions.add(new Transaction(type, amount, category, description, date));
            }

            refreshAllViews();
            saveDataInBackground();
            if (type.equals("Expense")) showBudgetAlert(date.substring(0, 7));
            showMessage(editing ? "Transaction updated!" : "Transaction added!");
        } catch (NumberFormatException ex) {
            showMessage("Please enter a valid amount.");
        } catch (DateTimeParseException ex) {
            showMessage("Enter a valid date in YYYY-MM-DD format.");
        }
    }

    // ---------------- VIEW REFRESH ----------------
    private static void refreshAllViews() {
        refreshDashboard();
        refreshTransactionsTable();
        refreshAnalytics();
    }

    private static void refreshDashboard() {
        double income = totalIncome(currentMonth());
        double expense = totalExpenses(currentMonth());
        if (incomeLabel != null) incomeLabel.setText(money(income));
        if (expenseLabel != null) expenseLabel.setText(money(expense));
        if (balanceLabel != null) balanceLabel.setText(money(income - expense));

        if (dashboardTableModel != null) {
            dashboardTableModel.setRowCount(0);
            ArrayList<Transaction> sorted = new ArrayList<>(transactions);
            sorted.sort((a, b) -> b.date.compareTo(a.date));
            for (int i = 0; i < Math.min(8, sorted.size()); i++) {
                Transaction t = sorted.get(i);
                dashboardTableModel.addRow(new Object[]{
                    t.date, t.category, t.description, t.type, money(t.amount)
                });
            }
        }

        String month = currentMonth();
        double budget = monthlyBudgets.getOrDefault(month, 0.0);
        double spent = totalExpenses(month);
        if (budgetLabel != null) budgetLabel.setText("Budget: " + money(budget));
        if (budgetProgressLabel != null) budgetProgressLabel.setText("Spent: " + money(spent) + " / " + money(budget));
        if (budgetProgressBar != null) {
            int percent = budget > 0 ? (int)Math.min(100, Math.round(spent / budget * 100)) : 0;
            budgetProgressBar.setValue(percent);
            budgetProgressBar.setString(budget > 0 ? percent + "% used" : "Budget not set");
            budgetProgressBar.setForeground(spent > budget ? RED : GREEN);
        }
    }

    private static void refreshTransactionsTable() {
        if (transactionsTableModel == null) return;
        transactionsTableModel.setRowCount(0);
        // Keep the table rows in the same order as the ArrayList.
        // This makes the selected model row map to the correct transaction.
        for (Transaction t : transactions) {
            transactionsTableModel.addRow(new Object[]{
                t.date, t.category, t.description, t.type, money(t.amount)
            });
        }
    }

    private static void refreshAnalytics() {
        if (analyticsChartPanel == null || analyticsSummaryLabel == null) return;
        String month = monthBox == null ? currentMonth() : (String) monthBox.getSelectedItem();
        analyticsSummaryLabel.setText("Income: " + money(totalIncome(month))
                + "     Expenses: " + money(totalExpenses(month))
                + "     Balance: " + money(totalIncome(month) - totalExpenses(month)));
        analyticsChartPanel.repaint();
    }

    // ---------------- CALCULATIONS ----------------
    private static String currentMonth() {
        return LocalDate.now().toString().substring(0, 7);
    }

    // Creates month choices for a wide range of years so users can select
    // historical records (including the 1990s) or future months for planning.
    // The range is intentionally broad and can be expanded if needed.
    private static String[] monthChoices() {
        final int startingYear = 1900;
        final int endingYear = 2100;
        ArrayList<String> months = new ArrayList<>();

        // Add all 12 months for every year in the supported range.
        for (int year = startingYear; year <= endingYear; year++) {
            for (int month = 1; month <= 12; month++) {
                months.add(String.format("%04d-%02d", year, month));
            }
        }

        return months.toArray(new String[0]);
    }

    private static double totalIncome(String month) {
        double total = 0;
        for (Transaction t : transactions) if (t.type.equals("Income") && t.date.startsWith(month)) total += t.amount;
        return total;
    }

    private static double totalExpenses(String month) {
        double total = 0;
        for (Transaction t : transactions) if (t.type.equals("Expense") && t.date.startsWith(month)) total += t.amount;
        return total;
    }

    private static double expensesForMonth(String month) {
        return totalExpenses(month);
    }

    private static HashMap<String, Double> categoryTotals(String month) {
        HashMap<String, Double> totals = new HashMap<>();
        for (String category : CATEGORIES) totals.put(category, 0.0);
        for (Transaction t : transactions) {
            if (t.type.equals("Expense") && t.date.startsWith(month)) {
                totals.put(t.category, totals.getOrDefault(t.category, 0.0) + t.amount);
            }
        }
        return totals;
    }

    private static String money(double amount) {
        return String.format("₹%.2f", amount);
    }

    // Shows a warning when spending reaches or exceeds a monthly budget.
    private static void showBudgetAlert(String month) {
        double budget = monthlyBudgets.getOrDefault(month, 0.0);
        if (budget <= 0) return;
        double spent = totalExpenses(month);
        if (spent >= budget) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Budget exceeded!\nBudget: " + money(budget)
                    + "\nExpenses: " + money(spent),
                    "Budget Alert", JOptionPane.WARNING_MESSAGE);
        } else if (spent >= budget * 0.8) {
            JOptionPane.showMessageDialog(mainFrame,
                    "You have used 80% or more of your budget.\n"
                    + "Budget: " + money(budget) + "\nExpenses: " + money(spent),
                    "Budget Alert", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ---------------- FILE STORAGE / MULTITHREADING ----------------
    // Background worker avoids freezing the GUI while saving/loading.
    private static void saveDataInBackground() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                saveData();
                return null;
            }
            @Override
            protected void done() {
                // Keep feedback quiet for automatic saves.
            }
        }.execute();
    }

    private static void saveData() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            writer.println("TYPE,AMOUNT,CATEGORY,DESCRIPTION,DATE");
            for (Transaction t : transactions) {
                writer.println("TRANSACTION," + t.amount + "," + csv(t.category)
                        + "," + csv(t.description) + "," + t.type + "," + t.date);
            }
            for (Map.Entry<String, Double> entry : monthlyBudgets.entrySet()) {
                writer.println("BUDGET," + entry.getValue() + ",,,," + entry.getKey());
            }
        }
    }

    // Load saved records in a background worker; then update Swing on the EDT.
    private static void loadDataInBackground() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadData();
                return null;
            }
            @Override
            protected void done() {
                if (mainFrame != null) refreshAllViews();
            }
        }.execute();
    }

    private static void loadData() throws IOException {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header.
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsv(line);
                if (parts.length >= 6 && parts[0].equals("TRANSACTION")) {
                    try {
                        transactions.add(new Transaction(parts[4], Double.parseDouble(parts[1]),
                                parts[2], parts[3], parts[5]));
                    } catch (Exception ignored) { }
                } else if (parts.length >= 6 && parts[0].equals("BUDGET")) {
                    try {
                        monthlyBudgets.put(parts[5], Double.parseDouble(parts[1]));
                    } catch (Exception ignored) { }
                }
            }
        }
    }

    // CSV helpers support commas and quote characters in text fields.
    private static String csv(String value) {
        if (value == null) value = "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String[] parseCsv(String line) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    current.append('\"'); i++;
                } else quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                values.add(current.toString()); current.setLength(0);
            } else current.append(ch);
        }
        values.add(current.toString());
        return values.toArray(new String[0]);
    }

    // Export a separate CSV report using a file chooser.
    private static void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save SmartSpend Report");
        chooser.setSelectedFile(new File("SmartSpendReport.csv"));
        if (chooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("Date,Type,Category,Description,Amount");
                    for (Transaction t : transactions) {
                        writer.println(csv(t.date) + "," + csv(t.type) + "," + csv(t.category)
                                + "," + csv(t.description) + "," + t.amount);
                    }
                }
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    showMessage("Report exported successfully:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    showMessage("Could not export report: " + ex.getMessage());
                }
            }
        }.execute();
    }

    // ---------------- SMALL UI HELPERS ----------------
    private static JPanel pagePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        return panel;
    }

    private static JPanel whiteCard() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    private static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 25));
        label.setForeground(NAVY);
        return label;
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(GREEN);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private static DefaultTableModel nonEditableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private static void showMessage(String message) {
        JOptionPane.showMessageDialog(mainFrame, message);
    }

    // Escapes text so a user's search is treated literally, not as regex.
    private static String PatternQuote(String text) {
        return java.util.regex.Pattern.quote(text);
    }

    // Small helper for text-field change events.
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable action;
        SimpleDocumentListener(Runnable action) { this.action = action; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { action.run(); }
    }
}
