import java.util.*;

// Custom Exception
class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}

// Reservation
class Reservation {

    private String reservationId;
    private String guestName;
    private String roomType;
    private boolean cancelled = false;

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

    public boolean isCancelled() {
        return cancelled;
    }

    public void markCancelled() {
        cancelled = true;
    }
}

// Inventory Service
class RoomInventory {

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 2);
        availability.put("Suite Room", 1);
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    public void decrementRoom(String roomType) {
        availability.put(roomType, availability.get(roomType) - 1);
    }

    public void incrementRoom(String roomType) {
        availability.put(roomType, availability.get(roomType) + 1);
    }

    public void displayInventory() {
        System.out.println("\nCurrent Inventory:");
        for (Map.Entry<String, Integer> entry : availability.entrySet()) {
            System.out.println(entry.getKey() + " Available: " + entry.getValue());
        }
    }
}

// Booking History
class BookingHistory {

    private Map<String, Reservation> reservations = new HashMap<>();

    public void recordReservation(Reservation r) {
        reservations.put(r.getReservationId(), r);
    }

    public Reservation getReservation(String reservationId) {
        return reservations.get(reservationId);
    }

    public Collection<Reservation> getAllReservations() {
        return reservations.values();
    }
}

// Booking Service
class BookingService {

    private RoomInventory inventory;
    private BookingHistory history;

    private Map<String, String> reservationRoomMap = new HashMap<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void confirmReservation(Reservation r) throws BookingException {

        if (inventory.getAvailability(r.getRoomType()) <= 0) {
            throw new BookingException("No rooms available for " + r.getRoomType());
        }

        String roomId = generateRoomId(r.getRoomType());

        reservationRoomMap.put(r.getReservationId(), roomId);

        inventory.decrementRoom(r.getRoomType());

        history.recordReservation(r);

        System.out.println("Reservation Confirmed");
        System.out.println("Guest: " + r.getGuestName());
        System.out.println("Room ID: " + roomId);
    }

    public String getAssignedRoom(String reservationId) {
        return reservationRoomMap.get(reservationId);
    }

    private String generateRoomId(String type) {
        return type.replace(" ", "").toUpperCase() + "-" + (100 + new Random().nextInt(900));
    }
}

// Cancellation Service
class CancellationService {

    private RoomInventory inventory;
    private BookingHistory history;
    private BookingService bookingService;

    // Stack to track released room IDs
    private Stack<String> rollbackStack = new Stack<>();

    public CancellationService(RoomInventory inventory,
                               BookingHistory history,
                               BookingService bookingService) {

        this.inventory = inventory;
        this.history = history;
        this.bookingService = bookingService;
    }

    public void cancelReservation(String reservationId) throws BookingException {

        Reservation reservation = history.getReservation(reservationId);

        if (reservation == null) {
            throw new BookingException("Reservation does not exist.");
        }

        if (reservation.isCancelled()) {
            throw new BookingException("Reservation already cancelled.");
        }

        String roomId = bookingService.getAssignedRoom(reservationId);

        rollbackStack.push(roomId);

        inventory.incrementRoom(reservation.getRoomType());

        reservation.markCancelled();

        System.out.println("\nReservation Cancelled");
        System.out.println("Released Room ID: " + roomId);
    }

    public void displayRollbackStack() {

        System.out.println("\nRollback Stack (Recently Released Rooms):");

        for (String id : rollbackStack) {
            System.out.println(id);
        }
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();

        BookingHistory history = new BookingHistory();

        BookingService bookingService =
                new BookingService(inventory, history);

        CancellationService cancellationService =
                new CancellationService(inventory, history, bookingService);

        try {

            // Confirm reservations
            Reservation r1 = new Reservation("RES101", "Alice", "Single Room");
            Reservation r2 = new Reservation("RES102", "Bob", "Double Room");

            bookingService.confirmReservation(r1);
            bookingService.confirmReservation(r2);

            inventory.displayInventory();

            // Cancel reservation
            cancellationService.cancelReservation("RES101");

            inventory.displayInventory();

            cancellationService.displayRollbackStack();

        } catch (BookingException e) {

            System.out.println("Operation Failed: " + e.getMessage());
        }
    }
}