package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;

//TODO: Nest in WeaponAction Class
class WeaponActionBuilder {
  Collection<WeaponEffect> fireEffects;
  Collection<WeaponEffect> castEffects;
  Collection<WeaponEffect> reloadEffects;
  int fireReloadTicks;
  int projectileMaxTicksLived = -1;
  Material projectileMaterial;
  Class<? extends Projectile> projectile = Egg.class;
  float fireVelocityMultiplier;
  boolean isCharged;
  int projectileCount = 1;
  int burstCount = 1;
  int burstDelayTicks = 0;

  public WeaponActionBuilder() {
    fireEffects = new ArrayList<>();
    castEffects = new ArrayList<>();
    reloadEffects = new ArrayList<>();
  }

  public WeaponActionBuilder withReloadTime(int ticks) {
    this.fireReloadTicks = ticks;
    return this;
  }

  public WeaponActionBuilder withProjectiles(int projectileCount) {
    this.projectileCount = projectileCount;
    return this;
  }

  public WeaponActionBuilder withBurstCount(int burstCount) {
    this.burstCount = burstCount;
    return this;
  }

  public WeaponActionBuilder withBurstDelayTicks(int burstDelayTicks) {
    this.burstDelayTicks = burstDelayTicks;
    return this;
  }

  public WeaponActionBuilder withProjectileMaxTicksLived(int ticks) {
    projectileMaxTicksLived = ticks;
    return this;
  }

  public WeaponActionBuilder withVelocityMultiplier(float velocityMultiplier) {
    this.fireVelocityMultiplier = velocityMultiplier;
    return this;
  }

  public WeaponActionBuilder addEffect(WeaponEffect effect) {
    this.fireEffects.add(effect);
    return this;
  }

  public WeaponActionBuilder addCastEffect(WeaponEffect effect) {
    this.castEffects.add(effect);
    return this;
  }

  public WeaponActionBuilder addReloadEffect(WeaponEffect effect) {
    this.reloadEffects.add(effect);
    return this;
  }

  public WeaponActionBuilder withProjectileMaterial(Material projectileMaterial) {
    this.projectileMaterial = projectileMaterial;
    return this;
  }

  public WeaponActionBuilder withProjectile(@NotNull Class<? extends Projectile> projectile) {
    this.projectile = projectile;
    return this;
  }

  public WeaponActionBuilder isCharged(boolean isCharged) {
    this.isCharged = isCharged;
    return this;
  }

  public WeaponAction build() {
    if (projectileMaterial == null) {
      projectileMaterial = Material.EGG;
    }

    return new WeaponAction(fireEffects, castEffects, reloadEffects, fireReloadTicks, projectileMaxTicksLived,
        fireVelocityMultiplier,
        projectile, projectileMaterial, isCharged, projectileCount, burstCount, burstDelayTicks);
  }
}
