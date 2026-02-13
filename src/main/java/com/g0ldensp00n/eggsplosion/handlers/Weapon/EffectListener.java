package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

class EffectListener implements Listener {
  protected EffectListener() {
    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  @EventHandler
  public void entityCollide(ProjectileHitEvent projectileHitEvent) {
    if (projectileHitEvent.getEntity().getType() == EntityType.EGG) {
      Egg egg = (Egg) projectileHitEvent.getEntity();
      if (egg.getShooter() instanceof Player) {
        Player shooter = (Player) egg.getShooter();
        NamespacedKey weaponIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_id");

        if (egg.getPersistentDataContainer().has(weaponIDKey)) {
          NamespacedKey weaponID = NamespacedKey.fromString(egg.getPersistentDataContainer().get(weaponIDKey, PersistentDataType.STRING))
          Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);
          NamespacedKey isWeaponPrimaryFireKey = new NamespacedKey(EggSplosion.getInstance(),
              "is_weapon_primary_fire");

          if (egg.getPersistentDataContainer().has(isWeaponPrimaryFireKey)) {
            boolean isWeaponPrimaryFire = egg.getPersistentDataContainer().get(isWeaponPrimaryFireKey,
                PersistentDataType.BOOLEAN);
            if (isWeaponPrimaryFire && !weapon.primaryFireEffects.isEmpty()) {
              for (WeaponEffect effect : weapon.primaryFireEffects) {
                effect.activateEffect(egg.getLocation(), shooter);
              }
            } else if (!weapon.secondaryFireEffects.isEmpty()) {
              for (WeaponEffect effect : weapon.secondaryFireEffects) {
                effect.activateEffect(egg.getLocation(), shooter);
              }
            }
          }
        }
      }
    }
  }
}
