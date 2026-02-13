package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;

public class Weapon implements Listener {
  NamespacedKey weaponID;
  Material projectile;
  Material item;
  WeaponAction primaryAction;
  WeaponAction secondaryAction;

  protected Weapon(NamespacedKey weaponID, Material item, WeaponAction primaryAction,
      WeaponAction secondaryAction) {
    this.weaponID = weaponID;
    this.primaryAction = primaryAction;
    this.secondaryAction = secondaryAction;
    this.item = item;

    projectile = Material.EGG;

    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  public ItemStack getItem() {
    ItemStack weapon = new ItemStack(item);
    NamespacedKey weaponIDKey = new NamespacedKey(EggSplosion.getInstance(),
        "weapon_id");

    ItemMeta weaponMeta = weapon.getItemMeta();
    weaponMeta.getPersistentDataContainer().set(weaponIDKey,
        PersistentDataType.STRING,
        weaponID.asString());
    weaponMeta.displayName(Component.translatable(weaponID.getKey()));
    weapon.setItemMeta(weaponMeta);
    return weapon;
  }

  private void fire(Player player, ItemStack weaponItem, WeaponAction action, boolean isPrimaryAction) {
    NamespacedKey weaponIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_id");
    NamespacedKey fireReloadKey;
    if (isPrimaryAction) {
      fireReloadKey = new NamespacedKey(EggSplosion.getInstance(), "primary_fire_reloaded_after");
    } else {

      fireReloadKey = new NamespacedKey(EggSplosion.getInstance(),
          "secondary_fire_reloaded_after");

    }

    Egg egg = player.launchProjectile(Egg.class);

    egg.setItem(new ItemStack(projectile));

    egg.getPersistentDataContainer().set(weaponIDKey, PersistentDataType.STRING, weaponID.asString());
    NamespacedKey isWeaponPrimaryFireKey = new NamespacedKey(EggSplosion.getInstance(),
        "is_weapon_primary_fire");

    egg.setVelocity(player.getLocation().getDirection().multiply(action.fireVelocityMultiplier));
    player.playSound(player.getLocation(), action.fireSound, SoundCategory.PLAYERS, 0.2f, 1f);
    egg.getPersistentDataContainer().set(isWeaponPrimaryFireKey, PersistentDataType.BOOLEAN, isPrimaryAction);

    ItemMeta meta = weaponItem.getItemMeta();
    meta.getPersistentDataContainer().set(fireReloadKey, PersistentDataType.INTEGER,
        Bukkit.getServer().getCurrentTick() + action.fireReloadTicks);
    weaponItem.setItemMeta(meta);
  }

  public void firePrimary(Player player, ItemStack weaponItem) {
    fire(player, weaponItem, primaryAction, true);
  }

  public void fireSecondary(Player player, ItemStack weaponItem) {
    fire(player, weaponItem, secondaryAction, false);
  }

  public boolean hasPrimaryEffect() {
    return !primaryAction.fireEffects.isEmpty();
  }

  public boolean hasSecondaryEffect() {
    return !secondaryAction.fireEffects.isEmpty();
  }

  public static boolean isWeapon(ItemStack item) {
    NamespacedKey weaponIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_id");

    if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(weaponIDKey)) {
      return true;
    }
    return false;
  }

  public static NamespacedKey getWeaponID(ItemStack item) {
    NamespacedKey weaponIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_id");

    if (isWeapon(item)) {
      NamespacedKey weaponID = NamespacedKey.fromString(
          item.getItemMeta().getPersistentDataContainer().get(weaponIDKey, PersistentDataType.STRING));
      return weaponID;
    }
    return null;
  }

  public static boolean isPrimaryReloading(ItemStack item) {
    NamespacedKey primaryFireReloadKey = new NamespacedKey(EggSplosion.getInstance(), "primary_fire_reloaded_after");
    if (item.getItemMeta().getPersistentDataContainer().has(primaryFireReloadKey)) {
      if (Bukkit.getCurrentTick() <= item.getItemMeta().getPersistentDataContainer().get(primaryFireReloadKey,
          PersistentDataType.INTEGER)) {
        return true;
      }
    }
    return false;
  }

  public static int getPrimaryReloadTimeLeft(ItemStack item) {
    NamespacedKey primaryFireReloadKey = new NamespacedKey(EggSplosion.getInstance(), "primary_fire_reloaded_after");

    if (item.getItemMeta().getPersistentDataContainer().has(primaryFireReloadKey)) {
      int tickReloaded = item.getItemMeta().getPersistentDataContainer().get(primaryFireReloadKey,
          PersistentDataType.INTEGER);
      return tickReloaded - Bukkit.getCurrentTick();
    }
    return 0;
  }

  public static boolean isSecondaryReloading(ItemStack item) {
    int reloadingTimeLeft = getSecondaryReloadTimeLeft(item);
    if (reloadingTimeLeft > 0) {
      return true;
    }
    return false;
  }

  public static int getSecondaryReloadTimeLeft(ItemStack item) {
    NamespacedKey secondaryFireReloadKey = new NamespacedKey(EggSplosion.getInstance(),
        "secondary_fire_reloaded_after");

    if (item.getItemMeta().getPersistentDataContainer().has(secondaryFireReloadKey)) {
      int tickReloaded = item.getItemMeta().getPersistentDataContainer().get(secondaryFireReloadKey,
          PersistentDataType.INTEGER);
      return tickReloaded - Bukkit.getCurrentTick();
    }
    return 0;
  }
}
