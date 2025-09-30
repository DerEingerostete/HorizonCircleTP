package de.dereingerostete.circletp.util;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// Fully made with ChatGPT, I needed to finish this in a couple of hours
public class CircleUtils {

    private CircleUtils() {}

    /**
     * Returns 'count' points evenly spaced on the circumference of a circle.
     * Full circle is used (2*PI). If count <= 0 returns an empty list.
     *
     * @param cx center X
     * @param cy center Y
     * @param radius circle radius (negative radius is accepted and treated as abs(radius))
     * @param count number of points to generate
     * @return List of Point2D.Double with coordinates on the circle
     */
    public static List<Point2D.Double> pointsOnCircle(double cx, double cy, double radius, int count) {
        return pointsOnCircle(cx, cy, radius, count, 0.0, 2 * Math.PI, false);
    }

    /**
     * More flexible: generate points across an arc (sweepAngle). If includeEndpoints==true
     * and sweepAngle < 2*PI, the first and last point will be at the arc endpoints (useful
     * for open arcs). If includeEndpoints==false (default behavior) points are spaced by
     * step = sweepAngle / count (wrap-around for full circle).
     * <p>
     * If count == 1, the single point is placed at the arc center (start + sweep/2).
     *
     * @param cx center X
     * @param cy center Y
     * @param radius radius
     * @param count number of points
     * @param startAngleRadians starting angle in radians (0 => +X axis)
     * @param sweepAngleRadians how large the arc is in radians (2*PI for a full circle)
     * @param includeEndpoints whether to force endpoints inclusion for partial arcs
     * @return list of points
     */
    public static List<Point2D.Double> pointsOnCircle(double cx, double cy, double radius,
                                                      int count, double startAngleRadians,
                                                      double sweepAngleRadians, boolean includeEndpoints) {
        List<Point2D.Double> pts = new ArrayList<>();
        if (count <= 0) return pts;

        double r = Math.abs(radius);

        // Special-case single point: place in the middle of the arc
        if (count == 1) {
            double angle = startAngleRadians + sweepAngleRadians / 2.0;
            pts.add(new Point2D.Double(cx + r * Math.cos(angle), cy + r * Math.sin(angle)));
            return pts;
        }

        double step;
        if (includeEndpoints && sweepAngleRadians < 2 * Math.PI) {
            // include both start and end: n points => step = sweep/(n-1)
            step = sweepAngleRadians / (count - 1);
        } else {
            // default: divide the sweep into 'count' equal angles (wrap-around for full circle)
            step = sweepAngleRadians / count;
        }

        for (int i = 0; i < count; i++) {
            double angle = startAngleRadians + i * step;
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            pts.add(new Point2D.Double(x, y));
        }
        return pts;
    }

}
