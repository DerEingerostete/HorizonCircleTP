package de.dereingerostete.circletp;

import de.dereingerostete.circletp.command.TPCommand;
import de.dereingerostete.circletp.command.util.SimpleCommand;
import de.dereingerostete.circletp.helper.RespawnHelper;
import de.dereingerostete.circletp.listener.JoinListener;
import de.dereingerostete.circletp.listener.RespawnListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class CircleTPPlugin extends JavaPlugin {
    public static Logger log;
    private static @Getter Plugin instance;

    @Override
    public void onEnable() {
        log = getSLF4JLogger();
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        RespawnHelper respawnHelper = new RespawnHelper();
        registerCommand(new TPCommand(respawnHelper));

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new RespawnListener(respawnHelper), this);
        manager.registerEvents(new JoinListener(respawnHelper), this);
        log.info("Plugin enabled");
    }

    private void registerCommand(@NotNull SimpleCommand command) {
        command.register("circletp");
    }

    @Override
    public void onDisable() {
        log.info("Plugin disabled");
    }

}
