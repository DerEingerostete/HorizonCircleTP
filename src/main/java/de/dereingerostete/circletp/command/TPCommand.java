package de.dereingerostete.circletp.command;

import de.dereingerostete.circletp.CircleTPPlugin;
import de.dereingerostete.circletp.command.util.SimpleCommand;
import de.dereingerostete.circletp.util.CircleUtils;
import de.dereingerostete.circletp.util.RadiusUtils;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import lombok.extern.slf4j.Slf4j;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class TPCommand extends SimpleCommand {

    public TPCommand() {
        super("circle-tp", true);
        setPermission("event.circle-tp");
        setDescription("Teleports all players in a circle around a certain point");
        setUsage("/circle-tp <circleX> <circleZ>");
        permissionMessage(Component.text("You dont have the permissions to do this!", NamedTextColor.RED));
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

        List<Player> allPlayers = List.copyOf(Bukkit.getAllOnlinePlayers());
        int playerCount = allPlayers.size();
        player.sendMessage(Component.text("§7Preparing teleportation of §c" + playerCount + "§7 players"));

        double radius = Math.ceil(RadiusUtils.radiusForArcSpacing(playerCount, 3));
        List<Point2D.Double> points = CircleUtils.pointsOnCircle(centerX, centerZ, radius, playerCount);
        World world = player.getWorld();

        Plugin plugin = CircleTPPlugin.getInstance();
        RegionScheduler regionScheduler = Bukkit.getRegionScheduler();

        CountDownLatch latch = new CountDownLatch(points.size());
        Map<Player, Location> playerLocations = new HashMap<>();
        for (int i = 0; i < points.size(); i++) {
            Point2D.Double point = points.get(i);
            int pointX = (int) Math.ceil(point.getX());
            int pointZ = (int) Math.ceil(point.getY());

            int chunkX = pointX >> 4;
            int chunkZ = pointZ >> 4;
            Player targetPlayer = allPlayers.get(i);
            regionScheduler.run(plugin, world, chunkX, chunkZ, task -> {
                if (task.isCancelled()) return;

                int y = world.getHighestBlockYAt(pointX, pointZ, HeightMap.MOTION_BLOCKING);
                Location location = new Location(world, pointX, y, pointZ);
                playerLocations.put(targetPlayer, location);
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException exception) {
            log.warn("Failed to for all locations to be loaded", exception);
            sendMessage(player, "Failed to load all locations for teleport. Aborting", NamedTextColor.RED);
            return;
        }

        sendMessage(player, "Teleporting all players now", NamedTextColor.GRAY);
        for (Map.Entry<Player, Location> entry : playerLocations.entrySet()) {
            Player targetPlayer = entry.getKey();
            Location location = entry.getValue();

            EntityScheduler entityScheduler = targetPlayer.getScheduler();
            entityScheduler.run(plugin, ignored -> {
                sendMessage(targetPlayer, "Teleporting...", NamedTextColor.GRAY);
                targetPlayer.teleportAsync(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }, null);
        }
    }

    private void sendMessage(@NotNull CommandSender sender, @NotNull String message, @NotNull TextColor color) {
        sender.sendMessage(Component.text(message, color));
    }

}
