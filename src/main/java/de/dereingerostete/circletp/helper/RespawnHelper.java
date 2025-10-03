package de.dereingerostete.circletp.helper;

import de.dereingerostete.circletp.util.CircleUtils;
import de.dereingerostete.circletp.util.RadiusUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static de.dereingerostete.circletp.CircleTPPlugin.log;

public class RespawnHelper {
    private @Getter @Setter boolean enabled;
    private final @NotNull List<Location> respawnLocations;

    public RespawnHelper() {
        this.enabled = false;
        this.respawnLocations = new ArrayList<>();
    }

    @Nullable
    public Location findRespawnLocation() {
        // Find fitting location
        if (respawnLocations.isEmpty()) return null; // We did not load any locations yet
        int index = ThreadLocalRandom.current().nextInt(respawnLocations.size());
        return respawnLocations.get(index);
    }

    public void generateRespawnLocations(@NotNull World world, double centerX, double centerZ, double radius) {
        respawnLocations.clear(); // Clear old locations

        // We want to have enough distance to get no overlapping chunks
        int count = RadiusUtils.countForMinSpacing(radius, 16);
        List<Point2D.Double> rawLocations = CircleUtils.pointsOnCircle(centerX, centerZ, radius, count);
        log.info("Generating respawn locations with a radius of {}", radius);

        // Convert to chunks, also removes duplicated chunks
        Set<Point2D.Double> rawChunks = rawLocations.stream().map(loc -> {
            int chunkX = (int) Math.ceil(loc.getX()) >> 4;
            int chunkZ = (int) Math.ceil(loc.getY()) >> 4;
            return new Point2D.Double(chunkX, chunkZ);
        }).collect(Collectors.toSet());

        log.info("Loading {} chunks for respawn locations", rawChunks.size());
        for (Point2D.Double chunkLocation : rawChunks) {
            int chunkX = (int) chunkLocation.getX();
            int chunkZ = (int) chunkLocation.getY();
            world.getChunkAtAsync(chunkX, chunkZ).thenAccept(chunk -> {
                // Generate locations per chunk
                for (int x = 2; x < 6; x++) {
                    for (int z = 2; z < 6; z++) {
                        int fullX = chunkX * 16 + x;
                        int fullZ = chunkZ * 16 + z;
                        double y = world.getHighestBlockYAt(fullX, fullZ, HeightMap.MOTION_BLOCKING) + 2.5;
                        Location location = new Location(world, fullX + 0.5, y, fullZ + 0.5);
                        respawnLocations.add(location);
                    }
                }
            });
        }
    }

}
