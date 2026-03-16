import java.util.*;

// Custom Exception for Invalid Booking
class InvalidBookingException extends Exception {

    public InvalidBookingException(String message) {
        super(message);
    }
}

// Abstract Room Class
abstract class Room {

    private String roomType;

    public Room(String roomType) {
        this.roomType = roomType;
    }

    public String getRoomType() {
        return roomType;
    }
}

// Concrete Rooms
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room");
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room");
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room");
    }
}

// Inventory Service
class RoomInventory {

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 1);
        availability.put("Suite Room", 1);
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, -1);
    }

    public void decrementRoom(String roomType) throws InvalidBookingException {

        int current = availability.getOrDefault(roomType, -1);

        if (current <= 0) {
            throw new InvalidBookingException("No available rooms for " + roomType);
        }

        availability.put(roomType, current - 1);
    }
}

// Reservation
class Reservation {

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
}

// Booking Request Queue
class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation nextRequest() {
        return queue.poll();
    }

    public boolean hasRequests() {
        return !queue.isEmpty();
    }
}

// Booking History
class BookingHistory {

    private List<Reservation> confirmedBookings = new ArrayList<>();

    public void recordReservation(Reservation r) {
        confirmedBookings.add(r);
    }

    public List<Reservation> getAllReservations() {
        return confirmedBookings;
    }
}

// Validator Service
class InvalidBookingValidator {

    private Set<String> validRoomTypes = new HashSet<>();

    public InvalidBookingValidator() {
        validRoomTypes.add("Single Room");
        validRoomTypes.add("Double Room");
        validRoomTypes.add("Suite Room");
    }

    public void validateReservation(Reservation r) throws InvalidBookingException {

        if (r.getGuestName() == null || r.getGuestName().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty.");
        }

        if (!validRoomTypes.contains(r.getRoomType())) {
            throw new InvalidBookingException("Invalid room type: " + r.getRoomType());
        }
    }
}

// Booking Service
class BookingService {

    private RoomInventory inventory;
    private BookingHistory history;
    private InvalidBookingValidator validator;

    private Set<String> allocatedRoomIds = new HashSet<>();

    public BookingService(RoomInventory inventory,
                          BookingHistory history,
                          InvalidBookingValidator validator) {

        this.inventory = inventory;
        this.history = history;
        this.validator = validator;
    }

    public void processReservations(BookingRequestQueue queue) {

        while (queue.hasRequests()) {

            Reservation r = queue.nextRequest();

            try {

                // Validation (Fail Fast)
                validator.validateReservation(r);

                if (inventory.getAvailability(r.getRoomType()) <= 0) {
                    throw new InvalidBookingException(
                            "No rooms available for " + r.getRoomType());
                }

                String roomId = generateRoomId(r.getRoomType());

                while (allocatedRoomIds.contains(roomId)) {
                    roomId = generateRoomId(r.getRoomType());
                }

                allocatedRoomIds.add(roomId);

                inventory.decrementRoom(r.getRoomType());

                history.recordReservation(r);

                System.out.println("Reservation Confirmed");
                System.out.println("Guest: " + r.getGuestName());
                System.out.println("Room ID: " + roomId);
                System.out.println();

            } catch (InvalidBookingException e) {

                System.out.println("Booking Failed for "
                        + r.getGuestName()
                        + " -> " + e.getMessage());
            }
        }
    }

    private String generateRoomId(String type) {
        return type.replace(" ", "").toUpperCase()
                + "-" + (100 + new Random().nextInt(900));
    }
}

// Reporting Service
class BookingReportService {

    public void displayHistory(BookingHistory history) {

        System.out.println("\n--- Booking History ---");

        for (Reservation r : history.getAllReservations()) {

            System.out.println(
                    r.getReservationId()
                            + " | " + r.getGuestName()
                            + " | " + r.getRoomType());
        }
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();

        BookingHistory history = new BookingHistory();

        InvalidBookingValidator validator = new InvalidBookingValidator();

        BookingRequestQueue queue = new BookingRequestQueue();

        BookingService bookingService =
                new BookingService(inventory, history, validator);

        BookingReportService reportService = new BookingReportService();

        // Booking requests (including invalid ones)
        queue.addRequest(new Reservation("RES101", "Alice", "Single Room"));
        queue.addRequest(new Reservation("RES102", "Bob", "Luxury Room")); // invalid type
        queue.addRequest(new Reservation("RES103", "", "Suite Room"));     // invalid name
        queue.addRequest(new Reservation("RES104", "Charlie", "Double Room"));

        // Process bookings
        bookingService.processReservations(queue);

        // Admin views booking history
        reportService.displayHistory(history);
    }
}