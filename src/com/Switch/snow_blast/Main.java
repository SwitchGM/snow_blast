package com.Switch.snow_blast;

import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
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

    private HashMap<Player, Long> snowballHitMessageMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        setupConfigFile();
    }

    @EventHandler
    public void onThrow(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            FileConfiguration config = this.getConfig();
            Player target = (Player) event.getHitEntity();
            Player shooter = (Player) event.getEntity().getShooter();
            if (event.getHitEntity() instanceof Player && event.getEntity().getShooter() instanceof Player) {
                snowballHitMessage(target, shooter);
            } else { // player hit by snow golem
                String targetHitByGolemMessage = config.getString("hit_by_snow_golem");
                target.sendMessage(translateChatColourString(targetHitByGolemMessage));
            }
        }
    }

    private void snowballHitMessage(Player target, Player shooter) {
        FileConfiguration config = this.getConfig();
        if (snowballHitMessageMap.containsKey(target)) {
            int cooldown = config.getInt("cooldown");
            if (snowballHitMessageMap.get(target) + (cooldown * 1000) <= System.currentTimeMillis()) {
                snowballHitMessageMap.remove(target);
            }
        } else {
            snowballHitMessageMap.put(shooter, System.currentTimeMillis());
            String snowballTargetMessage = config.getString("snowball_target_message");
            String snowballShooterMessage = config.getString("snowball_shooter_message");
            target.sendMessage(translateChatColourString(replacePlaceholders(snowballTargetMessage, target, shooter)));
            shooter.sendMessage(translateChatColourString(replacePlaceholders(snowballShooterMessage, target, shooter)));
        }
    }

    private String replacePlaceholders(String string, Player target, Player shooter) {
        String targetPlaceholder = "%target%";
        String shooterPlaceholder = "%shooter%";
        String newString = string.replaceAll(targetPlaceholder, target.getDisplayName());

        return newString.replaceAll(shooterPlaceholder, shooter.getDisplayName());
    }

    private String translateChatColourString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private void setupConfigFile() {
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        config.addDefault("cooldown", 3);
        config.addDefault("snowball_target_message", "&9%shooter% hit you");
        config.addDefault("snowball_shooter_message", "&9You hit %target%");

        saveConfig();

    }
}

