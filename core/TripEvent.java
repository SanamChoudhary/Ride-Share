package core;

public class TripEvent {
    private final String tripId;
    private final TripEventType type;
    private final String timestamp;
    private final String riderName;
    private final String driverName;
    private final Double fare;

    public TripEvent(String tripId, TripEventType type, String timestamp,
                     String riderName, String driverName, Double fare) {
        this.tripId = tripId;
        this.type = type;
        this.timestamp = timestamp;
        this.riderName = riderName;
        this.driverName = driverName;
        this.fare = fare;
    }

    public String getTripId() {
        return tripId;
    }

    public TripEventType getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getRiderName() {
        return riderName;
    }

    public String getDriverName() {
        return driverName;
    }

    public Double getFare() {
        return fare;
    }
}
