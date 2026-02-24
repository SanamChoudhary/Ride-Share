package observer;

import core.TripEvent;

public class RiderNotifier implements TripObserver {
    private final String riderName;

    public RiderNotifier(String riderName) {
        this.riderName = riderName;
    }

    @Override
    public void onTripEvent(TripEvent event) {
        switch (event.getType()) {
            case REQUESTED:
                System.out.println("RIDER APP (" + riderName + "): Trip requested. Finding a driver...");
                break;
            case ACCEPTED:
                System.out.println("RIDER APP (" + riderName + "): Driver "
                        + event.getDriverName() + " accepted your trip.");
                break;
            case STARTED:
                System.out.println("RIDER APP (" + riderName + "): Trip started. Enjoy the ride!");
                break;
            case COMPLETED:
                System.out.println("RIDER APP (" + riderName + "): Trip completed. Total: $"
                        + String.format("%.2f", event.getFare()));
                break;
            case CANCELLED:
                System.out.println("RIDER APP (" + riderName + "): Trip cancelled.");
                break;
        }
    }
}
