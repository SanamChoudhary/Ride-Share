package strategy;

import core.Trip;
import exceptions.InvalidParameterException;

public class SurgeFare implements FareStrategy {
    private final FareStrategy base;
    private final double multiplier;

    public SurgeFare(FareStrategy base, double multiplier) throws InvalidParameterException {
        if (base == null) {
            throw new InvalidParameterException("Base strategy cannot be null");
        }
        if (multiplier <= 0) {
            throw new InvalidParameterException("Multiplier must be greater than 0");
        }
        this.base = base;
        this.multiplier = multiplier;
    }

    @Override
    public double calculateFare(Trip trip) throws InvalidParameterException {
        return base.calculateFare(trip) * multiplier;
    }

    @Override
    public String name() {
        return "SurgeFare";
    }
}
