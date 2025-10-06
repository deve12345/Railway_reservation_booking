
import java.sql.*;
import java.util.Scanner;

public class RailwayReservation {
        //Mysql connection
	   private static Connection getConnection() throws Exception {
	        String url = "jdbc:mysql://localhost:3306/railway"; // database name
	        String user = "root"; // MySQL username
	        String password = "root"; // MySQL password
          //JDBC Driver add 
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        return DriverManager.getConnection(url, user, password);
	    }

	    
        //connection DB
	   public static void main(String[] args) {
	        Scanner sc = new Scanner(System.in);

	        try (Connection con = getConnection()) {
	            System.out.println("✅ Connected to Railway DB!");
            //print 
	            int choice = 0;
	            while (choice != 5) {
	                System.out.println("\nChoose Operation:");
	                System.out.println("1. Book Ticket");
	                System.out.println("2. View All Bookings");
	                System.out.println("3. Update Booking (Seat Count)");
	                System.out.println("4. Cancel Ticket");
	                System.out.println("5. Exit");
	                System.out.print("Enter choice: ");
	                choice = Integer.parseInt(sc.nextLine());

	                
	                switch (choice) {
	                    case 1: // BOOK TICKET
	                        System.out.print("Enter Passenger Name: ");
	                        String pname = sc.nextLine();
	                        System.out.print("Enter Age: ");
	                        int age = Integer.parseInt(sc.nextLine());
	                        System.out.print("Enter Gender: ");
	                        String gender = sc.nextLine();

	                        // Insert passenger
	                        String insertPassenger = "INSERT INTO passengers (name, age, gender) VALUES (?, ?, ?)";
	                        int passengerId = -1;
	                        try (PreparedStatement pstmt = con.prepareStatement(insertPassenger, Statement.RETURN_GENERATED_KEYS)) {
	                          pstmt.setString(1, pname);
	                            pstmt.setInt(2, age);
	                            pstmt.setString(3, gender);
	                            pstmt.executeUpdate();

	                            ResultSet keys = pstmt.getGeneratedKeys();
	                            if (keys.next()) {
	                                passengerId = keys.getInt(1);
	                            }
	                        }

	                        // Show trains
	                        System.out.println("\nAvailable Trains:");
	                        String selectTrains = "SELECT * FROM trains WHERE seats_available > 0";
	                        try (Statement stmt = con.createStatement();
	                             ResultSet rs = stmt.executeQuery(selectTrains)) {
	                            while (rs.next()) {
	                                System.out.println(rs.getInt("train_id") + " | " +
	                                        rs.getString("train_name") + " | " +
	                                        rs.getString("source") + " -> " +
	                                        rs.getString("destination") + " | Seats: " +
	                                        rs.getInt("seats_available"));
	                            }
	                        }

	                        System.out.print("Enter Train ID to book: ");
	                        int trainId = Integer.parseInt(sc.nextLine());
	                        System.out.print("Enter Seat Count: ");
	                        int seatCount = Integer.parseInt(sc.nextLine());

	                        // Insert booking
	                        String insertBooking = "INSERT INTO bookings (passenger_id, train_id, seat_count, status) VALUES (?, ?, ?, 'CONFIRMED')";
	                        try (PreparedStatement pstmt = con.prepareStatement(insertBooking)) {
	                            pstmt.setInt(1, passengerId);
	                            pstmt.setInt(2, trainId);
	                            pstmt.setInt(3, seatCount);
	                            pstmt.executeUpdate();
	                       }

	                        // Update train seats
	                        String updateSeats = "UPDATE trains SET seats_available = seats_available - ? WHERE train_id=?";
	                        try (PreparedStatement pstmt = con.prepareStatement(updateSeats)) {
	                            pstmt.setInt(1, seatCount);
	                            pstmt.setInt(2, trainId);
	                            pstmt.executeUpdate();
	                        }

	                        System.out.println("🎟 Ticket booked successfully!");
	                        break;

	                    case 2: // VIEW BOOKINGS
	                        String selectBookings = "SELECT b.booking_id, p.name, t.train_name, t.source, t.destination, b.seat_count, b.status " +
	                                "FROM bookings b JOIN passengers p ON b.passenger_id = p.passenger_id " +
	                                "JOIN trains t ON b.train_id = t.train_id";
	                        try (Statement stmt = con.createStatement();
	                             ResultSet rs = stmt.executeQuery(selectBookings)) {
	                             System.out.println("\n--- All Bookings ---");
	                           while (rs.next()) {
	                             System.out.println("Booking ID: " + rs.getInt("booking_id") +
	                                        " | Passenger: " + rs.getString("name") +
	                                        " | Train: " + rs.getString("train_name") +
	                                        " (" + rs.getString("source") + "->" + rs.getString("destination") + ")" +
	                                        " | Seats: " + rs.getInt("seat_count") +
	                                        " | Status: " + rs.getString("status"));
	                            }
	                        }
	                        break;

	                    case 3: // UPDATE SEAT COUNT
	                        System.out.print("Enter Booking ID to update: ");
	                        int bid = Integer.parseInt(sc.nextLine());
	                        System.out.print("Enter New Seat Count: ");
	                        int newSeats = Integer.parseInt(sc.nextLine());

	                        String updateBooking = "UPDATE bookings SET seat_count=? WHERE booking_id=?";
	                        try (PreparedStatement pstmt = con.prepareStatement(updateBooking)) {
	                            pstmt.setInt(1, newSeats);
	                            pstmt.setInt(2, bid);
	                            int rows = pstmt.executeUpdate();
	                            System.out.println(rows + " booking(s) updated.");
	                        }
	                        break;

	   
	                    case 4: // CANCEL TICKET
	                        System.out.print("Enter Booking ID to cancel: ");
	                        int cancelId = Integer.parseInt(sc.nextLine());

	                        String deleteBooking = "DELETE FROM bookings WHERE booking_id=?";
	                        try (PreparedStatement pstmt = con.prepareStatement(deleteBooking)) {
	                            pstmt.setInt(1, cancelId);
	                            int rows = pstmt.executeUpdate();
	                            System.out.println(rows + " booking(s) cancelled.");
	                        }
	                        break;

	                    case 5:
	                        System.out.println("Exiting program...");
	                        break;

	                    default:
	                        System.out.println("❌ Invalid choice. Try again.");
	                }
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            sc.close();
	            System.out.println("\n✅ Program finished. Connection closed.");
	        }
	    }
	}

