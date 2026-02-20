package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.destroystokyo.paper.ParticleBuilder;
import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

import net.kyori.adventure.sound.Sound;

public class VisualEffect extends WeaponEffect {
  HashMap<Integer, Collection<ParticleBuilder>> particleToShowAtDelay;
  boolean isPlayerParticleSoure = false;
  int radius;
  boolean sphericalRadius;

  public VisualEffect() {
    particleToShowAtDelay = new HashMap<>();
  }

  public VisualEffect(HashMap<Integer, Collection<ParticleBuilder>> particleToShowAtDelay,
      boolean isPlayerParticleSource, int radius, boolean sphericalRadius) {
    this.particleToShowAtDelay = particleToShowAtDelay;
    this.isPlayerParticleSoure = isPlayerParticleSource;
    this.radius = radius;
    this.sphericalRadius = sphericalRadius;
  }

  public void spawnParticle(int delayKey, Player player, Location location) {
    for (ParticleBuilder particleBuilder : particleToShowAtDelay.getOrDefault(delayKey, new ArrayList<>())) {
      if (isPlayerParticleSoure) {
        ParticleBuilder locatedParticle = particleBuilder.location(player.getLocation()).source(player)
            .receivers(radius, sphericalRadius);
        locatedParticle.spawn();
      } else {
        ParticleBuilder locatedParticle = particleBuilder.location(location).source(player).receivers(radius,
            sphericalRadius);
        locatedParticle.spawn();
      }
    }
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    for (int tickDelay : particleToShowAtDelay.keySet()) {
      if (tickDelay == 0) {
        spawnParticle(0, shooter, location);
      } else {
        new BukkitRunnable() {
          @Override
          public void run() {
            spawnParticle(tickDelay, shooter, location);
          }
        }.runTaskLater(EggSplosion.getInstance(), tickDelay);
      }
    }
  }

  public static VisualEffect.Builder builder() {
    return new VisualEffect.Builder();
  }

  public static VisualEffect explosionVisualEffect() {
    return VisualEffect.builder().addParticle(Particle.EXPLOSION.builder().count(0)).build();
  }

  public static class Builder {
    HashMap<Integer, Collection<ParticleBuilder>> particleToShowAtDelay;
    boolean isPlayerParticleSource = false;
    int radius = 32;
    boolean sphericalRadius = true;

    public Builder() {
      this.particleToShowAtDelay = new HashMap<>();
    }

    public Builder addParticle(ParticleBuilder particleBuilder) {
      return this.addParticleWithDelay(particleBuilder, 0);
    }

    public Builder addParticleWithDelay(ParticleBuilder particleBuilder, int tickDelay) {
      Collection<ParticleBuilder> particleBuilders = this.particleToShowAtDelay.getOrDefault(tickDelay,
          new ArrayList<>());
      particleBuilders.add(particleBuilder);
      this.particleToShowAtDelay.put(tickDelay, particleBuilders);
      return this;
    }

    public Builder withPlayerAsSource() {
      isPlayerParticleSource = true;
      return this;
    }

    public Builder withCubicBoundCheck() {
      sphericalRadius = false;
      return this;
    }

    public Builder withRadius(int radius) {
      this.radius = radius;
      return this;
    }

    public VisualEffect build() {
      return new VisualEffect(particleToShowAtDelay, isPlayerParticleSource, radius, sphericalRadius);
    }
  }
}
