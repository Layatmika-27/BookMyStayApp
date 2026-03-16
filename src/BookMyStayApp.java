import java.util.*;

// Abstract Room Class
abstract class Room {

    private String roomType;
    private int numberOfBeds;
    private int size;
    private double pricePerNight;

    public Room(String roomType, int numberOfBeds, int size, double pricePerNight) {
        this.roomType = roomType;
        this.numberOfBeds = numberOfBeds;
        this.size = size;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() {
        return roomType;
    }

    public void displayRoomDetails() {
        System.out.println("Room Type: " + roomType);
        System.out.println("Beds: " + numberOfBeds);
        System.out.println("Size: " + size + " sq ft");
        System.out.println("Price per Night: $" + pricePerNight);
    }
}

// Concrete Room Types
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 200, 100);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 350, 180);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 500, 350);
    }
}

// Inventory Service
class RoomInventory {

    private HashMap<String, Integer> availability;

    public RoomInventory() {
        availability = new HashMap<>();

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

    public void displayInventory() {
        System.out.println("\n---- Current Inventory ----");
        for (Map.Entry<String, Integer> entry : availability.entrySet()) {
            System.out.println(entry.getKey() + " Available: " + entry.getValue());
        }
    }
}

// Reservation Object
class Reservation {

    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
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

    private Queue<Reservation> requestQueue = new LinkedList<>();

    public void addRequest(Reservation reservation) {
        requestQueue.offer(reservation);
        System.out.println("Request added: " + reservation.getGuestName());
    }

    public Reservation getNextRequest() {
        return requestQueue.poll();
    }

    public boolean hasRequests() {
        return !requestQueue.isEmpty();
    }
}

// Booking Service (Allocation Logic)
class BookingService {

    private RoomInventory inventory;

    // Track allocated room IDs
    private Set<String> allocatedRoomIds = new HashSet<>();

    // Track allocations per room type
    private HashMap<String, Set<String>> roomAllocations = new HashMap<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processReservations(BookingRequestQueue queue) {

        System.out.println("\n---- Processing Reservations ----");

        while (queue.hasRequests()) {

            Reservation reservation = queue.getNextRequest();
            String roomType = reservation.getRoomType();

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                // Generate unique room ID
                String roomId = generateRoomId(roomType);

                // Prevent duplicates
                while (allocatedRoomIds.contains(roomId)) {
                    roomId = generateRoomId(roomType);
                }

                allocatedRoomIds.add(roomId);

                // Map room type to allocated room IDs
                roomAllocations.putIfAbsent(roomType, new HashSet<>());
                roomAllocations.get(roomType).add(roomId);

                // Update inventory immediately
                inventory.decrementRoom(roomType);

                System.out.println("Reservation Confirmed");
                System.out.println("Guest: " + reservation.getGuestName());
                System.out.println("Room Type: " + roomType);
                System.out.println("Assigned Room ID: " + roomId);
                System.out.println();

            } else {

                System.out.println("Reservation Failed for "
                        + reservation.getGuestName()
                        + " (No rooms available)");
            }
        }
    }

    private String generateRoomId(String roomType) {
        return roomType.replace(" ", "").toUpperCase() + "-" + (100 + new Random().nextInt(900));
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to BookMyStay\n");

        // Initialize services
        RoomInventory inventory = new RoomInventory();
        BookingRequestQueue queue = new BookingRequestQueue();
        BookingService bookingService = new BookingService(inventory);

        // Create booking requests
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Single Room"));
        queue.addRequest(new Reservation("Charlie", "Single Room"));
        queue.addRequest(new Reservation("David", "Suite Room"));

        // Process reservations
        bookingService.processReservations(queue);

        // Display updated inventory
        inventory.displayInventory();
    }
}