package observer;

import core.TripEvent;

public class DriverNotifier implements TripObserver {
    private final String driverName;

    public DriverNotifier(String driverName) {
        this.driverName = driverName;
    }

    @Override
    public void onTripEvent(TripEvent event) {
        switch (event.getType()) {
            case ACCEPTED:
                System.out.println("DRIVER APP (" + driverName + "): You accepted trip "
                        + event.getTripId() + ". Navigate to pickup.");
                break;
            case STARTED:
                System.out.println("DRIVER APP (" + driverName + "): Trip started. Drive safely.");
                break;
            case COMPLETED:
                System.out.println("DRIVER APP (" + driverName + "): Trip completed. Earnings: $"
                        + String.format("%.2f", event.getFare()));
                break;
            case CANCELLED:
                System.out.println("DRIVER APP (" + driverName + "): Trip was cancelled.");
                break;
        }
    }
}
