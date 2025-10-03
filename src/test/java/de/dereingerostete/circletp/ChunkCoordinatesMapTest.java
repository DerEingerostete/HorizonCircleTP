package de.dereingerostete.circletp;

import de.dereingerostete.circletp.util.CircleUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChunkCoordinatesMapTest {

    public static void main(String[] args) {
        int centerX = 0;
        int centerZ = 0;
        int radius = 300;
        int playerCount = 1000;

        // Generate points and sort them into chunks (so we don't load chunks multiple times)
        List<Point2D.Double> rawPoints = CircleUtils.pointsOnCircle(centerX, centerZ, radius, playerCount);
        Map<Point2D.Double, List<Point2D.Double>> chunkSortedCoordinates = new HashMap<>();
        for (Point2D.Double point : rawPoints) {
            int pointX = (int) Math.ceil(point.getX());
            int pointZ = (int) Math.ceil(point.getY());

            int chunkX = pointX >> 4;
            int chunkZ = pointZ >> 4;

            Point2D.Double chunkPoint = new Point2D.Double(chunkX, chunkZ);
            List<Point2D.Double> coordinates = chunkSortedCoordinates.get(chunkPoint);
            if (coordinates == null) {
                coordinates = new ArrayList<>();
                coordinates.add(point);
                chunkSortedCoordinates.put(chunkPoint, coordinates);
            } else {
                coordinates.add(point);
            }
        }

        chunkSortedCoordinates.forEach((chunkPoint, coordinates) -> {
            String formattedCoords = coordinates.stream()
                    .map(p -> "[" + p.getX() + ", " + p.getY() + "]")
                    .collect(Collectors.joining(", "));
            System.out.println("Chunk " + chunkPoint.getX() + "," + chunkPoint.getY() + ": (" + coordinates.size() + ") " + formattedCoords);
        });
    }

}
