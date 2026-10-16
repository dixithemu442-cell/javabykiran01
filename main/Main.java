 package main;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import util.DBConnection;

public class Main {

    // Password hashing
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed!", e);
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println("\n================================");
            System.out.println("     BANKING MANAGEMENT SYSTEM");
            System.out.println("================================");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            String choice = sc.nextLine().trim();

            // =====================================
            // 1. CREATE ACCOUNT
            // =====================================

            if (choice.equals("1")) {

                System.out.println("\n--- CREATE NEW ACCOUNT ---");

                System.out.print("Enter your name: ");
                String name = sc.nextLine().trim();

                System.out.print("Enter 12-digit Aadhaar number: ");
                String aadhaar = sc.nextLine().trim();

                System.out.print("Enter 10-digit mobile number: ");
                String mobile = sc.nextLine().trim();

                System.out.print("Create password: ");
                String password = sc.nextLine();

                if (name.isEmpty()) {
                    System.out.println("Name cannot be empty!");
                    continue;
                }

                if (!aadhaar.matches("\\d{12}")) {
                    System.out.println("Aadhaar must contain exactly 12 digits!");
                    continue;
                }

                if (!mobile.matches("\\d{10}")) {
                    System.out.println("Mobile number must contain exactly 10 digits!");
                    continue;
                }

                if (password.isEmpty()) {
                    System.out.println("Password cannot be empty!");
                    continue;
                }

                double initialBalance;

                try {
                    System.out.print("Enter initial deposit amount: ");
                    initialBalance = Double.parseDouble(sc.nextLine().trim());

                    if (!Double.isFinite(initialBalance)
                            || initialBalance < 0) {
                        System.out.println("Invalid deposit amount!");
                        continue;
                    }

                } catch (NumberFormatException e) {
                    System.out.println("Please enter a valid amount!");
                    continue;
                }

                String hashedPassword = hashPassword(password);

                String sql = "INSERT INTO accounts "
                        + "(account_holder_name, aadhaar_number, "
                        + "mobile_number, password_hash, balance) "
                        + "VALUES (?, ?, ?, ?, ?)";

                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                             sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, name);
                    ps.setString(2, aadhaar);
                    ps.setString(3, mobile);
                    ps.setString(4, hashedPassword);
                    ps.setDouble(5, initialBalance);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {

                        try (ResultSet generatedKeys =
                                     ps.getGeneratedKeys()) {

                            if (generatedKeys.next()) {

                                long accountNumber =
                                        generatedKeys.getLong(1);

                                System.out.println(
                                        "\nAccount created successfully!");
                                System.out.println(
                                        "Your Account Number: "
                                                + accountNumber);
                                System.out.println(
                                        "Please remember your account number.");
                            }
                        }
                    }

                } catch (SQLException e) {

                    if (e.getErrorCode() == 1062) {
                        System.out.println(
                                "This Aadhaar number is already registered!");
                    } else {
                        System.out.println(
                                "Account creation failed!");
                        e.printStackTrace();
                    }
                }

            }

            // =====================================
            // 2. LOGIN
            // =====================================

            else if (choice.equals("2")) {

                System.out.println("\n--- LOGIN ---");

                long accountNumber;

                try {
                    System.out.print("Enter account number: ");
                    accountNumber =
                            Long.parseLong(sc.nextLine().trim());

                } catch (NumberFormatException e) {
                    System.out.println("Invalid account number!");
                    continue;
                }

                System.out.print("Enter password: ");
                String password = sc.nextLine();

                String hashedPassword = hashPassword(password);

                String loginSql =
                        "SELECT account_number, account_holder_name "
                        + "FROM accounts "
                        + "WHERE account_number = ? "
                        + "AND password_hash = ?";

                boolean loginSuccessful = false;
                String accountHolderName = "";

                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps =
                             con.prepareStatement(loginSql)) {

                    ps.setLong(1, accountNumber);
                    ps.setString(2, hashedPassword);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {

                            loginSuccessful = true;
                            accountHolderName =
                                    rs.getString("account_holder_name");

                        } else {
                            System.out.println(
                                    "Invalid account number or password!");
                        }
                    }

                } catch (SQLException e) {
                    System.out.println("Login failed!");
                    e.printStackTrace();
                }

                if (!loginSuccessful) {
                    continue;
                }

                System.out.println(
                        "\nWelcome, " + accountHolderName + "!");
                System.out.println("Login successful!");

                boolean loggedIn = true;

                // =====================================
                // ACCOUNT MENU
                // =====================================

                while (loggedIn) {

                    System.out.println("\n-------------------------------");
                    System.out.println("       ACCOUNT MENU");
                    System.out.println("-------------------------------");
                    System.out.println("1. Check Balance");
                    System.out.println("2. Deposit Money");
                    System.out.println("3. Withdraw Money");
                    System.out.println("4. Transaction History");
                    System.out.println("5. Money Transfer");
                    System.out.println("6. Logout");
                    System.out.print("Enter your choice: ");

                    String menuChoice =
                            sc.nextLine().trim();

                    // =================================
                    // CHECK BALANCE
                    // =================================

                    if (menuChoice.equals("1")) {

                        String balanceSql =
                                "SELECT balance FROM accounts "
                                + "WHERE account_number = ?";

                        try (Connection con =
                                     DBConnection.getConnection();
                             PreparedStatement ps =
                                     con.prepareStatement(balanceSql)) {

                            ps.setLong(1, accountNumber);

                            try (ResultSet rs = ps.executeQuery()) {

                                if (rs.next()) {
                                    System.out.println(
                                            "Current Balance: Rs. "
                                            + rs.getBigDecimal("balance"));
                                } else {
                                    System.out.println(
                                            "Account not found!");
                                }
                            }

                        } catch (SQLException e) {
                            System.out.println(
                                    "Unable to fetch balance!");
                            e.printStackTrace();
                        }

                    }

                    // =================================
                    // DEPOSIT MONEY
                    // =================================

                    else if (menuChoice.equals("2")) {

                        double amount;

                        try {
                            System.out.print(
                                    "Enter deposit amount: Rs. ");

                            amount = Double.parseDouble(
                                    sc.nextLine().trim());

                            if (!Double.isFinite(amount)
                                    || amount <= 0) {
                                System.out.println(
                                        "Amount must be greater than zero!");
                                continue;
                            }

                        } catch (NumberFormatException e) {
                            System.out.println(
                                    "Please enter a valid amount!");
                            continue;
                        }

                        String depositSql =
                                "UPDATE accounts "
                                + "SET balance = balance + ? "
                                + "WHERE account_number = ?";

                        try (Connection con =
                                     DBConnection.getConnection();
                             PreparedStatement ps =
                                     con.prepareStatement(depositSql)) {

                            ps.setDouble(1, amount);
                            ps.setLong(2, accountNumber);

                            int rows = ps.executeUpdate();

                            if (rows > 0) {

                                System.out.println(
                                        "Deposit successful!");

                                showBalance(con, accountNumber);

                            } else {
                                System.out.println(
                                        "Account not found!");
                            }

                        } catch (SQLException e) {
                            System.out.println(
                                    "Deposit failed!");
                            e.printStackTrace();
                        }

                    }

                    // =================================
                    // WITHDRAW MONEY
                    // =================================

                    else if (menuChoice.equals("3")) {

                        double amount;

                        try {
                            System.out.print(
                                    "Enter withdrawal amount: Rs. ");

                            amount = Double.parseDouble(
                                    sc.nextLine().trim());

                            if (!Double.isFinite(amount)
                                    || amount <= 0) {
                                System.out.println(
                                        "Amount must be greater than zero!");
                                continue;
                            }

                        } catch (NumberFormatException e) {
                            System.out.println(
                                    "Please enter a valid amount!");
                            continue;
                        }

                        String withdrawSql =
                                "UPDATE accounts "
                                + "SET balance = balance - ? "
                                + "WHERE account_number = ? "
                                + "AND balance >= ?";

                        try (Connection con =
                                     DBConnection.getConnection();
                             PreparedStatement ps =
                                     con.prepareStatement(withdrawSql)) {

                            ps.setDouble(1, amount);
                            ps.setLong(2, accountNumber);
                            ps.setDouble(3, amount);

                            int rows = ps.executeUpdate();

                            if (rows > 0) {

                                System.out.println(
                                        "Withdrawal successful!");

                                showBalance(con, accountNumber);

                            } else {

                                System.out.println(
                                        "Withdrawal failed!");

                                System.out.println(
                                        "Insufficient balance or account not found.");
                            }

                        } catch (SQLException e) {
                            System.out.println(
                                    "Withdrawal failed!");
                            e.printStackTrace();
                        }

                    }

                    // =================================
                    // LOGOUT
                    // =================================

                 // =================================
                 // TRANSACTION HISTORY
                 // =================================

                 else if (menuChoice.equals("4")) {

                     String sql = "SELECT transaction_id, transaction_type, amount, "
                                + "trasaction_date FROM banking_db.transactions "
                                + "WHERE account_number = ? "
                                + "ORDER BY transaction_id DESC";

                     try (Connection con = DBConnection.getConnection();
                          PreparedStatement ps = con.prepareStatement(sql)) {

                         ps.setLong(1, accountNumber);

                         try (ResultSet rs = ps.executeQuery()) {

                             System.out.println("\n========== TRANSACTION HISTORY ==========");

                             System.out.printf("%-8s %-15s %-12s %-22s%n",
                                     "ID", "TYPE", "AMOUNT", "DATE");

                             boolean found = false;

                             while (rs.next()) {

                                 found = true;

                                 System.out.printf("%-8d %-15s %-12s %-22s%n",
                                         rs.getInt("transaction_id"),
                                         rs.getString("transaction_type"),
                                         rs.getBigDecimal("amount"),
                                         rs.getTimestamp("trasaction_date"));
                             }

                             if (!found) {
                                 System.out.println("No transactions found.");
                             }

                             System.out.println("=========================================");

                         }

                     } catch (SQLException e) {

                         System.out.println("Unable to fetch transaction history!");
                         e.printStackTrace();
                     }
                 }

                 // =================================
                 // LOGOUT
                 // =================================
                    
                 else if (menuChoice.equals("5")) {
                	 System.out.print("Enter receiver account number: ");

                	 long receiverAccount;

                	 try {
                	     receiverAccount = Long.parseLong(sc.nextLine().trim());

                	     if (receiverAccount == accountNumber) {
                	         System.out.println("You cannot transfer money to your own account!");
                	         continue;
                	     }

                	 } catch (NumberFormatException e) {
                	     System.out.println("Invalid account number!");
                	     continue;
                	 }

                	 System.out.print("Enter transfer amount: Rs. ");

                	 double amount;

                	 try {
                	     amount = Double.parseDouble(sc.nextLine().trim());

                	     if (!Double.isFinite(amount) || amount <= 0) {
                	         System.out.println("Amount must be greater than zero!");
                	         continue;
                	     }

                	 } catch (NumberFormatException e) {
                	     System.out.println("Invalid amount!");
                	     continue;
                	 }

                	 try (Connection con = DBConnection.getConnection()) {

                	     con.setAutoCommit(false);

                	     try {
                	         // Lock sender and receiver rows in a consistent order.
                	         long firstAccount = Math.min(accountNumber, receiverAccount);
                	         long secondAccount = Math.max(accountNumber, receiverAccount);

                	         String lockSql =
                	                 "SELECT account_number FROM accounts "
                	                 + "WHERE account_number IN (?, ?) "
                	                 + "ORDER BY account_number FOR UPDATE";

                	         int foundAccounts = 0;

                	         try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                	             ps.setLong(1, firstAccount);
                	             ps.setLong(2, secondAccount);

                	             try (ResultSet rs = ps.executeQuery()) {
                	                 while (rs.next()) {
                	                     foundAccounts++;
                	                 }
                	             }
                	         }

                	         if (foundAccounts != 2) {
                	             throw new SQLException("Receiver account not found!");
                	         }

                	         String debitSql =
                	                 "UPDATE accounts SET balance = balance - ? "
                	                 + "WHERE account_number = ? AND balance >= ?";

                	         try (PreparedStatement ps = con.prepareStatement(debitSql)) {
                	             ps.setDouble(1, amount);
                	             ps.setLong(2, accountNumber);
                	             ps.setDouble(3, amount);

                	             if (ps.executeUpdate() != 1) {
                	                 throw new SQLException("Insufficient balance!");
                	             }
                	         }

                	         String creditSql =
                	                 "UPDATE accounts SET balance = balance + ? "
                	                 + "WHERE account_number = ?";

                	         try (PreparedStatement ps = con.prepareStatement(creditSql)) {
                	             ps.setDouble(1, amount);
                	             ps.setLong(2, receiverAccount);

                	             if (ps.executeUpdate() != 1) {
                	                 throw new SQLException("Receiver account update failed!");
                	             }
                	         }

                	         // Record both sides of the transfer.
                	         String transactionSql =
                	                 "INSERT INTO transactions "
                	                 + "(account_number, transaction_type, amount, related_account_number) "
                	                 + "VALUES (?, ?, ?, ?)";

                	         try (PreparedStatement ps = con.prepareStatement(transactionSql)) {

                	             ps.setLong(1, accountNumber);
                	             ps.setString(2, "TRANSFER_SENT");
                	             ps.setDouble(3, amount);
                	             ps.setLong(4, receiverAccount);
                	             ps.executeUpdate();

                	             ps.setLong(1, receiverAccount);
                	             ps.setString(2, "TRANSFER_RECEIVED");
                	             ps.setDouble(3, amount);
                	             ps.setLong(4, accountNumber);
                	             ps.executeUpdate();
                	         }

                	         con.commit();

                	         System.out.println("Money transferred successfully!");
                	         System.out.println("Transferred amount: Rs. " + amount);
                	         showBalance(con, accountNumber);

                	     } catch (SQLException e) {
                	         con.rollback();
                	         System.out.println("Transfer failed: " + e.getMessage());
                	     } finally {
                	         con.setAutoCommit(true);
                	     }

                	 } catch (SQLException e) {
                	     System.out.println("Database error during transfer!");
                	     e.printStackTrace();
                	 }
                	 
                 }
                    
                    
                    
                    
                    

                 else if (menuChoice.equals("6")) {

                     loggedIn = false;

                     System.out.println("Logout successful!");
                     System.out.println("Thank you for using our banking system.");

                 }

                 // =================================
                 // INVALID CHOICE
                 // =================================

                 else {

                     System.out.println("Invalid choice! Please select 1-5.");
                 }
                }}

            // =====================================
            // 3. EXIT
            // =====================================

            else if (choice.equals("3")) {

                System.out.println(
                        "\nThank you for using Banking Management System.");

                break;

            }

            else {
                System.out.println(
                        "Invalid choice! Please select 1, 2 or 3.");
            }
        }

        sc.close();
    }

    // =====================================
    // FETCH AND DISPLAY UPDATED BALANCE
    // =====================================

    private static void showBalance(
            Connection con, long accountNumber) throws SQLException {

        String sql =
                "SELECT balance FROM accounts "
                + "WHERE account_number = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, accountNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    System.out.println(
                            "Updated Balance: Rs. "
                            + rs.getBigDecimal("balance"));
                }
            }
        }
    }
}