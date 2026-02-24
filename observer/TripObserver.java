package observer;

import core.TripEvent;

public interface TripObserver {
    void onTripEvent(TripEvent event);
}
