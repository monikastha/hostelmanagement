package Admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AdminHostelLogManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/hostelms"; // Replace with your database URL
    private static final String DB_USER = "root"; // Replace with your database username
    private static final String DB_PASSWORD = ""; // Replace with your database password
    private static final String TABLE_NAME = "hostel_log";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void logActivity(String studentId, String activity) {
        String sql = "INSERT INTO " + TABLE_NAME + " (std_id, std_activity, created_at, updated_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalDateTime now = LocalDateTime.now();
            Timestamp timestamp = Timestamp.valueOf(now);
            pstmt.setString(1, studentId);
            pstmt.setString(2, activity);
            pstmt.setTimestamp(3, timestamp);
            pstmt.setTimestamp(4, timestamp);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Activity logged successfully for student: " + studentId + " - " + activity);
            } else {
                System.out.println("Failed to log activity for student: " + studentId + " - " + activity);
            }
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
        }
    }

    public static void printAllLogs() {
        String sql = "SELECT h_id, std_id, std_activity, created_at, updated_at FROM " + TABLE_NAME;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("--- Hostel Log ---");
            while (rs.next()) {
                int hId = rs.getInt("h_id");
                String stdId = rs.getString("std_id");
                String activity = rs.getString("std_activity");
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                System.out.println("H_ID: " + hId + ", Student ID: " + stdId + ", Activity: " + activity +
                        ", Created At: " + createdAt + ", Updated At: " + updatedAt);
            }
            System.out.println("------------------");
        } catch (SQLException e) {
            System.err.println("Error retrieving logs: " + e.getMessage());
        }
    }

    public static void getCurrentResidingStudents() {
        String sql = "SELECT DISTINCT std_id FROM " + TABLE_NAME + " WHERE std_activity LIKE '%Check-in%' AND std_id NOT IN (SELECT std_id FROM " + TABLE_NAME + " WHERE std_activity LIKE '%Check-out%')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("--- Currently Residing Students ---");
            while (rs.next()) {
                String stdId = rs.getString("std_id");
                System.out.println("Student ID: " + stdId);
            }
            System.out.println("----------------------------------");
        } catch (SQLException e) {
            System.err.println("Error retrieving residing students: " + e.getMessage());
        }
    }

    public static void getStudentsWhoLeft() {
        String sql = "SELECT DISTINCT std_id FROM " + TABLE_NAME + " WHERE std_activity LIKE '%Check-out%' AND std_id IN (SELECT std_id FROM " + TABLE_NAME + " WHERE std_activity LIKE '%Check-in%')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("--- Students Who Have Left ---");
            while (rs.next()) {
                String stdId = rs.getString("std_id");
                System.out.println("Student ID: " + stdId);
            }
            System.out.println("-----------------------------");
        } catch (SQLException e) {
            System.err.println("Error retrieving students who left: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Example Usage:
        logActivity("S1001", "Check-in to Room 205");
        logActivity("S1002", "Check-in to Room 301");
        logActivity("S1001", "Room change to Room 206");
        logActivity("S1003", "Check-in to Room 110");
        logActivity("S1002", "Check-out");
        logActivity("S1004", "Check-in to Room 305");
        logActivity("S1004", "Check-out");

        printAllLogs();
        getCurrentResidingStudents();
        getStudentsWhoLeft();
    }
}