package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class KnockbackEffect extends WeaponEffect {
  float knockbackPower;
  double knockbackRadius = 4.0;

  public KnockbackEffect(float knockbackPower) {
    this.knockbackPower = knockbackPower;
  }

  public KnockbackEffect(float knockbackPower, double knockbackRadius) {
    this.knockbackPower = knockbackPower;
    this.knockbackRadius = knockbackRadius;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    double knockbackRadiusSq = knockbackRadius * knockbackRadius;
    for (LivingEntity entity : location.getWorld().getNearbyLivingEntities(location, knockbackRadius, knockbackRadius,
        knockbackRadius)) {
      if (entity.getLocation().distanceSquared(location) > knockbackRadiusSq)
        continue;

      double intensity = 1.0 - (entity.getLocation().distance(location) / knockbackRadius);
      Vector direction = entity.getLocation().toVector().subtract(location.toVector());

      if (direction.lengthSquared() == 0) {
        direction = new Vector(0, 0.5, 0);
      } else {
        direction.normalize();
      }

      direction.setY(direction.getY() + 0.25);

      direction.multiply(knockbackPower * intensity);

      entity.setVelocity(entity.getVelocity().add(direction));
      // TODO: Prevent Team Damage?
      if (entity.equals(shooter)) {
        shooter.setFallDistance(0);

        NamespacedKey windChargeAnchorKey = new NamespacedKey(EggSplosion.getInstance(),
            "wind_charge_anchor");
        shooter.getPersistentDataContainer().set(windChargeAnchorKey,
            PersistentDataType.DOUBLE,
            location.getY());
      }
    }
  }
}
