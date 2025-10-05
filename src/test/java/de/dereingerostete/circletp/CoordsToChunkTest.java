package de.dereingerostete.circletp;

import de.dereingerostete.circletp.util.LocationUtils;

public class CoordsToChunkTest {

    public static void main(String[] args) {
        int normalCords = 12;
        int chunkCords = LocationUtils.toChunkCoordinate(normalCords);
        System.out.println("normalCords: " + normalCords);
        System.out.println("chunkCords: " + chunkCords);
    }

}
