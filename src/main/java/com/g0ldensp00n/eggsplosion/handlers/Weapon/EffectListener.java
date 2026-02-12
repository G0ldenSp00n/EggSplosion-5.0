package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.UUID;

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
  @EventHandler
  public void entityCollide(ProjectileHitEvent projectileHitEvent) {
    if (projectileHitEvent.getEntity().getType() == EntityType.EGG) {
      Egg egg = (Egg) projectileHitEvent.getEntity();
      if (egg.getShooter() instanceof Player) {
        Player shooter = (Player) egg.getShooter();
        NamespacedKey weaponUUIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_uuid");

        if (egg.getPersistentDataContainer().has(weaponUUIDKey)) {
          UUID weaponUUID = UUID
              .fromString(egg.getPersistentDataContainer().get(weaponUUIDKey, PersistentDataType.STRING));
          Weapon weapon = WeaponRegistry.getInstance().getWeaponByUUID(weaponUUID);
          NamespacedKey isWeaponPrimaryFireKey = new NamespacedKey(EggSplosion.getInstance(),
              "isWeaponPrimaryFireKey");

          if (egg.getPersistentDataContainer().has(isWeaponPrimaryFireKey)) {
            boolean isWeaponPrimaryFire = egg.getPersistentDataContainer().get(isWeaponPrimaryFireKey,
                PersistentDataType.BOOLEAN);
            if (isWeaponPrimaryFire && weapon.primaryFire != null) {
              weapon.primaryFire.activateEffect(projectileHitEvent.getHitBlock().getLocation(), shooter);
            } else if (weapon.secondaryFire != null) {
              weapon.secondaryFire.activateEffect(projectileHitEvent.getHitBlock().getLocation(), shooter);
            }
          }
        }
      }
    }
  }
}
