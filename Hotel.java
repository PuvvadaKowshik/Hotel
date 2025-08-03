import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.*;

public class HotelReservationFinal extends JFrame {
    private JTextField bookingIdField, customerNameField, roomIdField, amountField;
    private JButton bookButton, viewBookingsButton;
    private JTable bookingTable;
    private DefaultTableModel tableModel;

    private final String ROOMS_FILE = "rooms.txt";
    private final String BOOKINGS_FILE = "bookings.txt";

    public HotelReservationFinal() {
        setTitle("Hotel Reservation System (File I/O)");
        setLayout(null);
        setSize(600, 500);

        JLabel bookingIdLabel = new JLabel("Booking ID:");
        bookingIdLabel.setBounds(30, 20, 120, 25);
        add(bookingIdLabel);

        bookingIdField = new JTextField();
        bookingIdField.setBounds(150, 20, 200, 25);
        add(bookingIdField);

        JLabel nameLabel = new JLabel("Customer Name:");
        nameLabel.setBounds(30, 60, 120, 25);
        add(nameLabel);

        customerNameField = new JTextField();
        customerNameField.setBounds(150, 60, 200, 25);
        add(customerNameField);

        JLabel roomIdLabel = new JLabel("Room ID:");
        roomIdLabel.setBounds(30, 100, 120, 25);
        add(roomIdLabel);

        roomIdField = new JTextField();
        roomIdField.setBounds(150, 100, 200, 25);
        add(roomIdField);

        JLabel amountLabel = new JLabel("Amount (₹):");
        amountLabel.setBounds(30, 140, 120, 25);
        add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(150, 140, 200, 25);
        add(amountField);

        bookButton = new JButton("Book Room");
        bookButton.setBounds(150, 180, 120, 30);
        add(bookButton);

        viewBookingsButton = new JButton("View Bookings");
        viewBookingsButton.setBounds(280, 180, 150, 30);
        add(viewBookingsButton);

        tableModel = new DefaultTableModel(new String[]{"Booking ID", "Customer Name", "Room ID", "Amount Paid"}, 0);
        bookingTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBounds(30, 230, 520, 200);
        add(scrollPane);

        // Actions
        bookButton.addActionListener(e -> bookRoom());
        viewBookingsButton.addActionListener(e -> viewBookings());
    }

    private void bookRoom() {
        int bookingId;
        try {
            bookingId = Integer.parseInt(bookingIdField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Booking ID.");
            return;
        }

        if (checkBookingIdExists(bookingId)) {
            JOptionPane.showMessageDialog(this, "Booking ID already exists. Please use a different ID.");
            return;
        }

        String customerName = customerNameField.getText();
        int roomId;
        double amount;

        try {
            roomId = Integer.parseInt(roomIdField.getText());
            amount = Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Room ID or Amount.");
            return;
        }

        // Payment Simulation
        JOptionPane.showMessageDialog(this, "Processing Payment of ₹" + amount + "... Payment Successful!");

        // Update Room Availability and Get Status
        String statusMessage = updateRoomAvailability(roomId);
        if (statusMessage.equals("Room booked successfully!")) {
            writeBookingToFile(bookingId, customerName, roomId, amount);
        }
        JOptionPane.showMessageDialog(this, statusMessage);
    }

    private String updateRoomAvailability(int roomId) {
        boolean found = false;
        boolean isAlreadyBooked = false;
        try {
            File inputFile = new File(ROOMS_FILE);
            File tempFile = new File("temp_rooms.txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] data = currentLine.split(",");
                int id = Integer.parseInt(data[0]);
                String type = data[1];
                boolean isAvailable = Boolean.parseBoolean(data[2]);

                if (id == roomId) {
                    found = true;
                    if (isAvailable) {
                        isAvailable = false;  // Book the room
                    } else {
                        isAlreadyBooked = true;  // Room is already booked
                    }
                }
                writer.write(id + "," + type + "," + isAvailable);
                writer.newLine();
            }
            writer.close();
            reader.close();

            // Replace original file with updated file
            if (inputFile.delete()) {
                tempFile.renameTo(inputFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!found) return "Room ID does not exist!";
        if (isAlreadyBooked) return "Room is already booked!";
        return "Room booked successfully!";
    }

    private void writeBookingToFile(int bookingId, String customerName, int roomId, double amount) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKINGS_FILE, true))) {
            writer.write(bookingId + "," + customerName + "," + roomId + "," + amount);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkBookingIdExists(int bookingId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                int existingId = Integer.parseInt(data[0]);
                if (existingId == bookingId) {
                    return true; // Duplicate Found
                }
            }
        } catch (IOException e) {
            // Ignore if file doesn't exist yet
        }
        return false;
    }

    private void viewBookings() {
        tableModel.setRowCount(0); // Clear existing rows
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKINGS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                tableModel.addRow(data);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No bookings found.");
        }
    }

    public static void main(String[] args) {
        // Initialize Room File with Sample Data (Run only first time)
        initializeRoomsFile();

        SwingUtilities.invokeLater(() -> {
            HotelReservationFinal app = new HotelReservationFinal();
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            app.setVisible(true);
        });
    }

    private static void initializeRoomsFile() {
        File file = new File("rooms.txt");
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("101,Deluxe,true\n");
                writer.write("102,Suite,true\n");
                writer.write("103,Standard,true\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
