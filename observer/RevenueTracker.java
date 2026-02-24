package observer;

import core.TripEvent;
import core.TripEventType;

public class RevenueTracker implements TripObserver {
    private double totalRevenue = 0.0;
    private int completedTrips = 0;

    @Override
    public void onTripEvent(TripEvent event) {
        if (event.getType() == TripEventType.COMPLETED) {
            totalRevenue += event.getFare();
            completedTrips++;
            System.out.println("REVENUE: +$" + String.format("%.2f", event.getFare())
                    + " (total revenue=$" + String.format("%.2f", totalRevenue)
                    + ", trips=" + completedTrips + ")");
        }
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public int getCompletedTrips() {
        return completedTrips;
    }
}
