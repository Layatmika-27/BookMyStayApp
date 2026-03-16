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

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public int getSize() {
        return size;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void displayRoomDetails() {
        System.out.println("Room Type: " + roomType);
        System.out.println("Beds: " + numberOfBeds);
        System.out.println("Size: " + size + " sq ft");
        System.out.println("Price per Night: $" + pricePerNight);
    }
}

// Concrete Room Classes
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

// Centralized Inventory
class RoomInventory {

    private HashMap<String, Integer> availability;

    public RoomInventory() {
        availability = new HashMap<>();

        availability.put("Single Room", 5);
        availability.put("Double Room", 3);
        availability.put("Suite Room", 0); // Example unavailable room
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    public Map<String, Integer> getAllAvailability() {
        return availability;
    }
}

// Search Service (Read-Only Access)
class SearchService {

    private RoomInventory inventory;

    public SearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public void searchAvailableRooms(List<Room> rooms) {

        System.out.println("---- Available Rooms ----");

        for (Room room : rooms) {

            int available = inventory.getAvailability(room.getRoomType());

            // Defensive validation (filter unavailable rooms)
            if (available > 0) {
                room.displayRoomDetails();
                System.out.println("Available Rooms: " + available);
                System.out.println();
            }
        }
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("Welcome to BookMyStay\n");

        // Initialize room objects
        List<Room> rooms = new ArrayList<>();

        rooms.add(new SingleRoom());
        rooms.add(new DoubleRoom());
        rooms.add(new SuiteRoom());

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize search service
        SearchService searchService = new SearchService(inventory);

        // Guest performs search
        searchService.searchAvailableRooms(rooms);

        System.out.println("Search completed. System state unchanged.");
    }
}