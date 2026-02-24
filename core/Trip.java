package core;

import exceptions.IllegalTransitionException;
import exceptions.InvalidParameterException;
import observer.TripObserver;
import strategy.FareStrategy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Trip {
    private final String tripId;
    private final String riderName;
    private final String pickupLocation;
    private final String dropoffLocation;
    private final double distanceMiles;
    private TripStatus status;
    private FareStrategy fareStrategy;
    private String driverName;
    private Double lastFare;
    private final List<TripObserver> observers;

    public Trip(String riderName, String pickupLocation, String dropoffLocation,
                double distanceMiles, FareStrategy fareStrategy)
            throws InvalidParameterException {
        if (riderName == null || riderName.isBlank()) {
            throw new InvalidParameterException("Rider name cannot be null or blank");
        }
        if (pickupLocation == null || pickupLocation.isBlank()) {
            throw new InvalidParameterException("Pickup location cannot be null or blank");
        }
        if (dropoffLocation == null || dropoffLocation.isBlank()) {
            throw new InvalidParameterException("Dropoff location cannot be null or blank");
        }
        if (distanceMiles <= 0) {
            throw new InvalidParameterException("Distance must be greater than 0");
        }
        if (fareStrategy == null) {
            throw new InvalidParameterException("Fare strategy cannot be null");
        }
        this.tripId = UUID.randomUUID().toString();
        this.riderName = riderName;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.distanceMiles = distanceMiles;
        this.fareStrategy = fareStrategy;
        this.status = TripStatus.CREATED;
        this.driverName = null;
        this.lastFare = null;
        this.observers = new ArrayList<>();
    }

    public String getTripId() {
        return tripId;
    }

    public String getRiderName() {
        return riderName;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public double getDistanceMiles() {
        return distanceMiles;
    }

    public TripStatus getStatus() {
        return status;
    }

    public FareStrategy getFareStrategy() {
        return fareStrategy;
    }

    public String getDriverName() {
        return driverName;
    }

    public Double getLastFare() {
        return lastFare;
    }

    public void setFareStrategy(FareStrategy fareStrategy) throws InvalidParameterException {
        if (fareStrategy == null) {
            throw new InvalidParameterException("Fare strategy cannot be null");
        }
        this.fareStrategy = fareStrategy;
    }

    public void addObserver(TripObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TripObserver observer) {
        observers.remove(observer);
    }

    public void request() throws IllegalTransitionException {
        if (status != TripStatus.CREATED) {
            throw new IllegalTransitionException("Cannot request trip from status: " + status);
        }
        status = TripStatus.REQUESTED;
        notifyObservers(new TripEvent(tripId, TripEventType.REQUESTED,
                Instant.now().toString(), riderName, driverName, null));
    }

    public void accept(String driverName) throws IllegalTransitionException, InvalidParameterException {
        if (status != TripStatus.REQUESTED) {
            throw new IllegalTransitionException("Cannot accept trip from status: " + status);
        }
        if (driverName == null || driverName.isBlank()) {
            throw new InvalidParameterException("Driver name cannot be null or blank");
        }
        this.driverName = driverName;
        status = TripStatus.ACCEPTED;
        notifyObservers(new TripEvent(tripId, TripEventType.ACCEPTED,
                Instant.now().toString(), riderName, driverName, null));
    }

    public void start() throws IllegalTransitionException {
        if (status != TripStatus.ACCEPTED) {
            throw new IllegalTransitionException("Cannot start trip from status: " + status);
        }
        status = TripStatus.STARTED;
        notifyObservers(new TripEvent(tripId, TripEventType.STARTED,
                Instant.now().toString(), riderName, driverName, null));
    }

    public double complete() throws IllegalTransitionException, InvalidParameterException {
        if (status != TripStatus.STARTED) {
            throw new IllegalTransitionException("Cannot complete trip from status: " + status);
        }
        double fare = fareStrategy.calculateFare(this);
        this.lastFare = fare;
        status = TripStatus.COMPLETED;
        notifyObservers(new TripEvent(tripId, TripEventType.COMPLETED,
                Instant.now().toString(), riderName, driverName, fare));
        return fare;
    }

    public void cancel() throws IllegalTransitionException {
        if (status != TripStatus.CREATED && status != TripStatus.REQUESTED
                && status != TripStatus.ACCEPTED) {
            throw new IllegalTransitionException("Cannot cancel trip from status: " + status);
        }
        status = TripStatus.CANCELLED;
        notifyObservers(new TripEvent(tripId, TripEventType.CANCELLED,
                Instant.now().toString(), riderName, driverName, null));
    }

    private void notifyObservers(TripEvent event) {
        List<TripObserver> snapshot = new ArrayList<>(observers);
        for (TripObserver observer : snapshot) {
            observer.onTripEvent(event);
        }
    }
}
