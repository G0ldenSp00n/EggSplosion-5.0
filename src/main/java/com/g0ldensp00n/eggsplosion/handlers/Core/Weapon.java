package com.g0ldensp00n.eggsplosion.handlers.Core;

import org.bukkit.Bukkit;
import java.util.*;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;

public class Weapon implements Listener {
  private ArrayList<Player> reloadingPlayers = new ArrayList<>();
  private Map<String, Long> reloadingPlayerTimes = new Hashtable<String, Long>();
  private Plugin plugin;

  public Weapon(Plugin plugin) {
    this.plugin = plugin;
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {
    if (playerInteractEvent.getItem() != null) {
      // Possible Explosion Caluculation -5.3x+(3.7x^2)
      if ((playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_AIR)
          || playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
        Player player = playerInteractEvent.getPlayer();
        if (!(player.hasPotionEffect(PotionEffectType.INVISIBILITY)
            && player.hasPotionEffect(PotionEffectType.REGENERATION))) {
          switch (playerInteractEvent.getItem().getType()) {
            case NETHERITE_HOE:
              playerInteractEvent.setCancelled(true);
              launchWeapon(player, 2, 15, 15, 4);
              break;
            case DIAMOND_HOE:
              playerInteractEvent.setCancelled(true);
              launchWeapon(player, 2, 3, 4.2f, 3);
              break;
            case GOLDEN_HOE:
              playerInteractEvent.setCancelled(true);
              launchWeapon(player, 2, 2.8f, 0.8875f, 1);
              break;
            case IRON_HOE:
              playerInteractEvent.setCancelled(true);
              launchWeapon(player, 2, 2.6f, 0.7f, 1);
              break;
            case COPPER_HOE:
              playerInteractEvent.setCancelled(true);
              launchWeapon(player, 2, 2.5f, 0.6f, 1);
              break;
            case STONE_HOE:
              playerInteractEvent.setCancelled(true);
              launchWeapon(player, 2, 2.4f, 2.5f, 2);
              break;
            case WOODEN_HOE:
              playerInteractEvent.setCancelled(true);
              launchWeapon(player, 2, 2, 0.05f, 1);
              break;
            default:
              break;
          }
        } else {
          player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
              TextComponent.fromLegacy("Can't Fire Weapon with Spawn Protection"));
        }
      }
    }
  }

  private void launchWeapon(Player player, int velocityMultiplier, float explosionPower, double reloadTime,
      int rocket_jump_power) {
    reloadTime = reloadTime * 20;
    Long playerReloadTime = reloadingPlayerTimes.get(player.getUniqueId().toString());
    if (playerReloadTime == null || playerReloadTime <= System.currentTimeMillis()) {
      reloadingPlayers.remove(player);
      reloadingPlayerTimes.put(player.getUniqueId().toString(),
          Math.round(System.currentTimeMillis() + (reloadTime * (1000 / 20))));
    }

    if (!reloadingPlayers.contains(player)) {
      fireWeapon(player, velocityMultiplier, explosionPower, rocket_jump_power);
      reloadingPlayers.add(player);
      xpLoading(player, reloadTime);
    }
  }

  private void xpLoading(Player player, double delayLength) {
    new BukkitRunnable() {
      float maxXP = 1.0F;
      float divideXP = maxXP / (float) delayLength;

      @Override
      public void run() {
        if (maxXP > 0) {
          player.setExp(maxXP);
          maxXP = maxXP - divideXP;
        } else {
          player.setExp(0);
          player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5f, 2);
          cancel();
        }
      }
    }.runTaskTimer(this.plugin, 0, 1);
  }

  private void fireWeapon(Player player, int velocityMultiplier, float explosionSize, int rocket_jump_power) {
    player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.2f, 1f);
    Egg egg = player.launchProjectile(Egg.class);
    egg.setCustomName(player.getUniqueId() + " / " + explosionSize);
    egg.setVelocity(egg.getVelocity().multiply(velocityMultiplier));
    egg.setMetadata("rocket_jump_power",
        new FixedMetadataValue(Bukkit.getServer().getPluginManager().getPlugin("EggSplosion"), rocket_jump_power));
  }
}
