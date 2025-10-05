package de.dereingerostete.circletp.helper;

import de.dereingerostete.circletp.CircleTPPlugin;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static de.dereingerostete.circletp.CircleTPPlugin.log;

public class RandomRespawnHelper {
    private final @NotNull List<Location> respawnLocations;
    private final @NotNull Random random;

    public RandomRespawnHelper(@NotNull ConfigurationSection section) {
        this.respawnLocations = new ArrayList<>();
        this.random = new Random();

        String worldName = section.getString("world", null);
        World world = worldName == null ? null : Bukkit.getWorld(worldName);

        int maxCoordinates = Integer.MAX_VALUE;
        WorldBorder border = world == null ? null : world.getWorldBorder();
        if (border != null) {
            Location centerLocation = border.getCenter();
            int size = (int) Math.floor(border.getSize() / 2D);
            maxCoordinates = centerLocation.getBlockX() + size;
        }

        int maxChunks = section.getInt("maxChunks", 1800);
        int minDistance = Math.min(section.getInt("min", 350), maxCoordinates);
        int maxDistance = Math.min(section.getInt("max", 2_000), maxCoordinates);
        if (world != null) loadRespawnLocations(world, minDistance, maxDistance, maxChunks);
    }

    @Nullable
    public Location getRandomLocation() {
        if (respawnLocations.isEmpty()) return null;

        int size = respawnLocations.size();
        int index = random.nextInt(size);
        return respawnLocations.get(index);
    }

    private void loadRespawnLocations(@NotNull World world, int minDistance, int maxDistance, int maxChunks) {
        log.info("Generating random respawn chunks");

        // First generate some random chunks
        Set<Point2D.Double> chunkLocations = new HashSet<>();
        for (int i = 0; i < maxChunks; i++) {
            int x = random.nextInt(minDistance, maxDistance);
            int z  = random.nextInt(minDistance, maxDistance);
            if (random.nextBoolean()) x = -x;
            if (random.nextBoolean()) z = -z;

            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            Point2D.Double chunkPoint = new Point2D.Double(chunkX, chunkZ);
            chunkLocations.add(chunkPoint);
        }

        // Then go through each chunk
        log.warn("Loading {} chunks for respawn locations", chunkLocations.size());
        CountDownLatch countDownLatch = new CountDownLatch(maxChunks);
        for (Point2D.Double chunkPoint : chunkLocations) {
            int chunkX = (int) chunkPoint.getX();
            int chunkZ = (int) chunkPoint.getY();
            world.getChunkAtAsync(chunkX, chunkZ).thenAccept(chunk -> {
                // For each chunk get multiple locations and their highest y
                // Also check if it's a solid block underneath

                int startX = random.nextInt(0, 8);
                int endX = random.nextInt(9, 16);

                int startZ = random.nextInt(0, 8);
                int endZ = random.nextInt(9, 16);
                for (int chunkPartX = startX; chunkPartX < endX; chunkPartX++) {
                    for (int chunkPartZ = startZ; chunkPartZ < endZ; chunkPartZ++) {
                        ChunkSnapshot snapshot = chunk.getChunkSnapshot();
                        int y = snapshot.getHighestBlockYAt(chunkPartX, chunkPartZ);

                        // Don't add if there is water
                        Material blockType = snapshot.getBlockType(chunkPartX, y - 1, chunkPartZ);
                        if (blockType.isSolid()) {
                            double fullX = chunkX * 16 + chunkPartX + 0.5;
                            double fullZ = chunkZ * 16 + chunkPartZ + 0.5;
                            Location location = new Location(world, fullX, y + 1.5, fullZ);
                            respawnLocations.add(location);
                        }

                        countDownLatch.countDown();
                    }
                }
            });
        }

        AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
        scheduler.runNow(CircleTPPlugin.getInstance(), task -> {
            try {
                countDownLatch.await();
                log.info("Successfully loaded {} respawn locations", respawnLocations.size());
            } catch (InterruptedException exception) {
                log.warn("Failed to wait for all chunks to load to notify (This is not important)", exception);
            }
        });
    }

}
