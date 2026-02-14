package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class DashEffect extends WeaponEffect {
  float dashPower;
  //TODO: Enderman Warp
  Sound dashSound = Sound.ENTITY_EN;
  Particle dashParticle = Particle.EXPLOSION_EMITTER;

  public DashEffect(float dashPower) {
    this.dashPower = dashPower;
  }

  public ExplosionEffect(float dashPower, Sound dashSound) {
    this.dashPower = dashPower;
    this.dashSound = dashSound;
  }

  public ExplosionEffect(float dashPower, Particle dashParticle) {
    this.dashPower = dashPower;
    this.dashParticle = dashParticle;
  }

  public ExplosionEffect(float dashPower, Sound dashSound, Particle dashParticle) {
    this.dashPower = dashPower;
    this.dashSound = dashSound;
    this.dashParticle = dashParticle;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    world.playSound(shooter.getLocation()), explosionSound,
        SoundCategory.HOSTILE, 2, 1);
    shooter.setVelocity(shooter.getVelocity().add(shooter.getLocation().getDirection().multiply(dashPower));
  }
}
