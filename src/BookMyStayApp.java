import java.util.*;

// Abstract Room Class
abstract class Room {

    private String roomType;
    private int beds;
    private int size;
    private double price;

    public Room(String roomType, int beds, int size, double price) {
        this.roomType = roomType;
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public String getRoomType() {
        return roomType;
    }
}

// Concrete Rooms
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room",1,200,100);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room",2,350,180);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room",3,500,350);
    }
}

// Inventory Service
class RoomInventory {

    private Map<String,Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room",2);
        availability.put("Double Room",2);
        availability.put("Suite Room",1);
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType,0);
    }

    public void decrementRoom(String roomType) {
        availability.put(roomType, availability.get(roomType)-1);
    }
}

// Reservation
class Reservation {

    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String reservationId,String guestName,String roomType) {
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

// Booking History (Chronological Storage)
class BookingHistory {

    private List<Reservation> confirmedBookings = new ArrayList<>();

    public void recordReservation(Reservation r) {
        confirmedBookings.add(r);
    }

    public List<Reservation> getAllReservations() {
        return confirmedBookings;
    }
}

// Booking Service
class BookingService {

    private RoomInventory inventory;
    private BookingHistory history;

    private Set<String> allocatedRoomIds = new HashSet<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    public void processReservations(BookingRequestQueue queue) {

        while(queue.hasRequests()) {

            Reservation r = queue.nextRequest();

            if(inventory.getAvailability(r.getRoomType()) > 0) {

                String roomId = generateRoomId(r.getRoomType());

                while(allocatedRoomIds.contains(roomId)) {
                    roomId = generateRoomId(r.getRoomType());
                }

                allocatedRoomIds.add(roomId);

                inventory.decrementRoom(r.getRoomType());

                history.recordReservation(r);

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
        return type.replace(" ","").toUpperCase() + "-" + (100 + new Random().nextInt(900));
    }
}

// Reporting Service
class BookingReportService {

    public void displayBookingHistory(BookingHistory history) {

        System.out.println("\n--- Booking History ---");

        List<Reservation> reservations = history.getAllReservations();

        for(Reservation r : reservations) {
            System.out.println("Reservation ID: " + r.getReservationId()
                    + " | Guest: " + r.getGuestName()
                    + " | Room Type: " + r.getRoomType());
        }
    }

    public void generateSummaryReport(BookingHistory history) {

        Map<String,Integer> roomCounts = new HashMap<>();

        for(Reservation r : history.getAllReservations()) {

            roomCounts.put(
                    r.getRoomType(),
                    roomCounts.getOrDefault(r.getRoomType(),0) + 1
            );
        }

        System.out.println("\n--- Booking Summary Report ---");

        for(Map.Entry<String,Integer> entry : roomCounts.entrySet()) {
            System.out.println(entry.getKey() + " Bookings: " + entry.getValue());
        }
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();

        BookingHistory history = new BookingHistory();

        BookingRequestQueue queue = new BookingRequestQueue();

        BookingService bookingService = new BookingService(inventory, history);

        BookingReportService reportService = new BookingReportService();

        // Create booking requests
        queue.addRequest(new Reservation("RES101","Alice","Single Room"));
        queue.addRequest(new Reservation("RES102","Bob","Double Room"));
        queue.addRequest(new Reservation("RES103","Charlie","Single Room"));

        // Process reservations
        bookingService.processReservations(queue);

        // Admin views booking history
        reportService.displayBookingHistory(history);

        // Admin generates summary report
        reportService.generateSummaryReport(history);
    }
}