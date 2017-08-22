package io.mymc.redhavensurvival;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DeathPosition extends JavaPlugin implements Listener {
    private File configf;
    private File locf;
    private FileConfiguration config;
    private FileConfiguration locations;

    public DeathPosition() {
    }

    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }

        try {
            this.createFiles();
        } catch (InvalidConfigurationException exc) {
            exc.printStackTrace();
        }

        // this.getConfig().addDefault("Enable-Chests", Boolean.valueOf(false));
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("Redhaven plugin has been enabled!");
    }

    public void onDisable() {
        this.getLogger().info("Disabling Redhaven plugin.");
        try {
            this.getLocationsConfig().save(this.locf);
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        this.getLogger().info("Redhaven plugin has been disabled!");
    }

    private void createFiles() throws InvalidConfigurationException {
        this.configf = new File(this.getDataFolder(), "config.yml");
        this.locf = new File(this.getDataFolder(), "locations.yml");
        if (!this.configf.exists()) {
            this.configf.getParentFile().mkdirs();
            this.saveResource("config.yml", false);
        }

        if (!this.locf.exists()) {
            this.locf.getParentFile().mkdirs();
            this.saveResource("locations.yml", false);
        }

        this.config = new YamlConfiguration();
        this.locations = new YamlConfiguration();

        try {
            this.config.load(this.configf);
            this.locations.load(this.locf);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    @EventHandler(
            priority = EventPriority.LOWEST,
            ignoreCancelled = true
    )
    public void onPlayerDeathLowest(PlayerDeathEvent event) {
        String deathMessage = event.getDeathMessage();
        if (deathMessage != null) {
            Player player = event.getEntity();
            this.setPlayerDeathLocation(player);
            this.sendDeathPositionMessage(player, player.getLocation());
        }
    }

    private void sendDeathPositionMessage(Player player, Location deathLocation) {
        player.sendMessage(this.getPrefixedMessage("Your last death was at:"));
        player.sendMessage(
                String.format("    X: %d, Y: %d, Z: %d",
                        Math.round(deathLocation.getX()),
                        Math.round(deathLocation.getY()),
                        Math.round(deathLocation.getZ())
                )
        );
    }

    private FileConfiguration getLocationsConfig() {
        return this.locations;
    }

    private Location getPlayerDeathLocation(Player player) {
        Map<String, Object> configLocation = (Map<String, Object>) this.getLocationsConfig().get(player.getName());
        return configLocation == null ? null : Location.deserialize(configLocation);
    }

    private void setPlayerDeathLocation(Player player) {
        this.getLocationsConfig().set(player.getName(), player.getLocation().serialize());
    }

    private String getPrefixedMessage(String message) {
        return ChatColor.GOLD + "[" + ChatColor.RED + "Redhaven" + ChatColor.GOLD + "]" + ChatColor.WHITE + " " + message;
    }
}
