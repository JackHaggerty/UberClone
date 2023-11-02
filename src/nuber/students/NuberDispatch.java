package nuber.students;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	private final int MAX_DRIVERS = 999;
	
	private boolean logEvents;
	//thread safe queue
	Queue<Driver> idleDrivers = new ConcurrentLinkedQueue<>();
	//private Semaphore availableDrivers = new Semaphore(1);
	private HashMap<String, NuberRegion> regionData = new HashMap<>();
	private int bookingCount = 0;


	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents)
	{
		this.logEvents = logEvents;
	    // Instantiate the regions
	    for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) {
	        String regionName = entry.getKey();
	        int maxBookings = entry.getValue();
	        NuberRegion region = new NuberRegion(this, regionName, maxBookings);
	        regionData.put(regionName, region);
	    }
	    
		
	}
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public synchronized boolean addDriver(Driver newDriver)
	{
		Boolean result = idleDrivers.offer(newDriver);
		if (result) {
			bookingCount--;
			if (bookingCount < 0) {
				bookingCount = 0;
			}
		}
		try {
			notifyAll();
		}
		catch (Exception e) {
			System.out.println("Exception "+e+" on notifyAll");
		}
		return result;
	}
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 */
	public synchronized Driver getDriver() throws InterruptedException
	{
		bookingCount++;
		try {
			while (idleDrivers.isEmpty()) {
				
				wait();
			}
		}
		catch (Exception e) {
			System.out.println("Exception "+e+" on wait");
		}
		
	    return idleDrivers.poll();
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		
		// get region and book passenger
		Future<BookingResult> result = regionData.get(region).bookPassenger(passenger);
		

		return result;

	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver()
	{
		return bookingCount;
	}
	public synchronized void decrementBookingCount() {
	    bookingCount--;
	}
	
	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
	    for (NuberRegion region : regionData.values()) {
	        region.shutdown();
	    }
	}

}
