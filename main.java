import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class Expense {
    String category;
    double amount;

    public Expense(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    public Object[] toRow(Double budget) {
        return new Object[]{category, amount, budget != null ? budget : "N/A"};
    }
}

public class ExpenseTrackerGUI {
    private JFrame frame, loginFrame;
    private JTextField categoryField, amountField, budgetCategoryField, budgetAmountField;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;
    private java.util.List<Expense> expenses;
    private Map<String, Double> budgets;
    private PieChartPanel chartPanel;
    private static final String FILE_NAME = "expenses.txt";

    public ExpenseTrackerGUI() {
        loadExpenses();
        showLoginPage();
    }

    private void showLoginPage() {
        loginFrame = new JFrame("Login");
        loginFrame.setSize(400, 200);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(3, 2, 5, 5));
        
        loginFrame.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        loginFrame.add(usernameField);
        
        loginFrame.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        loginFrame.add(passwordField);
        
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            
            if ("admin".equals(username) && "password".equals(password)) {
                loginFrame.dispose();
                showMainApp();
            } else {
                showMessage("Invalid username or password!");
            }
        });
        loginFrame.add(loginButton);
        
        loginFrame.setVisible(true);
    }

    private void showMainApp() {
        expenses = new ArrayList<>();
        budgets = new HashMap<>();

        frame = new JFrame("Expense Tracker");
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new GridLayout(1, 1, 5, 5));
        JPanel budgetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        budgetPanel.setBorder(BorderFactory.createTitledBorder("Set Budget"));

        budgetCategoryField = new JTextField(10);
        budgetAmountField = new JTextField(10);
        JButton setBudgetButton = new JButton("Set Budget");
        setBudgetButton.addActionListener(e -> setBudget());

        budgetPanel.add(new JLabel("Category:"));
        budgetPanel.add(budgetCategoryField);
        budgetPanel.add(new JLabel("Budget Amount:"));
        budgetPanel.add(budgetAmountField);
        budgetPanel.add(setBudgetButton);

        JPanel expensePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expensePanel.setBorder(BorderFactory.createTitledBorder("Add Expense"));

        categoryField = new JTextField(10);
        amountField = new JTextField(10);
        JButton addButton = new JButton("Add Expense");
        addButton.addActionListener(e -> addExpense());

        expensePanel.add(new JLabel("Category:"));
        expensePanel.add(categoryField);
        expensePanel.add(new JLabel("Amount:"));
        expensePanel.add(amountField);
        expensePanel.add(addButton);

        topPanel.add(budgetPanel);
        topPanel.add(expensePanel);

        String[] columns = {"Category", "Amount", "Budget"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScrollPane = new JScrollPane(table);

        JButton deleteButton = new JButton("Delete Expense");
        deleteButton.addActionListener(e -> deleteExpense(table));

        totalLabel = new JLabel("Total Expense: ₹0.00", SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel chartContainer = new JPanel(new BorderLayout());
        chartPanel = new PieChartPanel(expenses);
        chartPanel.setPreferredSize(new Dimension(500, 500));
        chartContainer.add(new JLabel("Overall Expense", SwingConstants.CENTER), BorderLayout.NORTH);
        chartContainer.add(chartPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(deleteButton, BorderLayout.NORTH);
        bottomPanel.add(totalLabel, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(tableScrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(chartContainer, BorderLayout.EAST);

        frame.setVisible(true);
    }

    private void setBudget() {
        try {
            String category = budgetCategoryField.getText().trim();
            double budget = Double.parseDouble(budgetAmountField.getText().trim());
            if (budget <= 0) throw new NumberFormatException();
            budgets.put(category, budget);
            showMessage("Budget set for " + category + ": ₹" + budget);
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid positive amount.");
        }
    }

    private void addExpense() {
        try {
            String category = categoryField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());
            
            expenses.add(new Expense(category, amount));
            tableModel.addRow(new Expense(category, amount).toRow(budgets.get(category)));
            if (budgets.containsKey(category) && amount > budgets.get(category)) {
                showMessage("Warning: Expense exceeds budget for " + category + "!");
            }
            updateTotal();
            saveExpenses();
        } catch (NumberFormatException e) {
            showMessage("Please enter a valid amount.");
        }
    }

    private void deleteExpense(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            expenses.remove(selectedRow);
            tableModel.removeRow(selectedRow);
            updateTotal();
            saveExpenses();
        }
    }

    private void updateTotal() {
        double total = expenses.stream().mapToDouble(exp -> exp.amount).sum();
        totalLabel.setText("Total Expense: ₹" + total);
        chartPanel.repaint();
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerGUI::new);
    }
}
