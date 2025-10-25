package de.dereingerostete.circletp.listener;

import de.dereingerostete.circletp.helper.RespawnHelper;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

@RequiredArgsConstructor
public class JoinListener implements Listener {
    private final @NotNull RespawnHelper helper;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        if (player.hasPlayedBefore()) return; // Only new players

        Location location = helper.isCircleTPEnabled() ?
                helper.findCircleRespawn() :    // Random location around circle
                helper.getRandomRespawn();      // Random respawn location

        if (location != null) {
            event.setSpawnLocation(location);
        }
    }

}
