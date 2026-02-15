package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;

class WeaponActionBuilder {
  Collection<WeaponEffect> fireEffects;
  Collection<WeaponEffect> castEffects;
  int fireReloadTicks;
  int projectileMaxTicksLived = -1;
  Material projectileMaterial;
  Class<? extends Projectile> projectile = Egg.class;
  float fireVelocityMultiplier;
  Sound fireSound;

  public WeaponActionBuilder() {
    fireEffects = new ArrayList<>();
    castEffects = new ArrayList<>();
  }

  public WeaponActionBuilder withReloadTime(int ticks) {
    this.fireReloadTicks = ticks;
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

  public WeaponActionBuilder withFireSound(Sound sound) {
    this.fireSound = sound;
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

  public WeaponActionBuilder withProjectileMaterial(Material projectileMaterial) {
    this.projectileMaterial = projectileMaterial;
    return this;
  }

  public WeaponActionBuilder withProjectile(@NotNull Class<? extends Projectile> projectile) {
    this.projectile = projectile;
    return this;
  }

  public WeaponAction build() {
    if (fireSound == null) {
      fireSound = Sound.ENTITY_CHICKEN_EGG;
    }

    if (projectileMaterial == null) {
      projectileMaterial = Material.EGG;
    }

    return new WeaponAction(fireEffects, castEffects, fireReloadTicks, projectileMaxTicksLived, fireVelocityMultiplier,
        fireSound, projectile, projectileMaterial);
  }
}
