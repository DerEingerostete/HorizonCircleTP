package de.dereingerostete.circletp;

import de.dereingerostete.circletp.command.TPCommand;
import de.dereingerostete.circletp.command.util.SimpleCommand;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Slf4j
public class CircleTPPlugin extends JavaPlugin {
    public static Logger LOGGER;
    private static @Getter Plugin instance;

    @Override
    public void onEnable() {
        LOGGER = getSLF4JLogger();
        instance = this;

        registerCommand(new TPCommand());
        LOGGER.info("Plugin enabled");
    }

    private void registerCommand(@NotNull SimpleCommand command) {
        command.register("circletp");
    }

    @Override
    public void onDisable() {
        LOGGER.info("Plugin disabled");
    }

}
