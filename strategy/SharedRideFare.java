package strategy;

import core.Trip;
import exceptions.InvalidParameterException;

public class SharedRideFare implements FareStrategy {
    private final double baseFare;
    private final double perMileRate;
    private final double discountRate;

    public SharedRideFare(double baseFare, double perMileRate, double discountRate)
            throws InvalidParameterException {
        if (baseFare < 0) {
            throw new InvalidParameterException("Base fare cannot be negative");
        }
        if (perMileRate < 0) {
            throw new InvalidParameterException("Per mile rate cannot be negative");
        }
        if (discountRate < 0 || discountRate > 1) {
            throw new InvalidParameterException("Discount rate must be between 0 and 1");
        }
        this.baseFare = baseFare;
        this.perMileRate = perMileRate;
        this.discountRate = discountRate;
    }

    @Override
    public double calculateFare(Trip trip) throws InvalidParameterException {
        if (trip.getDistanceMiles() <= 0) {
            throw new InvalidParameterException("Distance must be greater than 0");
        }
        return (baseFare + trip.getDistanceMiles() * perMileRate) * (1 - discountRate);
    }

    @Override
    public String name() {
        return "SharedRideFare";
    }
}
