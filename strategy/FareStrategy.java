package strategy;

import core.Trip;
import exceptions.InvalidParameterException;

public interface FareStrategy {
    double calculateFare(Trip trip) throws InvalidParameterException;
    String name();
}
