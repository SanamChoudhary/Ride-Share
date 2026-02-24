import core.*;
import exceptions.*;
import observer.*;
import strategy.*;

import java.util.ArrayList;
import java.util.List;

/**
 * SELF-SCORING Main (Total = 75 points)
 * <p>
 * This version uses ONLY application-defined CHECKED exceptions:
 *   - InvalidParameterException
 *   - IllegalTransitionException
 * <p>
 * Weighting:
 *   - Strategy: 42 pts (Standard, Surge, Shared, runtime swap)
 *   - Observer: 26 pts (detach, event integrity, snapshot safety)
 *   - Guardrails: 7 pts (illegal transitions + invalid parameters)
 * <p>
 * Drop-in replacement for src/se350/Main.java
 */
public final class Main {

    // ------------------------------------------------------------
    // Tiny self-scoring harness (supports checked exceptions)
    // ------------------------------------------------------------
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static int earned = 0;
    private static int possible = 0;

    private static void scoredTest(String name, int points, ThrowingRunnable r) {
        System.out.println("\n[TEST] " + name + " (" + points + " pts)");
        possible += points;
        try {
            r.run();
            earned += points;
            System.out.println("PASS (+" + points + ")");
        } catch (Throwable t) {
            System.out.println("FAIL (+" + 0 + " of " + points + "): "
                    + t.getClass().getSimpleName() + " - " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    private static void scoredExpectThrows(String name, int points, Class<? extends Throwable> expected, ThrowingRunnable r) {
        System.out.println("\n[TEST] " + name + " (" + points + " pts)  expect " + expected.getSimpleName());
        possible += points;
        try {
            r.run();
            System.out.println("FAIL (+" + 0 + " of " + points + "): Expected exception but none was thrown");
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                earned += points;
                System.out.println("PASS (+" + points + ") threw " + t.getClass().getSimpleName());
            } else {
                System.out.println("FAIL (+" + 0 + " of " + points + "): Threw "
                        + t.getClass().getSimpleName() + " but expected " + expected.getSimpleName());
                t.printStackTrace(System.out);
            }
        }
    }

    private static void assertTrue(boolean condition, String msgIfFalse) throws InvalidParameterException {
        if (!condition) throw new InvalidParameterException(msgIfFalse);
    }

    private static void assertEquals(double expected, double actual, String msg) throws InvalidParameterException {
        if (Math.abs(expected - actual) > 1.0E-4) {
            throw new InvalidParameterException(msg + " expected=" + expected + " actual=" + actual);
        }
    }

    private static double roundToCents(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ------------------------------------------------------------
    // Test observers for complex behaviors
    // ------------------------------------------------------------
    private static final class CountingObserver implements TripObserver {
        private int count = 0;
        private final TripEventType filter; // null = count all

        CountingObserver() { this.filter = null; }
        CountingObserver(TripEventType filter) { this.filter = filter; }

        @Override
        public void onTripEvent(TripEvent event) {
            if (event == null) return;
            if (filter == null || event.getType() == filter) count++;
        }

        int getCount() { return count; }
    }

    /**
     * Removes another observer during notification to ensure Trip.notifyObservers()
     * uses a snapshot list (avoids ConcurrentModificationException).
     */
    private static final class RemovesOtherObserverOnAccepted implements TripObserver {
        private final Trip trip;
        private final TripObserver targetToRemove;
        private boolean removed = false;

        RemovesOtherObserverOnAccepted(Trip trip, TripObserver targetToRemove) throws InvalidParameterException {
            if (trip == null) throw new InvalidParameterException("trip must not be null");
            if (targetToRemove == null) throw new InvalidParameterException("targetToRemove must not be null");
            this.trip = trip;
            this.targetToRemove = targetToRemove;
        }

        @Override
        public void onTripEvent(TripEvent event) {
            if (event == null) return;

            if (!removed && event.getType() == TripEventType.ACCEPTED) {
                System.out.println("  (Observer removes another observer during ACCEPTED)");
                try {
                    trip.removeObserver(targetToRemove);
                    removed = true;
                } catch (Exception e) {
                    // For this instructor harness, swallow: we only care that Trip notification does not crash.
                    System.out.println("  (Unexpected exception removing observer: " + e.getMessage() + ")");
                }
            }
        }
    }

    // ------------------------------------------------------------
    // MAIN
    // ------------------------------------------------------------
    public static void main(String[] args) {

        // Build strategies (constructors are checked-exception based)
        FareStrategy standard;
        FareStrategy surge;
        FareStrategy shared;

        try {
            standard = new StandardFare(2.50, 1.75);
            surge = new SurgeFare(standard, 1.80);
            shared = new SharedRideFare(2.00, 1.25, 0.10);
        } catch (InvalidParameterException e) {
            System.out.println("FATAL: Could not initialize strategies: " + e.getMessage());
            e.printStackTrace(System.out);
            return;
        }

        System.out.println("==================================================");
        System.out.println("  Rideshare Strategy + Observer — Demo + 75-pt Score");
        System.out.println("==================================================");

        // ------------------------------------------------------------
        // DEMOS (not scored)
        // ------------------------------------------------------------
        try {
            System.out.println("\n=== DEMO A: Standard trip w/ Rider + Logger + Revenue ===");
            TripLogger demoLogger = new TripLogger();
            RevenueTracker demoRevenue = new RevenueTracker();

            Trip demo1 = new Trip("Chris", "DePaul Loop", "Wrigley Field", 5.2, standard);
            demo1.addObserver(demoLogger);
            demo1.addObserver(demoRevenue);
            demo1.addObserver(new RiderNotifier(demo1.getRiderName()));

            demo1.request();
            demo1.accept("Ava");
            // optionally add a driver app observer from Main (pure Observer wiring)
            demo1.addObserver(new DriverNotifier("Ava"));

            demo1.start();
            double demoFare1 = demo1.complete();
            System.out.println("DEMO A fare (" + demo1.getFareStrategy().name() + "): $" + String.format("%.2f", demoFare1));

            System.out.println("\n=== DEMO B: Surge trip w/ detach RiderNotifier mid-trip ===");
            Trip demo2 = new Trip("Chris", "O'Hare", "Downtown", 16.4, surge);
            RiderNotifier riderApp2 = new RiderNotifier(demo2.getRiderName());

            demo2.addObserver(demoLogger);
            demo2.addObserver(demoRevenue);
            demo2.addObserver(riderApp2);

            demo2.request();
            demo2.accept("Noah");
            demo2.addObserver(new DriverNotifier("Noah"));

            System.out.println("  -- Detaching RiderNotifier now --");
            demo2.removeObserver(riderApp2);

            demo2.start();
            double demoFare2 = demo2.complete();
            System.out.println("DEMO B fare (" + demo2.getFareStrategy().name() + "): $" + String.format("%.2f", demoFare2));

            System.out.println("\n=== DEMO C: Shared ride quick trip ===");
            Trip demo3 = new Trip("Jamie", "Lincoln Park", "Chinatown", 7.8, shared);
            demo3.addObserver(demoLogger);
            demo3.addObserver(demoRevenue);

            demo3.request();
            demo3.accept("Mia");
            demo3.addObserver(new DriverNotifier("Mia"));

            demo3.start();
            double demoFare3 = demo3.complete();
            System.out.println("DEMO C fare (" + demo3.getFareStrategy().name() + "): $" + String.format("%.2f", demoFare3));

            System.out.println("\n--- Demo Revenue Summary ---");
            System.out.println("Completed trips tracked: " + demoRevenue.getCompletedTrips());
            System.out.println("Total revenue tracked: $" + String.format("%.2f", demoRevenue.getTotalRevenue()));
        } catch (Exception e) {
            System.out.println("⚠️ Demo section encountered an exception (continuing to scoring): " + e);
            e.printStackTrace(System.out);
        }

        // ------------------------------------------------------------
        // SELF-SCORING TESTS (Total MUST be 75 points)
        // ------------------------------------------------------------
        System.out.println("\n\n==================================================");
        System.out.println("  SELF-SCORING TESTS (Total 75 pts)");
        System.out.println("==================================================");

        // ============================================================
        // STRATEGY — 42 points
        // ============================================================

        scoredTest("Strategy: StandardFare math correct", 11, () -> {
            Trip t = new Trip("Riley", "A", "B", 10.0, standard);
            t.request();
            t.accept("Driver1");
            t.start();
            double fare = t.complete();

            double expected = roundToCents(2.50 + (1.75 * 10.0));
            assertEquals(expected, fare, "Standard fare mismatch.");
        });

        scoredTest("Strategy: SurgeFare wraps StandardFare correctly", 11, () -> {
            Trip t = new Trip("Riley", "A", "B", 10.0, surge);
            t.request();
            t.accept("Driver2");
            t.start();
            double fare = t.complete();

            double expected = roundToCents((2.50 + (1.75 * 10.0)) * 1.80);
            assertEquals(expected, fare, "Surge fare mismatch.");
        });

        scoredTest("Strategy: SharedRideFare discount applied", 10, () -> {
            Trip t = new Trip("Riley", "A", "B", 10.0, shared);
            t.request();
            t.accept("Driver3");
            t.start();
            double fare = t.complete();

            double raw = 2.00 + (1.25 * 10.0);
            double expected = roundToCents(raw * 0.90);
            assertEquals(expected, fare, "Shared fare mismatch.");
        });

        scoredTest("Strategy: Runtime strategy swap affects fare on completion", 10, () -> {
            Trip t = new Trip("Morgan", "Start", "End", 8.0, standard);
            t.request();
            t.accept("Driver4");
            t.start();

            t.setFareStrategy(surge); // swap before complete
            double fare = t.complete();

            double expected = roundToCents((2.50 + (1.75 * 8.0)) * 1.80);
            assertEquals(expected, fare, "Fare should reflect swapped strategy.");
        });

        // ============================================================
        // OBSERVER — 26 points
        // ============================================================

        scoredTest("Observer: attach/detach works (detached COMPLETED observer gets 0)", 11, () -> {
            Trip t = new Trip("Alex", "A", "B", 3.5, standard);

            CountingObserver allEvents = new CountingObserver();
            CountingObserver completedOnly = new CountingObserver(TripEventType.COMPLETED);

            t.addObserver(allEvents);
            t.addObserver(completedOnly);

            t.request();
            t.accept("Driver5");

            t.removeObserver(completedOnly); // detach before completion

            t.start();
            t.complete();

            assertTrue(allEvents.getCount() == 4, "Expected 4 events (REQUESTED, ACCEPTED, STARTED, COMPLETED).");
            assertTrue(completedOnly.getCount() == 0, "Detached observer should receive 0 COMPLETED events.");
        });

        scoredTest("Observer: COMPLETED event contains non-null fare matching returned fare", 9, () -> {
            Trip t = new Trip("Casey", "A", "B", 2.0, standard);

            final List<TripEvent> seen = new ArrayList<>();
            t.addObserver(seen::add);

            t.request();
            t.accept("Driver6");
            t.start();
            double returnedFare = t.complete();

            TripEvent last = seen.getLast();
            assertTrue(last.getType() == TripEventType.COMPLETED, "Last event should be COMPLETED");
            assertTrue(last.getFare() != null, "COMPLETED event fare should be non-null");
            assertEquals(returnedFare, last.getFare(), "Event fare should match returned fare");
        });

        scoredTest("Observer: snapshot safety (remove observer during notify does not crash)", 6, () -> {
            Trip t = new Trip("Jordan", "S", "T", 6.0, standard);

            CountingObserver counter = new CountingObserver();
            t.addObserver(counter);

            t.addObserver(new RemovesOtherObserverOnAccepted(t, counter));

            t.request();
            t.accept("Driver7");
            t.start();
            t.complete();

            assertTrue(t.getStatus() == TripStatus.COMPLETED, "Trip should still complete successfully.");
        });

        // ============================================================
        // GUARDRAILS — 7 points (checked exceptions only)
        // ============================================================

        scoredExpectThrows("Guardrail: accept() before request() throws", 2, IllegalTransitionException.class, () -> {
            Trip t = new Trip("Pat", "A", "B", 3.0, standard);
            t.accept("DriverX");
        });

        scoredExpectThrows("Guardrail: start() before accept() throws", 2, IllegalTransitionException.class, () -> {
            Trip t = new Trip("Pat", "A", "B", 3.0, standard);
            t.request();
            t.start();
        });

        scoredExpectThrows("Guardrail: complete() before start() throws", 2, IllegalTransitionException.class, () -> {
            Trip t = new Trip("Pat", "A", "B", 3.0, standard);
            t.request();
            t.accept("DriverY");
            t.complete();
        });

        scoredExpectThrows("Guardrail: reject distanceMiles <= 0", 1, InvalidParameterException.class, () ->
                new Trip("Pat", "A", "B", 0.0, standard));

        // ------------------------------------------------------------
        // Score summary
        // ------------------------------------------------------------
        System.out.println("\n==================================================");
        System.out.println("  SCORE SUMMARY");
        System.out.println("==================================================");
        System.out.println("SCORE: " + earned + " of " + possible);

    }
}
