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

    private HashMap<String, Integer> availability = new HashMap<>();

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
}

// Reservation Object
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

// Booking Queue
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

// Booking Service (Room Allocation)
class BookingService {

    private RoomInventory inventory;

    private Set<String> allocatedRoomIds = new HashSet<>();

    private HashMap<String, String> reservationRoomMap = new HashMap<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void processReservations(BookingRequestQueue queue) {

        while (queue.hasRequests()) {

            Reservation r = queue.nextRequest();

            if (inventory.getAvailability(r.getRoomType()) > 0) {

                String roomId = generateRoomId(r.getRoomType());

                while (allocatedRoomIds.contains(roomId)) {
                    roomId = generateRoomId(r.getRoomType());
                }

                allocatedRoomIds.add(roomId);

                reservationRoomMap.put(r.getReservationId(), roomId);

                inventory.decrementRoom(r.getRoomType());

                System.out.println("Reservation Confirmed");
                System.out.println("Guest: " + r.getGuestName());
                System.out.println("Room ID: " + roomId);
                System.out.println();

            } else {

                System.out.println("Reservation Failed for " + r.getGuestName());
            }
        }
    }

    private String generateRoomId(String type) {
        return type.replace(" ", "").toUpperCase() + "-" + (100 + new Random().nextInt(900));
    }
}

// Add-On Service
class AddOnService {

    private String serviceName;
    private double price;

    public AddOnService(String serviceName, double price) {
        this.serviceName = serviceName;
        this.price = price;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getPrice() {
        return price;
    }
}

// Add-On Service Manager
class AddOnServiceManager {

    private Map<String, List<AddOnService>> reservationServices = new HashMap<>();

    public void addService(String reservationId, AddOnService service) {

        reservationServices.putIfAbsent(reservationId, new ArrayList<>());

        reservationServices.get(reservationId).add(service);

        System.out.println(service.getServiceName() + " added to reservation " + reservationId);
    }

    public double calculateTotalServiceCost(String reservationId) {

        double total = 0;

        List<AddOnService> services = reservationServices.get(reservationId);

        if (services != null) {

            for (AddOnService s : services) {
                total += s.getPrice();
            }
        }

        return total;
    }

    public void displayServices(String reservationId) {

        List<AddOnService> services = reservationServices.get(reservationId);

        if (services == null) {
            System.out.println("No services selected.");
            return;
        }

        System.out.println("\nAdd-On Services for Reservation " + reservationId);

        for (AddOnService s : services) {
            System.out.println(s.getServiceName() + " - $" + s.getPrice());
        }

        System.out.println("Total Add-On Cost: $" + calculateTotalServiceCost(reservationId));
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();

        BookingRequestQueue queue = new BookingRequestQueue();

        BookingService bookingService = new BookingService(inventory);

        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Create reservation requests
        Reservation r1 = new Reservation("RES101", "Alice", "Single Room");
        Reservation r2 = new Reservation("RES102", "Bob", "Double Room");

        queue.addRequest(r1);
        queue.addRequest(r2);

        // Process bookings
        bookingService.processReservations(queue);

        // Add optional services
        serviceManager.addService("RES101", new AddOnService("Breakfast", 20));
        serviceManager.addService("RES101", new AddOnService("Airport Pickup", 40));
        serviceManager.addService("RES102", new AddOnService("Spa Access", 60));

        // Display services
        serviceManager.displayServices("RES101");
        serviceManager.displayServices("RES102");
    }
}