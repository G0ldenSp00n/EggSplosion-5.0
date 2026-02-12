package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.NamespacedKey;

import com.g0ldensp00n.eggsplosion.EggSplosion;

class WeaponRegistry {
  private static WeaponRegistry instance;

  private HashMap<UUID, Weapon> weapons;
  private UUID defaultWeaponUUID;

  public WeaponRegistry() {
    instance = this;

    weapons = new HashMap<>();
    defaultWeaponUUID = UUID.randomUUID();
    weapons.put(defaultWeaponUUID, new Weapon(defaultWeaponUUID, new WeaponEffect(), new WeaponEffect()));
  }

  public static WeaponRegistry getInstance() {
    return instance;
  }

  public void createWeapon(WeaponEffect primaryFire, WeaponEffect secondaryFire) {
    UUID weaponUUID = UUID.randomUUID();
    weapons.put(weaponUUID, new Weapon(weaponUUID, primaryFire, secondaryFire));
  }

  public Weapon getDefaultWeapon() {
    return weapons.get(defaultWeaponUUID);
  }

  public Weapon getWeaponByUUID(UUID key) {
    return weapons.getOrDefault(key, getDefaultWeapon());
  }
}
