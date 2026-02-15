package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class DamageEffect extends WeaponEffect {
  double damageRadius;
  float damageAmount;
  DamageType damageType;
  boolean doFalloff = true;

  public DamageEffect(double damageRadius, float damageAmount, DamageType damageType) {
    this.damageRadius = damageRadius;
    this.damageAmount = damageAmount;
    this.damageType = damageType;
  }

  public DamageEffect(double damageRadius, float damageAmount, DamageType damageType, boolean doFalloff) {
    this.damageRadius = damageRadius;
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.doFalloff = doFalloff;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    double damageRadiusSq = damageRadius * damageRadius;
    for (LivingEntity entity : location.getWorld().getNearbyLivingEntities(location,
        damageRadius, damageRadius,
        damageRadius)) {
      if (entity.getLocation().distanceSquared(location) > damageRadiusSq)
        continue;

      double intensity = 1.0 - (entity.getLocation().distance(location) / damageRadius);
      if (!entity.equals(shooter)) {
        if (doFalloff) {
          entity.damage(damageAmount * intensity,
              DamageSource.builder(damageType).withDirectEntity(shooter).withCausingEntity(shooter).build());
        } else {
          entity.damage(damageAmount,
              DamageSource.builder(damageType).withDirectEntity(shooter).withCausingEntity(shooter).build());
        }
      }
    }
  }
}
