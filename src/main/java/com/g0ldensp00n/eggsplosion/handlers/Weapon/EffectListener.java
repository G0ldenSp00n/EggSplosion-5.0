package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.g0ldensp00n.eggsplosion.EggSplosion;

class EffectListener implements Listener {
  protected EffectListener() {
    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  @EventHandler
  public void entityCollide(ProjectileHitEvent projectileHitEvent) {
    Projectile projectile = projectileHitEvent.getEntity();
    if (projectile.getShooter() instanceof Player) {
      Player shooter = (Player) projectile.getShooter();
      if (projectile.getPersistentDataContainer().has(WeaponRegistry.getWeaponIDKey())) {
        NamespacedKey weaponID = NamespacedKey
            .fromString(projectile.getPersistentDataContainer().get(WeaponRegistry.getWeaponIDKey(),
                PersistentDataType.STRING));
        Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);
        if (projectile instanceof WitherSkull) {
          WitherSkull skull = (WitherSkull) projectile;
          if (!skull.isCharged()) {
            projectileHitEvent.setCancelled(true);
          }
        } else if (projectile instanceof Fireball) {
          projectileHitEvent.setCancelled(true);
        } else if (projectile instanceof Firework) {
          projectile.remove();
        }
        if (projectile.getPersistentDataContainer().has(WeaponRegistry.getIsWeaponPrimaryFireKey())) {
          boolean isWeaponPrimaryFire = projectile.getPersistentDataContainer().get(
              WeaponRegistry.getIsWeaponPrimaryFireKey(),
              PersistentDataType.BOOLEAN);
          if (isWeaponPrimaryFire) {
            for (WeaponEffect effect : weapon.primaryAction.fireEffects) {
              effect.activateEffect(projectile.getLocation(), shooter);
            }
          } else if (!isWeaponPrimaryFire) {
            // TODO: Sneak Action??
            // TODO: CLEAN THIS UP
            for (WeaponEffect effect : weapon.secondaryAction.fireEffects) {
              effect.activateEffect(projectile.getLocation(), shooter);
            }

            if (weapon.secondaryAction.projectileSplit > 0
                && projectile.getPersistentDataContainer().has(WeaponRegistry.getSplitCountKey())) {
              for (int i = 0; i < weapon.secondaryAction.projectileSplit; i += 1) {
                Vector projectileAngle = new Vector(0, 1, 0);
                if (projectileHitEvent.getHitBlockFace() != null) {
                  projectileAngle = projectileHitEvent.getHitBlockFace().getDirection();
                }
                Random random = new Random();
                float deflectionAmount = 1.5f;
                Vector offset = new Vector(random.nextFloat(-deflectionAmount, deflectionAmount),
                    random.nextFloat(-deflectionAmount, deflectionAmount),
                    random.nextFloat(-deflectionAmount, deflectionAmount));
                projectileAngle = projectileAngle.add(offset).normalize();

                int splitCount = projectile.getPersistentDataContainer().get(WeaponRegistry.getSplitCountKey(),
                    PersistentDataType.INTEGER);
                if (splitCount < weapon.secondaryAction.maxSplit) {
                  weapon.spawnActionProjectile(shooter, weapon.secondaryAction,
                      projectileHitEvent.getEntity().getLocation(), projectileAngle.multiply(0.5),
                      splitCount + 1);
                }
              }
            } else {
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void onWindChargeExplode(ExplosionPrimeEvent event) {
    if (event.getEntity().getPersistentDataContainer().has(WeaponRegistry.getWeaponIDKey())) {
      if (event.getEntity() instanceof WindCharge) {
        event.setCancelled(true);
        event.getEntity().remove();
      }
    }
  }

  @EventHandler
  public void fireballIgniteEvent(BlockIgniteEvent blockIgniteEvent) {
    Entity entity = blockIgniteEvent.getIgnitingEntity();
    if (entity instanceof Fireball || entity instanceof SmallFireball) {
      if (entity.getPersistentDataContainer().has(WeaponRegistry.getWeaponIDKey())) {
        blockIgniteEvent.setCancelled(true);
      }
    } else if (entity instanceof Player) {
      Player player = (Player) entity;
      if (player.getGameMode() != GameMode.CREATIVE) {
        blockIgniteEvent.setCancelled(true);
      }
    }
  }
}
