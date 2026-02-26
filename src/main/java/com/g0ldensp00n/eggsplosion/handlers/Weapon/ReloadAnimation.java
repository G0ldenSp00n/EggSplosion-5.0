package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.g0ldensp00n.eggsplosion.EggSplosion;

class ReloadAnimation implements Listener {
  HashMap<UUID, BukkitTask> playerUUIDToRunnable;

  protected ReloadAnimation() {
    playerUUIDToRunnable = new HashMap<>();

    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  @EventHandler
  public void playerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (!playerUUIDToRunnable.containsKey(player.getUniqueId())) {
      playerUUIDToRunnable.put(player.getUniqueId(),
          new BukkitRunnable() {
            @Override
            public void run() {
              ItemStack heldWeapon = player.getInventory().getItemInMainHand();

              if (Weapon.isWeapon(heldWeapon)) {
                NamespacedKey weaponID = Weapon.getWeaponID(heldWeapon);
                Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);

                if (weapon == null) {
                  player.setLevel(0);
                  player.setExp(0);
                  return;
                }

                if (heldWeapon.getPersistentDataContainer().has(WeaponRegistry.getSecondaryFireReloadAfterKey())) {
                  int secondaryReloadTimeLeft = Weapon.getReloadTimeLeft(weapon.secondaryAction, heldWeapon);

                  float percentageReloaded = (float) (secondaryReloadTimeLeft)
                      / (float) weapon.secondaryAction.fireReloadTicks;
                  if (percentageReloaded >= 0 && percentageReloaded <= 1) {
                    player.setExp(percentageReloaded);
                  } else {
                    player.setExp(0);
                  }
                } else {
                  player.setExp(0);
                }

                if (heldWeapon.getPersistentDataContainer().has(WeaponRegistry.getSneakActionReloadAfterKey())) {
                  int sneakReloadTimeLeft = Weapon.getReloadTimeLeft(weapon.sneakAction, heldWeapon);
                  if (sneakReloadTimeLeft > 0) {
                    player.setLevel((sneakReloadTimeLeft / 20) + 1);
                  } else {
                    player.setLevel(0);
                  }
                } else {
                  player.setLevel(0);
                }
              } else {
                player.setLevel(0);
                player.setExp(0);
              }
            }
          }.runTaskTimer(EggSplosion.getInstance(), 0, 1));
    }
  }

  @EventHandler
  public void playerLeave(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (playerUUIDToRunnable.containsKey(player.getUniqueId())) {
      playerUUIDToRunnable.get(player.getUniqueId()).cancel();
      playerUUIDToRunnable.remove(player.getUniqueId());
    }
  }

  public void startWeaponReloadTimer(Player player) {
  }
}
