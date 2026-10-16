 package main;

import java.sql.Connection;
import java.sql.SQLException;
import util.DBConnection;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection con = DBConnection.getConnection()) {

            System.out.println("MySQL Connected Successfully!");

        } catch (SQLException e) {

            System.out.println("MySQL Connection Failed!");
            System.out.println(e.getMessage());
        }
    }
}