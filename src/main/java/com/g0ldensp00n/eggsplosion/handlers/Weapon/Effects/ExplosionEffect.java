package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class ExplosionEffect extends WeaponEffect {
  float explosionPower;
  Sound explosionSound = Sound.ENTITY_GENERIC_EXPLODE;
  Particle explosionParticle = Particle.EXPLOSION_EMITTER;

  public ExplosionEffect(float explosionPower) {
    this.explosionPower = explosionPower;
  }

  public ExplosionEffect(float explosionPower, Sound explosionSound) {
    this.explosionPower = explosionPower;
    this.explosionSound = explosionSound;
  }

  public ExplosionEffect(float explosionPower, Particle explosionParticle) {
    this.explosionPower = explosionPower;
    this.explosionParticle = explosionParticle;
  }

  public ExplosionEffect(float explosionPower, Sound explosionSound, Particle explosionParticle) {
    this.explosionPower = explosionPower;
    this.explosionSound = explosionSound;
    this.explosionParticle = explosionParticle;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    World world = location.getWorld();

    world.createExplosion(location, explosionPower, false, true, (Entity) shooter);
    world.spawnParticle(explosionParticle, location, 0);
    world.playSound(location, explosionSound,
        SoundCategory.HOSTILE, 2, 1);
  }
}
