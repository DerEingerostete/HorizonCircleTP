package de.dereingerostete.circletp.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class LocationUtils {

    public static void setYawToCenter(@NotNull Location location, double centerX, double centerZ) {
        Vector toCenterVector = new Vector(centerX - location.getX(), 0.0, centerZ - location.getZ());
        double x = toCenterVector.getX();
        double z = toCenterVector.getZ();
        double pi2 = 2 * Math.PI;
        double theta = Math.atan2(-x, z);
        float yaw = (float) Math.toDegrees((theta + pi2) % pi2);
        location.setYaw(yaw);
    }

}
