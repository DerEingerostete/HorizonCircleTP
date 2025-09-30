package de.dereingerostete.circletp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CirclePointsRenderer {

    /** Generate `count` points evenly around a circle (center cx,cy, given radius). */
    public static List<Point2D.Double> pointsOnCircle(double cx, double cy, double radius, int count) {
        List<Point2D.Double> pts = new ArrayList<>();
        if (count <= 0) return pts;
        double r = Math.abs(radius);

        // Special-case single point: put it at start angle's midpoint (here we choose 0).
        if (count == 1) {
            double angle = 0.0;
            pts.add(new Point2D.Double(cx + r * Math.cos(angle), cy + r * Math.sin(angle)));
            return pts;
        }

        double twoPi = 2.0 * Math.PI;
        double step = twoPi / count;
        for (int i = 0; i < count; i++) {
            double angle = i * step; // startAngle = 0 (pointing +X), CCW
            double x = cx + r * Math.cos(angle);
            double y = cy + r * Math.sin(angle);
            pts.add(new Point2D.Double(x, y));
        }
        return pts;
    }

    /** Convenience: compute recommended count so adjacent arc length >= minArcLength */
    public static int countForMinSpacing(double radius, double minArcLength) {
        if (minArcLength <= 0) throw new IllegalArgumentException("minArcLength must be >0");
        double r = Math.abs(radius);
        double circumference = 2.0 * Math.PI * r;
        int count = (int) Math.floor(circumference / minArcLength);
        return Math.max(1, count);
    }

    public static void main(String[] args) throws IOException {
        // -- Configurable parameters (change these or pass a single integer arg to control count) --
        double cx = 0.0;           // logical center X (world coords)
        double cy = 0.0;           // logical center Y (world coords)
        double radius = 325;     // logical radius (world coords)
        int count = 128;            // default number of points to plot
        int imageW = 900;          // image width in pixels
        int imageH = 900;          // image height in pixels
        int padding = 40;          // pixels padding around the drawn world

        // If user supplied a command-line integer, use it as 'count'
        if (args.length >= 1) {
            try {
                count = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Arg 0 not an integer; using default count = " + count);
            }
        }

        // Generate points
        List<Point2D.Double> pts = pointsOnCircle(cx, cy, radius, count);

        // Create image and Graphics2D
        BufferedImage img = new BufferedImage(imageW, imageH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            // Quality rendering hints
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Fill background
            g.setPaint(Color.WHITE);
            g.fillRect(0, 0, imageW, imageH);

            // Determine world bounding box for the part we need to show (circle)
            double worldMinX = cx - radius;
            double worldMaxX = cx + radius;
            double worldMinY = cy - radius;
            double worldMaxY = cy + radius;
            double bboxW = worldMaxX - worldMinX;
            double bboxH = worldMaxY - worldMinY;

            // Compute scale: world units -> pixels
            double scaleX = (imageW - 2.0 * padding) / bboxW;
            double scaleY = (imageH - 2.0 * padding) / bboxH;
            double scale = Math.min(scaleX, scaleY);

            // Helper conversions
            final double imgH = imageH;
            java.util.function.DoubleUnaryOperator worldToPixelX = (wx) -> padding + (wx - worldMinX) * scale;
            java.util.function.DoubleUnaryOperator worldToPixelY = (wy) -> imgH - (padding + (wy - worldMinY) * scale);

            // Draw circle outline
            double centerPx = worldToPixelX.applyAsDouble(cx);
            double centerPy = worldToPixelY.applyAsDouble(cy);
            double radiusPx = radius * scale;
            g.setStroke(new BasicStroke(2f));
            g.setPaint(Color.BLACK);
            Shape circle = new Ellipse2D.Double(centerPx - radiusPx, centerPy - radiusPx, radiusPx * 2, radiusPx * 2);
            g.draw(circle);

            // Draw center crosshair
            g.setStroke(new BasicStroke(1.5f));
            g.setPaint(Color.DARK_GRAY);
            int crossSize = 8;
            g.drawLine((int)(centerPx - crossSize), (int)centerPy, (int)(centerPx + crossSize), (int)centerPy);
            g.drawLine((int)centerPx, (int)(centerPy - crossSize), (int)centerPx, (int)(centerPy + crossSize));

            // Draw each point as a filled disc and label angle index
            int dotPixelRadius = 6;
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            for (int i = 0; i < pts.size(); i++) {
                Point2D.Double p = pts.get(i);
                double px = worldToPixelX.applyAsDouble(p.x);
                double py = worldToPixelY.applyAsDouble(p.y);

                // point fill
                g.setPaint(Color.RED);
                Ellipse2D.Double dot = new Ellipse2D.Double(px - dotPixelRadius, py - dotPixelRadius,
                        2 * dotPixelRadius, 2 * dotPixelRadius);
                g.fill(dot);

                // outline
                g.setPaint(Color.BLACK);
                g.setStroke(new BasicStroke(1f));
                g.draw(dot);

                // optional label (index)
                String label = Integer.toString(i);
                g.drawString(label, (float)(px + dotPixelRadius + 2), (float)(py - dotPixelRadius - 2));
            }

            // A small legend and info
            g.setPaint(Color.BLACK);
            g.drawString(String.format("center=(%.1f, %.1f) radius=%.1f count=%d", cx, cy, radius, count),
                    10, 16);
        } finally {
            g.dispose();
        }

        // Write PNG
        File out = new File("circle_points.png");
        ImageIO.write(img, "png", out);
        System.out.println("Wrote image: " + out.getAbsolutePath());
    }
}
