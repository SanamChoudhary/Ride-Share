package observer;

import core.TripEvent;

public class TripLogger implements TripObserver {

    @Override
    public void onTripEvent(TripEvent event) {
        System.out.println("LOG: [" + event.getTimestamp() + "] Trip " + event.getTripId()
                + " -> " + event.getType()
                + (event.getDriverName() != null ? (" (driver=" + event.getDriverName() + ")") : "")
                + (event.getFare() != null ? (" (fare=$" + String.format("%.2f", event.getFare()) + ")") : ""));
    }
}
