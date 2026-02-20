package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;
//SOUNDS ?
//
// Sound dashSound = Sound.ENTITY_ILLUSIONER_MIRROR_MOVE;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.EffectListeners.KnockbackEffectListener;

public class DashEffect extends WeaponEffect {
  float dashPower;
  Particle dashParticle = Particle.CLOUD;

  public DashEffect(float dashPower) {
    this.dashPower = dashPower;
  }

  public DashEffect(float dashPower, Particle dashParticle) {
    this.dashPower = dashPower;
    this.dashParticle = dashParticle;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    for (float i = 0; i < dashPower * 2.0; i += 0.5) {
      Location loc = shooter.getEyeLocation().add(shooter.getEyeLocation().getDirection().multiply(i));
      Vector getParticleDirection = shooter.getLocation().add(0, 0.5, 0)
          .getDirection().multiply(-0.05);
      shooter.getWorld().spawnParticle(dashParticle, loc.subtract(shooter.getLocation().getDirection().multiply(2)),
          0,
          getParticleDirection.getX(),
          getParticleDirection.getY(),
          getParticleDirection.getZ(), 1.f);
    }

    shooter.setVelocity(shooter.getVelocity().add(shooter.getLocation().getDirection().multiply(dashPower)));
    shooter.setFallDistance(0);

    shooter.getPersistentDataContainer().set(KnockbackEffectListener.getWindChargeAnchorKey(),
        PersistentDataType.DOUBLE,
        location.getY());
  }
}
