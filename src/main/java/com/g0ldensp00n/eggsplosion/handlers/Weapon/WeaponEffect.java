package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import com.g0ldensp00n.eggsplosion.EggSplosion;

class WeaponEffect implements Listener {
  float explosionPower;
  float knockbackPower;
  Collection<PotionEffect> potionEffects;
  Sound soundEffect;

  public void activateEffect(Location location, Player shooter) {
    World world = location.getWorld();

    for (int i = 0; i < knockbackPower; i++) {
      WindCharge wind_charge = (WindCharge) world.spawnEntity(location.add(0, .75, 0),
          EntityType.WIND_CHARGE);
      wind_charge.setShooter(shooter);
      wind_charge.explode();
    }

    if (knockbackPower != -1 && potionEffects.size() != 0) {
      double radius = 4.0;
      double radiusSq = radius * radius;
      for (LivingEntity entity : location.getWorld().getNearbyLivingEntities(location, radius, radius, radius)) {
        if (entity.getLocation().distanceSquared(location) > radiusSq)
          continue;

        if (entity instanceof Player) {
          Player victim = (Player) entity;

          double intensity = 1.0 - (entity.getLocation().distance(location) / radius);
          int originalDurration = 100;
          int newDuration = (int) (originalDurration * intensity);

          if (EggSplosion.getInstance().getLobbyManager().canPlayerAttackPlayer(victim, shooter)) {
            for (PotionEffect effect : potionEffects) {
              effect.withDuration(newDuration).apply(entity);
            }
          }

          Vector direction = entity.getLocation().toVector().subtract(location.toVector());

          if (direction.lengthSquared() == 0) {
            direction = new Vector(0, 0.5, 0);
          } else {
            direction.normalize();
          }

          direction.multiply(knockbackPower * intensity);

          entity.setVelocity(entity.getVelocity().add(direction));
          if (entity == shooter) {
            shooter.setFallDistance(0);

            NamespacedKey windChargeAnchorKey = new NamespacedKey(EggSplosion.getInstance(),
                "windChargeAnchor");
            shooter.getPersistentDataContainer().set(windChargeAnchorKey, PersistentDataType.DOUBLE,
                location.getY());

          }
        }
      }
    }

    if (explosionPower != -1) {
      world.createExplosion(location, explosionPower, false, true, (Entity) shooter);
      world.spawnParticle(Particle.EXPLOSION, location, 0);
      world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2, 1);
    }

    if (potionEffects != null && potionEffects.size() != 0) {
      SplashPotion potion = (SplashPotion) world.spawnEntity(location, EntityType.SPLASH_POTION);
      potion.setShooter(shooter);
      potion.collidesAt(location);
      potion.getEffects().addAll(potionEffects);
    }
  }

  @EventHandler
  public void onFallDamage(EntityDamageEvent event) {
    if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
      return;
    if (!(event.getEntity() instanceof LivingEntity))
      return;

    LivingEntity entity = (LivingEntity) event.getEntity();

    NamespacedKey windChargeAnchorKey = new NamespacedKey(EggSplosion.getInstance(),
        "windChargeAnchor");
    if (entity.getPersistentDataContainer().has(windChargeAnchorKey)) {
      double startY = entity.getPersistentDataContainer().get(windChargeAnchorKey, PersistentDataType.DOUBLE);
      double landY = entity.getLocation().getY();

      entity.getPersistentDataContainer().remove(windChargeAnchorKey);
      if (landY >= startY) {
        event.setCancelled(true);
      } else {
        double distanceFallen = startY - landY;
        double newDamage = distanceFallen - 3.0;

        if (newDamage <= 0) {
          event.setCancelled(true);
        } else {
          event.setDamage(newDamage);
        }
      }
    }
  }
}
