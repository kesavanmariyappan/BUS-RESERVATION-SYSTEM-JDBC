package BUS;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

class User {
    private int userId;
    private String userName;
    private int busNumber; // New attribute to store bus number
    private int reservedSeats; // New attribute to store reserved seats

    public User(int userId, String userName, int busNumber, int reservedSeats) {
        this.userId = userId;
        this.userName = userName;
        this.busNumber = busNumber;
        this.reservedSeats = reservedSeats;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public int getBusNumber() {
        return busNumber;
    }

    public int getReservedSeats() {
        return reservedSeats;
    }

    public void setBusNumber(int busNumber) {
        this.busNumber = busNumber;
    }

    public void setReservedSeats(int reservedSeats) {
        this.reservedSeats = reservedSeats;
    }
}

class Bus {
    private int busNumber;
    private int totalSeats;
    private int availableSeats;

    public Bus(int busNumber, int totalSeats) {
        this.busNumber = busNumber;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    public int getBusNumber() {
        return busNumber;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public boolean reserveSeats(int seats) {
        if (seats > 0 && seats <= availableSeats) {
            availableSeats -= seats;
            updateDatabase(); // Update available seats in the database
            return true;
        }
        return false;
    }

    private void updateDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE buses SET available_seats = ? WHERE bus_number = ?")) {

            preparedStatement.setInt(1, availableSeats);
            preparedStatement.setInt(2, busNumber);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayBusInfo() {
        System.out.println("Bus Number: " + busNumber);
        System.out.println("Total Seats: " + totalSeats);
        System.out.println("Available Seats: " + availableSeats);
    }

    public void saveToDatabase() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO buses (total_seats, available_seats) VALUES (?, ?)")) {

            preparedStatement.setInt(1, totalSeats);
            preparedStatement.setInt(2, availableSeats);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class BusReservationSystem {
    private int reservationCounter;

    public BusReservationSystem() {
        this.reservationCounter = 1;
        createUsersTableIfNotExists();
        createBusesTableIfNotExists();
        createReservationsTableIfNotExists();
    }

    private void createUsersTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS users (" +
                             "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                             "user_name VARCHAR(255) NOT NULL," +
                             "bus_number INT," +
                             "reserved_seats INT" +
                             ")")) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createBusesTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS buses (" +
                             "bus_number INT AUTO_INCREMENT PRIMARY KEY," +
                             "total_seats INT," +
                             "available_seats INT" +
                             ")")) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createReservationsTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS reservations (" +
                             "reservation_number INT AUTO_INCREMENT PRIMARY KEY," +
                             "bus_number INT," +
                             "reserved_seats INT," +
                             "user_id INT," +
                             "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                             ")")) {

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBus(int totalSeats) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123")) {
            // Check if the number of buses is less than 3
            PreparedStatement countBusesStatement = connection.prepareStatement(
                    "SELECT COUNT(*) AS num_buses FROM buses");
            ResultSet countBusesResult = countBusesStatement.executeQuery();
            if (countBusesResult.next()) {
                int numBuses = countBusesResult.getInt("num_buses");
                if (numBuses >= 3) {
                    return;
                }
            }

            // Proceed with adding the bus
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO buses (total_seats, available_seats) VALUES (?, ?)");
            preparedStatement.setInt(1, totalSeats);
            preparedStatement.setInt(2, totalSeats);
            preparedStatement.executeUpdate();

            System.out.println("Bus added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayAvailableBuses() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM buses")) {

            System.out.println("Available Buses:");
            while (resultSet.next()) {
                int busNumber = resultSet.getInt("bus_number");
                int totalSeats = resultSet.getInt("total_seats");
                int availableSeats = resultSet.getInt("available_seats");

                Bus loadedBus = new Bus(busNumber, totalSeats);
                loadedBus.reserveSeats(totalSeats - availableSeats); // Update available seats
                loadedBus.displayBusInfo();
                System.out.println("------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUserDetails() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter User Name:");
        String userName = scanner.nextLine();

        return new User(0, userName, 0, 0); // User ID, bus number, and reserved seats will be auto-generated by the database
    }

    public int saveUserToDatabase(User user) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO users (user_name, bus_number, reserved_seats) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setInt(2, user.getBusNumber());
            preparedStatement.setInt(3, user.getReservedSeats());

            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user = new User(generatedKeys.getInt(1), user.getUserName(), user.getBusNumber(), user.getReservedSeats());
                    return user.getUserId();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void makeReservation(int busNumber, int seats, User user) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123")) {
            // Check if there are enough available seats
            PreparedStatement checkAvailabilityStatement = connection.prepareStatement(
                    "SELECT available_seats FROM buses WHERE bus_number = ?");
            checkAvailabilityStatement.setInt(1, busNumber);
            ResultSet availabilityResult = checkAvailabilityStatement.executeQuery();

            if (availabilityResult.next()) {
                int availableSeats = availabilityResult.getInt("available_seats");
                if (seats > availableSeats) {
                    System.out.println("Not enough seats available for reservation.");
                    return;
                }
            } else {
                System.out.println("Invalid bus number.");
                return;
            }

            // Get the current maximum reservation number
            PreparedStatement getMaxReservationNumberStatement = connection.prepareStatement(
                    "SELECT MAX(reservation_number) AS max_reservation_number FROM reservations");
            ResultSet maxReservationResult = getMaxReservationNumberStatement.executeQuery();
            int maxReservationNumber = 0;
            if (maxReservationResult.next()) {
                maxReservationNumber = maxReservationResult.getInt("max_reservation_number");
            }

            // Increment the reservationCounter based on the current maximum reservation number
            reservationCounter = Math.max(reservationCounter, maxReservationNumber + 1);

            // Proceed with the reservation
            PreparedStatement reservationStatement = connection.prepareStatement(
                    "INSERT INTO reservations (reservation_number, bus_number, reserved_seats, user_id) VALUES (?, ?, ?, ?)");
            reservationStatement.setInt(1, reservationCounter);
            reservationStatement.setInt(2, busNumber);
            reservationStatement.setInt(3, seats);
            reservationStatement.setInt(4, user.getUserId());
            reservationStatement.executeUpdate();

            // Update user's reserved seats and bus number
            user.setBusNumber(busNumber);
            user.setReservedSeats(user.getReservedSeats() + seats);

            // Update available seats in the buses table
            PreparedStatement updateSeatsStatement = connection.prepareStatement(
                    "UPDATE buses SET available_seats = available_seats - ? WHERE bus_number = ?");
            updateSeatsStatement.setInt(1, seats);
            updateSeatsStatement.setInt(2, busNumber);
            updateSeatsStatement.executeUpdate();

            // Display reservation details
            System.out.println("Reservation Details:");
            System.out.println("Reservation Number: " + reservationCounter);
            System.out.println("Bus Number: " + busNumber);
            System.out.println("Reserved Seats: " + seats);
            System.out.println("User ID: " + user.getUserId());
            System.out.println("------------");

            reservationCounter++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void updateUserDataInDatabase(User user) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bus", "root", "KESAVAN?123");
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "UPDATE users SET bus_number = ?, reserved_seats = ? WHERE user_id = ?")) {

            preparedStatement.setInt(1, user.getBusNumber());
            preparedStatement.setInt(2, user.getReservedSeats());
            preparedStatement.setInt(3, user.getUserId());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        BusReservationSystem reservationSystem = new BusReservationSystem();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1. Display Available Buses");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
			        // Adding buses
			        reservationSystem.addBus(20);
			        reservationSystem.addBus(30);
			        reservationSystem.addBus(25);
			
			        // Displaying available buses
			        reservationSystem.displayAvailableBuses();
			
			        // Getting user details
			        User user = reservationSystem.getUserDetails();
			
			        // Saving user to the database and getting user ID
			        int userId = reservationSystem.saveUserToDatabase(user);
			        System.out.println("User ID: " + userId);
			
			        // Making reservations
			        System.out.println("Enter Bus Number for Reservation:");
			        int busNumber = scanner.nextInt();
			        System.out.println("Enter Number of Seats to Reserve:");
			        int seats = scanner.nextInt();
			
			        reservationSystem.makeReservation(busNumber, seats, new User(userId, user.getUserName(), 0, 0));
			
			        // Displaying available buses after reservations
			        reservationSystem.displayAvailableBuses();
			        break;
			        
                case 2:
                    System.out.println("Exiting the program. Goodbye!");
                    System.exit(0);
                    break;
            }
        }
    }
}


