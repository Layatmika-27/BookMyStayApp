import java.util.*;

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

// Thread-safe Inventory
class RoomInventory {

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 2);
        availability.put("Suite Room", 1);
    }

    // Critical section
    public synchronized boolean allocateRoom(String roomType) {

        int available = availability.getOrDefault(roomType, 0);

        if (available <= 0) {
            return false;
        }

        availability.put(roomType, available - 1);

        return true;
    }

    public synchronized void displayInventory() {

        System.out.println("\nRemaining Inventory:");

        for (Map.Entry<String, Integer> entry : availability.entrySet()) {
            System.out.println(entry.getKey() + " Available: " + entry.getValue());
        }
    }
}

// Shared Booking Queue
class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public synchronized void addRequest(Reservation r) {
        queue.offer(r);
    }

    public synchronized Reservation getNextRequest() {
        return queue.poll();
    }

    public synchronized boolean hasRequests() {
        return !queue.isEmpty();
    }
}

// Booking Processor (Thread)
class ConcurrentBookingProcessor extends Thread {

    private BookingRequestQueue queue;
    private RoomInventory inventory;

    public ConcurrentBookingProcessor(String name,
                                      BookingRequestQueue queue,
                                      RoomInventory inventory) {

        super(name);
        this.queue = queue;
        this.inventory = inventory;
    }

    @Override
    public void run() {

        while (true) {

            Reservation reservation;

            synchronized (queue) {

                if (!queue.hasRequests()) {
                    break;
                }

                reservation = queue.getNextRequest();
            }

            if (reservation == null) {
                continue;
            }

            boolean allocated = inventory.allocateRoom(reservation.getRoomType());

            if (allocated) {

                System.out.println(
                        Thread.currentThread().getName()
                                + " confirmed booking for "
                                + reservation.getGuestName()
                                + " (" + reservation.getRoomType() + ")"
                );

            } else {

                System.out.println(
                        Thread.currentThread().getName()
                                + " failed booking for "
                                + reservation.getGuestName()
                                + " (No rooms available)"
                );
            }
        }
    }
}

// Main Application
public class BookMyStayApp {

    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();

        BookingRequestQueue queue = new BookingRequestQueue();

        // Simulate multiple guest requests
        queue.addRequest(new Reservation("RES101", "Alice", "Single Room"));
        queue.addRequest(new Reservation("RES102", "Bob", "Single Room"));
        queue.addRequest(new Reservation("RES103", "Charlie", "Single Room"));
        queue.addRequest(new Reservation("RES104", "David", "Double Room"));
        queue.addRequest(new Reservation("RES105", "Emma", "Suite Room"));

        // Multiple booking processors (threads)
        ConcurrentBookingProcessor t1 =
                new ConcurrentBookingProcessor("Processor-1", queue, inventory);

        ConcurrentBookingProcessor t2 =
                new ConcurrentBookingProcessor("Processor-2", queue, inventory);

        ConcurrentBookingProcessor t3 =
                new ConcurrentBookingProcessor("Processor-3", queue, inventory);

        // Start concurrent processing
        t1.start();
        t2.start();
        t3.start();

        try {

            t1.join();
            t2.join();
            t3.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        inventory.displayInventory();
    }
}