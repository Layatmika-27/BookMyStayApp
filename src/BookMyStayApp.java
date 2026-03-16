import java.io.*;
import java.util.*;

// Reservation (Serializable)
class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String reservationId, String guestName, String roomType) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    @Override
    public String toString() {
        return reservationId + " | " + guestName + " | " + roomType;
    }
}

// Inventory (Serializable)
class RoomInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 2);
        availability.put("Suite Room", 1);
    }

    public boolean allocateRoom(String roomType) {

        int count = availability.getOrDefault(roomType, 0);

        if (count <= 0) {
            return false;
        }

        availability.put(roomType, count - 1);

        return true;
    }

    public Map<String, Integer> getAvailability() {
        return availability;
    }

    public void displayInventory() {

        System.out.println("\nCurrent Inventory:");

        for (Map.Entry<String, Integer> entry : availability.entrySet()) {
            System.out.println(entry.getKey() + " Available: " + entry.getValue());
        }
    }
}

// Booking History (Serializable)
class BookingHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Reservation> reservations = new ArrayList<>();

    public void addReservation(Reservation r) {
        reservations.add(r);
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void displayBookings() {

        System.out.println("\nBooking History:");

        for (Reservation r : reservations) {
            System.out.println(r);
        }
    }
}

// System State Wrapper
class SystemState implements Serializable {

    private static final long serialVersionUID = 1L;

    RoomInventory inventory;
    BookingHistory history;

    public SystemState(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }
}

// Persistence Service
class PersistenceService {

    private static final String FILE_NAME = "bookmystay_data.ser";

    // Save system state
    public static void save(SystemState state) {

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(state);

            System.out.println("\nSystem state saved successfully.");

        } catch (IOException e) {

            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // Load system state
    public static SystemState load() {

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            System.out.println("System state restored from file.");

            return (SystemState) in.readObject();

        } catch (FileNotFoundException e) {

            System.out.println("No previous data found. Starting fresh.");

        } catch (Exception e) {

            System.out.println("Failed to load saved data. Starting fresh.");
        }

        return null;
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        RoomInventory inventory;
        BookingHistory history;

        // Attempt recovery
        SystemState recoveredState = PersistenceService.load();

        if (recoveredState != null) {

            inventory = recoveredState.inventory;
            history = recoveredState.history;

        } else {

            inventory = new RoomInventory();
            history = new BookingHistory();
        }

        // Simulate bookings
        Reservation r1 = new Reservation("RES201", "Alice", "Single Room");
        Reservation r2 = new Reservation("RES202", "Bob", "Double Room");

        if (inventory.allocateRoom(r1.getRoomType())) {
            history.addReservation(r1);
        }

        if (inventory.allocateRoom(r2.getRoomType())) {
            history.addReservation(r2);
        }

        // Display current state
        inventory.displayInventory();
        history.displayBookings();

        // Save state before shutdown
        PersistenceService.save(new SystemState(inventory, history));
    }
}