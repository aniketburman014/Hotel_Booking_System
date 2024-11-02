package com.main;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/hoteldb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "admin1234";

    public static void main(String[] args) {
        logger.info("Welcome to the Hotel Booking System...");
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            logger.info("Connected to DB...");
            
            BookingService bookingService = new BookingService(connection);
            Scanner scanner = new Scanner(System.in);
            
            boolean flag = true;
            while (flag) {
                showMenu();
                int option = scanner.nextInt();
                logger.info("Option selected by user: " + option);

                switch (option) {
                    case 1 -> bookingService.bookRoom(scanner);
                    case 2 -> bookingService.viewBookedRoom(scanner);
                    case 3 -> bookingService.showAllBookings();
                    case 4 -> bookingService.updateBookingDetails(scanner);
                    case 5 -> bookingService.deleteBooking(scanner);
                    case 6 -> {
                        logger.info("Thank you for using the system. Program terminated.");
                        flag = false;
                    }
                    default -> logger.warning("Invalid option. Please select an option from 1 to 6.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to DB: " + e.getMessage(), e);
        }
    }

    private static void showMenu() {
        System.out.println("\nPlease select from the following options:");
        System.out.println("1. Book a room");
        System.out.println("2. View the room");
        System.out.println("3. Show all bookings");
        System.out.println("4. Update booking details");
        System.out.println("5. Delete booking");
        System.out.println("6. Exit");
        System.out.print("Enter option: ");
    }
}

class BookingService {
    
    private final Connection connection;

    public BookingService(Connection connection) {
        this.connection = connection;
    }

    public void bookRoom(Scanner scanner) {
        String sql = "INSERT INTO booking_table (name, room_no, phone) VALUES (?, ?, ?)";
        
        System.out.print("Enter name: ");
        String name = scanner.next();
        System.out.print("Enter room number: ");
        int roomNo = scanner.nextInt();
        System.out.print("Enter phone number: ");
        String phone = scanner.next();
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, roomNo);
            ps.setString(3, phone);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Room booked successfully!");
            } else {
                System.out.println("Booking failed. Please try again.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to book a room: " + e.getMessage(), e);
        }
    }

    public void viewBookedRoom(Scanner scanner) {
        String sql = "SELECT * FROM booking_table WHERE id = ? AND name = ?";
        
        System.out.print("Enter booking ID: ");
        int id = scanner.nextInt();
        System.out.print("Enter name: ");
        String name = scanner.next();
        
        if (!bookingExists(id)) {
            System.out.println("No booking found with the provided ID.");
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    displayBooking(rs);
                } else {
                    System.out.println("No booking found with the provided ID and name.");
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve booking: " + e.getMessage(), e);
        }
    }

    public void showAllBookings() {
        String sql = "SELECT * FROM booking_table";
        
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
             
            while (rs.next()) {
                displayBooking(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to retrieve bookings: " + e.getMessage(), e);
        }
    }

    public void updateBookingDetails(Scanner scanner) {
        String sql = "UPDATE booking_table SET phone = ? WHERE id = ?";
        
        System.out.print("Enter booking ID to update: ");
        int id = scanner.nextInt();
        System.out.print("Enter new phone number: ");
        String phone = scanner.next();
        
        if (!bookingExists(id)) {
            System.out.println("No booking found with the provided ID.");
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setInt(2, id);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Booking updated successfully!" : "Update failed.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update booking: " + e.getMessage(), e);
        }
    }

    public void deleteBooking(Scanner scanner) {
        String sql = "DELETE FROM booking_table WHERE id = ?";
        
        System.out.print("Enter booking ID to delete: ");
        int id = scanner.nextInt();
        
        if (!bookingExists(id)) {
            System.out.println("No booking found with the provided ID.");
            return;
        }

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println(rowsAffected > 0 ? "Booking deleted successfully!" : "Delete failed.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to delete booking: " + e.getMessage(), e);
        }
    }

    private boolean bookingExists(int id) {
        String sql = "SELECT * FROM booking_table WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to check booking existence: " + e.getMessage(), e);
        }
        return false;
    }

    private void displayBooking(ResultSet rs) throws SQLException {
        System.out.printf("ID: %d, Name: %s, Room No: %d, Phone: %s, Booking Time: %s%n",
                rs.getInt("id"), rs.getString("name"), rs.getInt("room_no"), rs.getString("phone"), rs.getTimestamp("booking_time"));
    }
}
