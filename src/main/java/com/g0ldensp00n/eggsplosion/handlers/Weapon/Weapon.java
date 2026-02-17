package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;

public class Weapon implements Listener {
  NamespacedKey weaponID;
  Component displayName;
  Material item;
  WeaponAction primaryAction;
  WeaponAction secondaryAction;
  WeaponAction sneakAction;

  protected Weapon(NamespacedKey weaponID, Material item, WeaponAction primaryAction,
      WeaponAction secondaryAction) {
    this.weaponID = weaponID;
    this.primaryAction = primaryAction;
    this.secondaryAction = secondaryAction;
    this.item = item;
    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  public static WeaponBuilder builder(String weaponID) {
    return new WeaponBuilder(weaponID);
  }

  protected Weapon(NamespacedKey weaponID, Component displayName, Material item, WeaponAction primaryAction,
      WeaponAction secondaryAction, WeaponAction sneakAction) {
    this.weaponID = weaponID;
    this.primaryAction = primaryAction;
    this.secondaryAction = secondaryAction;
    this.sneakAction = sneakAction;
    this.item = item;
    this.displayName = displayName;

    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  public ItemStack getItem() {
    ItemStack weapon = new ItemStack(item);
    weapon.editPersistentDataContainer(pdc -> {
      pdc.set(WeaponRegistry.getWeaponIDKey(),
          PersistentDataType.STRING,
          weaponID.asString());
    });
    ItemMeta weaponMeta = weapon.getItemMeta();
    UseCooldownComponent useCooldownComponent = weaponMeta.getUseCooldown();
    useCooldownComponent.setCooldownGroup(weaponID);
    weaponMeta.displayName(displayName);
    weaponMeta.setUseCooldown(useCooldownComponent);
    weaponMeta.setUnbreakable(true);

    weapon.setItemMeta(weaponMeta);
    return weapon;
  }

  private void setReloading(Player player, ItemStack weaponItem, WeaponAction action) {
    weaponItem.editPersistentDataContainer(pdc -> {
      pdc.set(action.reloadingKey, PersistentDataType.INTEGER,
          Bukkit.getServer().getCurrentTick() + action.fireReloadTicks);
    });
    if (action.isPrimaryAction()) {
      player.setCooldown(weaponID, primaryAction.fireReloadTicks);
    }
    player.playSound(player.getLocation(), action.fireSound, SoundCategory.PLAYERS, 0.2f, 1f);
    if (action.hasReloadEffect()) {
      new BukkitRunnable() {
        @Override
        public void run() {
          for (WeaponEffect effect : action.reloadEffects) {
            effect.activateEffect(player.getLocation(), player);
          }
        }
      }.runTaskLater(EggSplosion.getInstance(), action.fireReloadTicks);
    }

  }

  private void fire(Player player, ItemStack weaponItem, WeaponAction action) {
    Projectile projectile = player.launchProjectile(action.projectile,
        player.getLocation().getDirection().multiply(action.fireVelocityMultiplier));

    if (projectile instanceof Egg) {
      Egg egg = (Egg) projectile;
      egg.setItem(new ItemStack(action.projectileMaterial));
    }

    projectile.getPersistentDataContainer().set(WeaponRegistry.getWeaponIDKey(), PersistentDataType.STRING,
        weaponID.asString());
    projectile.getPersistentDataContainer().set(WeaponRegistry.getIsWeaponPrimaryFireKey(), PersistentDataType.BOOLEAN,
        action.isPrimaryAction());

    if (action.projectileMaxTicksLived != -1) {
      new BukkitRunnable() {
        @Override
        public void run() {
          if (projectile != null && !projectile.isDead()) {
            for (WeaponEffect effect : action.fireEffects) {
              effect.activateEffect(projectile.getLocation(), player);
            }
            projectile.remove();
          }
        }
      }.runTaskLater(EggSplosion.getInstance(), action.projectileMaxTicksLived);
    }
  }

  public void fireWeapon(Player player, ItemStack weaponItem, WeaponAction action) {
    activateCastAction(player, weaponItem, action);
    if (action.hasFireEffect()) {
      fire(player, weaponItem, action);
    }
    setReloading(player, weaponItem, action);

  }

  private void activateCastAction(Player player, ItemStack weaponItem,
      WeaponAction action) {
    for (WeaponEffect effect : action.castEffects) {
      effect.activateEffect(player.getLocation(), player);
    }
  }

  public static boolean isWeapon(ItemStack item) {
    if (item.hasItemMeta() && item.getPersistentDataContainer().has(WeaponRegistry.getWeaponIDKey())) {
      return true;
    }
    return false;
  }

  public static NamespacedKey getWeaponID(ItemStack item) {
    if (isWeapon(item)) {
      NamespacedKey weaponID = NamespacedKey.fromString(
          item.getPersistentDataContainer().get(WeaponRegistry.getWeaponIDKey(), PersistentDataType.STRING));
      return weaponID;
    }
    return null;
  }

  public static boolean isReloading(WeaponAction action, ItemStack item) {
    int reloadingTimeLeft = getReloadTimeLeft(action, item);
    if (reloadingTimeLeft > 0) {
      return true;
    }
    return false;
  }

  public static int getReloadTimeLeft(WeaponAction action, ItemStack item) {
    if (item.getPersistentDataContainer().has(action.reloadingKey)) {
      int tickReloaded = item.getPersistentDataContainer().get(action.reloadingKey,
          PersistentDataType.INTEGER);
      return tickReloaded - Bukkit.getCurrentTick();
    }
    return 0;
  }
}
