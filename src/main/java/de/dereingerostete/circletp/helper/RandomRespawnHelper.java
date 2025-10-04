package de.dereingerostete.circletp.helper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RandomRespawnHelper {
    private final @NotNull Random random;
    private final @Nullable World world;
    private final int minDistance;
    private final int maxDistance;

    public RandomRespawnHelper(@NotNull ConfigurationSection section) {
        String worldName = section.getString("world", null);
        this.world = worldName == null ? null : Bukkit.getWorld(worldName);

        int maxCoordinates = Integer.MAX_VALUE;
        WorldBorder border = world == null ? null : world.getWorldBorder();
        if (border != null) {
            Location centerLocation = border.getCenter();
            int size = (int) Math.floor(border.getSize() / 2D);
            maxCoordinates = centerLocation.getBlockX() + size;
        }

        this.minDistance = Math.min(section.getInt("min", 5_000), maxCoordinates);
        this.maxDistance = Math.min(section.getInt("max", 25_000), maxCoordinates);
        this.random = new Random();
    }

    @Nullable
    public Location generateRandomLocation() {
        if (world == null) return null;
        int x = random.nextInt(minDistance, maxDistance);
        int z  = random.nextInt(minDistance, maxDistance);
        if (random.nextBoolean()) x = -x;
        if (random.nextBoolean()) z = -z;
        return new Location(world, x, 0, z);
    }

}
