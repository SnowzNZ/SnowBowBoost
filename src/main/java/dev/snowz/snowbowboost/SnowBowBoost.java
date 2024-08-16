package dev.snowz.snowbowboost;

import dev.snowz.snowbowboost.commands.SBBReloadCommand;
import dev.snowz.snowbowboost.listeners.BowShoot;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SnowBowBoost extends JavaPlugin {

    private static SnowBowBoost instance;

    public static SnowBowBoost getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(new BowShoot(), this);

        Objects.requireNonNull(getCommand("sbbreload")).setExecutor(new SBBReloadCommand());
    }
}
