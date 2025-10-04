package de.dereingerostete.circletp.listener;

import de.dereingerostete.circletp.helper.RespawnHelper;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class JoinListener implements Listener {
    private final @NotNull RespawnHelper helper;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) { // Only new players
            helper.teleportRandomRespawn(player);
        }
    }

}
