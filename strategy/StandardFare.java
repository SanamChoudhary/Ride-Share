package strategy;

import core.Trip;
import exceptions.InvalidParameterException;

public class StandardFare implements FareStrategy {
    private final double baseFare;
    private final double perMileRate;

    public StandardFare(double baseFare, double perMileRate) throws InvalidParameterException {
        if (baseFare < 0) {
            throw new InvalidParameterException("Base fare cannot be negative");
        }
        if (perMileRate < 0) {
            throw new InvalidParameterException("Per mile rate cannot be negative");
        }
        this.baseFare = baseFare;
        this.perMileRate = perMileRate;
    }

    @Override
    public double calculateFare(Trip trip) throws InvalidParameterException {
        if (trip.getDistanceMiles() <= 0) {
            throw new InvalidParameterException("Distance must be greater than 0");
        }
        return baseFare + (trip.getDistanceMiles() * perMileRate);
    }

    @Override
    public String name() {
        return "StandardFare";
    }
}
