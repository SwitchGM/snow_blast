package com.Switch.snow_blast;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * Created by SwitchGM,
 * Project : snow_blast
 * Package : com.Switch.snow_blast
 * ©2018 JoeAmphlett
 */
public class Main extends JavaPlugin implements Listener {

    private HashMap<Player, Long> hitMap = new HashMap<>();
    private HashMap<Player, Long> freezeMap = new HashMap<>();
    private HashMap<Player, Long> fireworkMap = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        setupConfigFile();
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball) {
            Entity targetEntity = event.getHitEntity();
            Entity shooterEntity = (Entity) event.getEntity().getShooter();
            int action = (int) (Math.random() * 100);
            if (targetEntity instanceof Player) {
                Player target = (Player) targetEntity;
                if (action <= actionChance("snowball_freeze.chance")) { // this is a terrible way of doing chances...
                    targetFreeze(target, shooterEntity);
                    return;
                }
                if (action <= actionChance("snowball_firework.chance")) {
                    firework(target, shooterEntity);
                    return;
                }
                snowballHit(target, shooterEntity);
            }
        }
    }

    private void snowballHit(Player target, Entity shooterEntity) {
        FileConfiguration config = this.getConfig();
        if (hitMap.containsKey(target)) {
            if (hitMap.get(target) + (config.getInt("snowball_hit.cooldown") * 1000) <= System.currentTimeMillis()) {
                hitMap.remove(target);
                return;
            } else {
                return;
            }
        }
        snowBallHitMessage(target, shooterEntity);
        hitMap.put(target, System.currentTimeMillis());
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

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (freezeMap.containsKey(player)) {
            FileConfiguration config = this.getConfig();
            if (freezeMap.get(player) + (config.getInt("snowball_freeze.cooldown") * 1000) <= System.currentTimeMillis()) {
                freezeMap.remove(player);
                Location location = player.getLocation();
                location.getBlock().setType(Material.AIR);
            } else {
                event.setCancelled(true);
            }
        }
    }

    private void targetFreeze(Player target, Entity shooterEntity) {
        if (freezeMap.containsKey(target)) {
            FileConfiguration config = this.getConfig();
            if (freezeMap.get(target) + (config.getInt("snowball_freeze.cooldown")) >= System.currentTimeMillis()) {
                freezeMap.remove(target);
            } else {
                return;
            }
        }
        freezeMap.put(target, System.currentTimeMillis());
        Location location = new Location(target.getWorld(),
                target.getLocation().getBlockX() + 0.5,
                target.getLocation().getBlockY(),
                target.getLocation().getBlockZ() + 0.5,
                target.getEyeLocation().getYaw(),
                target.getEyeLocation().getPitch());
        target.getLocation().getBlock().setType(Material.ICE);
        target.teleport(location);
        targetFreezeMessage(target, shooterEntity);
    }

    private void targetFreezeMessage(Player target, Entity shooterEntity) {
        FileConfiguration config = this.getConfig();
        if (shooterEntity instanceof Snowman) {
            String golemMessage = config.getString("snowball_freeze.golem_message");
            target.sendMessage(translateColorCodes(golemMessage));
        }
        else if (shooterEntity instanceof Player) {
            Player shooter = (Player) shooterEntity;
            String targetMessage = config.getString("snowball_freeze.target_message");
            target.sendMessage(translateColorCodes(translatePlaceholder(targetMessage, target, shooter)));
        }
    }

    private void firework(Player target, Entity shooterEntity) {
        if (fireworkMap.containsKey(target)) {
            FileConfiguration config = this.getConfig();
            if (fireworkMap.get(target) + (config.getInt("snowball_firework.cooldown") * 1000) <= System.currentTimeMillis()) {
                fireworkMap.remove(target);
            } else {
                return;
            }
        }
        fireworkMap.put(target, System.currentTimeMillis());
        Firework firework = target.getPlayer().getWorld().spawn(target.getPlayer().getLocation(), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.addEffect(FireworkEffect.builder()
                .flicker(true).trail(true).withColor(Color.RED, Color.GREEN)
                .withFade(Color.WHITE).with(FireworkEffect.Type.BALL).build());
        firework.setFireworkMeta(fireworkMeta);
        firework.detonate();
        fireworkMessage(target, shooterEntity);
    }

    private void fireworkMessage(Player target, Entity shooterEntity) {
        FileConfiguration config = this.getConfig();
        if (shooterEntity instanceof Snowman) {
            String golemMessage = config.getString("snowball_firework.golem_message");
            target.sendMessage(translateColorCodes(golemMessage));
        }
        else if (shooterEntity instanceof Player) {
            Player shooter = (Player) shooterEntity;
            String targetMessage = config.getString("snowball_firework.target_message");
            target.sendMessage(translateColorCodes(translatePlaceholder(targetMessage, target, shooter)));
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

    private int actionChance(String configChancePath) {
        FileConfiguration config = this.getConfig();
        String string = configChancePath.substring(0, configChancePath.indexOf('.'));
        int chance = config.getInt("snowball_freeze.chance");
        if (!string.equals("snowball_freeze")) {
            chance += config.getInt("snowball_freeze.chance");
        } else {return chance;}
        if (!string.equals("snowball_firework")) {
            chance += config.getInt("snowball_freeze.chance");
        } else {return chance;}
        return chance;
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

        config.addDefault("snowball_firework.chance", 20);
        config.addDefault("snowball_firework.cooldown", 3);
        config.addDefault("snowball_firework.target_message", "&b%shooter% &9hit you with a firework");
        config.addDefault("snowball_firework.shooter_message", "&9You hit &b%target% &9 a firework");
        config.addDefault("snowball_firework.golem_message", "&9A &bsnowman &9hit you with a firework");

        saveConfig();

    }
}

