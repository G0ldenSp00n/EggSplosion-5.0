package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class ExplosionEffect extends WeaponEffect {
  float explosionPower;
  float pitch = 1;;
  float volume = 1;

  public ExplosionEffect(float explosionPower) {
    this.explosionPower = explosionPower;
  }

  public ExplosionEffect withPitch(float pitch) {
    this.pitch = pitch;
    return this;
  }

  public ExplosionEffect withVolume(float volume) {
    this.volume = volume;
    return this;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    World world = location.getWorld();

    world.createExplosion(location, explosionPower, false, true, (Entity) shooter);
  }
}
