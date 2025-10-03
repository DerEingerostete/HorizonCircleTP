package de.dereingerostete.circletp.command;

import de.dereingerostete.circletp.command.util.SimpleCommand;
import de.dereingerostete.circletp.helper.RespawnHelper;
import de.dereingerostete.circletp.util.CircleUtils;
import de.dereingerostete.circletp.util.LocationUtils;
import de.dereingerostete.circletp.util.RadiusUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static de.dereingerostete.circletp.CircleTPPlugin.log;

public class TPCommand extends SimpleCommand {
    private static final double MIN_RADIUS = 300; // Minimum radius we always require
    private final @NotNull RespawnHelper respawnHelper;

    public TPCommand(@NotNull RespawnHelper respawnHelper) {
        super("circle-tp", true);
        this.respawnHelper = respawnHelper;

        setPermission("event.circle-tp");
        setDescription("Teleports all players in a circle around a certain point. It also sets the players respawn point");
        setUsage("/circle-tp <circleX> <circleZ>");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args, int arguments) {
        sendMessage(sender, "You need to be a player to perform this command!", NamedTextColor.RED);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String[] args, int arguments) {
        if (args.length == 0) {
            sendMessage(player, "Wrong usage. Use /circle-tp <circleX> <circleZ>", NamedTextColor.RED);
            return;
        }

        double centerX, centerZ;
        try {
            centerX = Double.parseDouble(args[0]);
            centerZ = Double.parseDouble(args[1]);
        } catch (NumberFormatException exception) {
            sendMessage(player, "Failed to parse coordinates!", NamedTextColor.RED);
            return;
        }

        List<Player> allPlayers = List.copyOf(Bukkit.getOnlinePlayers());
        int playerCount = allPlayers.size();
        player.sendMessage(Component.text("§7Preparing teleportation of §c" + playerCount + "§7 players"));

        double radius = Math.ceil(RadiusUtils.radiusForArcSpacing(playerCount, 3));
        radius = Math.max(radius, MIN_RADIUS); // Make sure to enforce min radius

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

        // Load positions and sort them to players
        int i = 0;
        World world = player.getWorld();
        List<CompletableFuture<?>> chunkFutures = new ArrayList<>();
        Map<Player, Location> playerLocations = new HashMap<>();
        for (Map.Entry<Point2D.Double, List<Point2D.Double>> entry : chunkSortedCoordinates.entrySet()) {
            List<Point2D.Double> coordinates = entry.getValue();
            Point2D.Double chunkPoint = entry.getKey();
            int chunkX = (int) chunkPoint.getX();
            int chunkZ = (int) chunkPoint.getY();

            for (Point2D.Double point : coordinates) {
                int pointX = (int) point.getX();
                int pointZ = (int) point.getY();

                Player targetPlayer = allPlayers.get(i);
                i++;

                CompletableFuture<Void> future = world.getChunkAtAsync(chunkX, chunkZ).thenAccept(chunk -> {
                    int y = world.getHighestBlockYAt(pointX, pointZ, HeightMap.MOTION_BLOCKING);
                    Location location = new Location(world, pointX, y + 1.5, pointZ).toCenterLocation();
                    LocationUtils.setYawToCenter(location, centerX, centerZ);
                    playerLocations.put(targetPlayer, location);
                });
                chunkFutures.add(future);
            }
        }

        try {
            sendMessage(player, "Please wait a moment before all locations are loaded",  NamedTextColor.GRAY);
            CompletableFuture<?>[] futures = chunkFutures.toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
        } catch (RuntimeException exception) {
            log.warn("Failed to wait for all locations to be loaded", exception);
            sendMessage(player, "Failed to load all locations for teleport. Aborting", NamedTextColor.RED);
            return;
        }

        sendMessage(player, "Teleporting all players now", NamedTextColor.GRAY);
        for (Map.Entry<Player, Location> entry : playerLocations.entrySet()) {
            Player targetPlayer = entry.getKey();
            Location location = entry.getValue();

           sendMessage(targetPlayer, "Teleporting...", NamedTextColor.GRAY);
           targetPlayer.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        respawnHelper.generateRespawnLocations(world, centerX, centerZ, radius);
        respawnHelper.setEnabled(true);
        sendMessage(player, "Respawning around the center is now active", NamedTextColor.DARK_GREEN);
    }

    private void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull TextColor color) {
        sender.sendMessage(Component.text(message, color));
    }

}
