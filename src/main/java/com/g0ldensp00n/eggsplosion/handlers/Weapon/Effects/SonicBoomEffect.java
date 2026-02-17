package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class SonicBoomEffect extends WeaponEffect {
  Particle particle;
  double stepSize;
  int maxDistance;
  int start = 0;
  Collection<WeaponEffect> effectToApply;

  public SonicBoomEffect(Particle particle, double stepSize, int maxDistance, Collection<WeaponEffect> effectToApply) {
    this.particle = particle;
    this.stepSize = stepSize;
    this.maxDistance = maxDistance;
    this.effectToApply = effectToApply;
  }

  public SonicBoomEffect(Particle particle, double stepSize, int maxDistance, int start,
      Collection<WeaponEffect> effectToApply) {
    this.particle = particle;
    this.stepSize = stepSize;
    this.maxDistance = maxDistance;
    this.effectToApply = effectToApply;
    this.start = start;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    for (int i = start; i < maxDistance; i += stepSize) {
      Location loc = shooter.getEyeLocation().add(shooter.getEyeLocation().getDirection().multiply(i));
      if (particle != null) {
        shooter.getWorld().spawnParticle(particle, loc, 1);
      }
      // if (sonicBoomSound != null) {
      //   Random random = new Random();
      //   shooter.getLocation().getWorld().playSound(shooter.getEyeLocation(), sonicBoomSound,
      //       SoundCategory.HOSTILE, 1.0f, random.nextFloat(0.8f, 1.2f));
      //
      // }
      for (WeaponEffect effect : effectToApply) {
        effect.activateEffect(loc, shooter);
      }
    }
  }

}
