package de.dereingerostete.circletp.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import de.dereingerostete.circletp.helper.RespawnHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import static de.dereingerostete.circletp.CircleTPPlugin.log;

public class RespawnListener implements Listener {
    private final @NotNull RespawnHelper helper;
    private final @NotNull PotionEffect resistanceEffect;
    private final @NotNull PotionEffect fireResistanceEffect;

    public RespawnListener(@NotNull RespawnHelper helper) {
        this.helper = helper;
        this.resistanceEffect = new PotionEffect(
                PotionEffectType.RESISTANCE,
                20 * 4,
                254,
                false, false
        );
        this.fireResistanceEffect = new PotionEffect(
                PotionEffectType.FIRE_RESISTANCE,
                20 * 10,
                1,
                false, false
        );
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRespawn(@NotNull PlayerRespawnEvent event) {
        boolean userDefined = event.isBedSpawn() || event.isAnchorSpawn();
        if (userDefined) return; // User set its own spawn point. Use this instead

        // No enabled -> Use random respawn
        Player player = event.getPlayer();
        if (!helper.isEnabled()) {
            // We set a temporary location just to make sure player does not respawn near center
            World world = player.getWorld();
            Location temporaryLocation = new Location(world, 1000, 100_000, 1000);
            event.setRespawnLocation(temporaryLocation);

            helper.teleportRandomRespawn(player);
            return;
        }

        Location location = helper.findCircleRespawn();
        if (location != null) {
            event.setRespawnLocation(location);
            player.sendMessage(Component.text("It appears you haven't set a respawn point since the teleport.", NamedTextColor.GRAY));
            player.sendMessage(Component.text("You'll now respawn near the crystal.", NamedTextColor.DARK_PURPLE));
        } else {
            log.warn("No respawn location for player {} found", player.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPostRespawn(@NotNull PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("event.circle-tp.ignored")) { // Skilled players do not get this
            player.addPotionEffect(resistanceEffect);
            player.addPotionEffect(fireResistanceEffect);
        }
    }

}
