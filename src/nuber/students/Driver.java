package nuber.students;

public class Driver extends Person {
	private Passenger currentPassenger;
	private int maxDelay;
	
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
		this.maxDelay = maxDelay;
	}
	
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger) throws InterruptedException 
	{
		this.currentPassenger = newPassenger;
        int sleepTime = (int)(Math.random() * maxDelay);
        Thread.sleep(sleepTime);
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() throws InterruptedException {
        // Sleep the current thread for the travel time of the passenger
        int sleepTime = currentPassenger.getTravelTime();
        Thread.sleep(sleepTime);

        // Once the destination is reached, set the currentPassenger to null
        currentPassenger = null;
	}
	
}
