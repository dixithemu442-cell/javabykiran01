 package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.Account;
import util.DBConnection;

public class AccountDAO {

    public boolean createAccount(Account account, String passwordHash) {

        String sql = "INSERT INTO accounts "
                + "(account_holder_name, aadhaar_number, mobile_number, password_hash, balance) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, account.getAccountHolderName());
            ps.setString(2, account.getAadhaarNumber());
            ps.setString(3, account.getMobileNumber());
            ps.setString(4, passwordHash);
            ps.setDouble(5, account.getBalance());

            int rows = ps.executeUpdate();

            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Account creation failed: " + e.getMessage());
            return false;
        }
    }
}
