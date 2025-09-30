package de.dereingerostete.circletp.util;

/**
 * Utility methods to compute a circle radius given number of points and desired spacing.
 * Fully made with ChatGPT, I needed to finish this in a couple of hours
 */
public class RadiusUtils {

    private RadiusUtils() {}

    /**
     * Compute radius so that the arc length between adjacent points around a full circle
     * (n points evenly spaced) equals the given spacing.
     * <p>
     * arcLength = (2 * PI * r) / n  =>  r = arcLength * n / (2 * PI)
     *
     * @param count number of points (n). Must be >= 1. If count == 1 returns 0.
     * @param spacing desired arc spacing (must be > 0)
     * @return radius that yields the requested arc spacing (>= 0)
     * @throws IllegalArgumentException for invalid inputs
     */
    public static double radiusForArcSpacing(int count, double spacing) {
        if (count < 1) throw new IllegalArgumentException("count must be >= 1");
        if (!(spacing > 0)) throw new IllegalArgumentException("spacing must be > 0");
        if (count == 1) return 0.0; // no adjacent points; choose 0 as a sensible default
        return spacing * count / (2.0 * Math.PI);
    }

    /**
     * Compute radius so that the straight-line (chord) distance between adjacent points
     * around a full circle (n points evenly spaced) equals the given spacing.
     * <p>
     * chord = 2 * r * sin(PI / n)  =>  r = chord / (2 * sin(PI / n))
     *
     * @param count number of points (n). Must be >= 2 for a meaningful chord.
     * @param spacing desired chord spacing (must be > 0)
     * @return radius that yields the requested chord spacing (>= 0)
     * @throws IllegalArgumentException for invalid inputs
     */
    public static double radiusForChordSpacing(int count, double spacing) {
        if (count < 2) throw new IllegalArgumentException("count must be >= 2 for chord spacing");
        if (!(spacing > 0)) throw new IllegalArgumentException("spacing must be > 0");
        double angle = Math.PI / count; // half central angle
        double denom = 2.0 * Math.sin(angle);
        if (denom <= 0) throw new IllegalStateException("unexpected numerical issue computing sin(PI / count)");
        return spacing / denom;
    }

}
