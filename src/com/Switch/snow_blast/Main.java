package com.Switch.snow_blast;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * Created by SwitchGM,
 * Project : snow_blast
 * Package : com.Switch.snow_blast
 * ©2018 JoeAmphlett
 */
public class Main extends JavaPlugin implements Listener {

    private HashMap<Player, Long> snowballHitMap = new HashMap<>();
    private HashMap<Player, Long> freezePlayerMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        setupConfigFile();
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Entity target = event.getHitEntity();
            Entity shooter = (Entity) event.getEntity().getShooter();
            if (target instanceof Player) {
                FileConfiguration config = this.getConfig();
                int action = (int) (Math.random() * 100);
                if (action <= config.getInt("snowball_freeze.chance")) { // this is a terrible way of doing chances...

                }
                snowBallHitMessage((Player) target, shooter);
            }
        }
    }

    private void snowBallHitMessage(Player target, Entity shooterEntity) {
        FileConfiguration config = this.getConfig();
        if (shooterEntity instanceof Snowman) {
            String golemMessage = config.getString("snowball_hit.golem_message");
            target.sendMessage(translateColorCodes(golemMessage));
        }
        else if (shooterEntity instanceof Player) {
            Player shooter = (Player) shooterEntity;
            String targetMessage = config.getString("snowball_hit.target_message");
            String shooterMessage = config.getString("snowball_hit.shooter_message");
            target.sendMessage(translateColorCodes(translatePlaceholder(targetMessage, target, shooter)));
            target.sendMessage(translateColorCodes(translatePlaceholder(shooterMessage, target, shooter)));
        }
    }

    private String translatePlaceholder(String string, Player target, Player shooter) {
        String targetPlaceholder = "%target%";
        String shooterPlaceholder = "%shooter%";
        if (string.contains("%target%")) {
            string = string.replaceAll(targetPlaceholder, target.getDisplayName());
        }
        if (string.contains("%shooter")) {
            string = string.replaceAll(shooterPlaceholder, shooter.getDisplayName());
        }
        return string;
    }

    private String translateColorCodes(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private void setupConfigFile() {
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        config.addDefault("snowball_hit.cooldown", 3);
        config.addDefault("snowball_hit.target_message", "&9%shooter% hit you");
        config.addDefault("snowball_hit.shooter_message", "&9You hit %target%");
        config.addDefault("snowball_hit.golem_message", "&9A snowman hit you");

        config.addDefault("snowball_freeze.chance", 20);
        config.addDefault("snowball_freeze.cooldown", 3);
        config.addDefault("snowball_freeze.target_message", "&9%shooter% froze you");
        config.addDefault("snowball_freeze.shooter_message", "&9You froze %target%");
        config.addDefault("snowball_freeze.golem_message", "&9A snowman froze you");

        saveConfig();

    }
}

