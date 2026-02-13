package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;

class Weapon implements Listener {
  NamespacedKey weaponID;
  Material projectile;
  Material item;
  Collection<WeaponEffect> primaryFireEffects;
  Collection<WeaponEffect> secondaryFireEffects;
  int primaryFireReloadTicks;
  Sound primaryFireSound;
  int secondaryFireReloadTicks;
  Sound secondaryFireSound;

  protected Weapon(NamespacedKey weaponID, Collection<WeaponEffect> primaryEffects,
      Collection<WeaponEffect> secondaryEffects) {
    this.weaponID = weaponID;
    this.primaryFireEffects = primaryEffects;
    this.secondaryFireEffects = secondaryEffects;

    primaryFireReloadTicks = 100;
    secondaryFireReloadTicks = 20;
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
    // weaponMeta.displayName(Translata);
    weapon.setItemMeta(weaponMeta);
    return weapon;

  }
}
