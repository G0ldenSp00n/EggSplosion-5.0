package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WeaponAction {
  Collection<WeaponEffect> fireEffects;
  Collection<WeaponEffect> castEffects;
  Collection<WeaponEffect> reloadEffects;
  int fireReloadTicks;
  float fireVelocityMultiplier;
  Particle trailParticle;
  int projectileMaxTicksLived = -1;
  boolean isCharged = false;
  Class<? extends Projectile> projectile = Egg.class;
  Material projectileMaterial;
  NamespacedKey reloadingKey;
  int projectileCount = 1;
  int burstCount = 1;
  int burstDelayTicks = 0;
  int projectileSplit = 0;

  public WeaponAction(Collection<WeaponEffect> fireEffects,
      Collection<WeaponEffect> castEffects, Collection<WeaponEffect> reloadEffects) {
    this(fireEffects, castEffects, reloadEffects, 10, -1, 1.0f, Egg.class, Material.EGG,
        false, 1, 1, 0, 0);
  }

  public WeaponAction(Collection<WeaponEffect> fireEffects, Collection<WeaponEffect> castEffects,
      Collection<WeaponEffect> reloadEffects, int fireReloadTicks,
      int maxTicksLived, float fireVelocityMultiplier, Class<? extends Projectile> projectile,
      Material projectileMaterial, boolean isCharged, int projectileCount, int burstCount, int burstDelayTicks,
      int splitCount) {
    this.fireEffects = fireEffects;
    this.castEffects = castEffects;
    this.reloadEffects = reloadEffects;
    this.fireReloadTicks = fireReloadTicks;
    this.fireVelocityMultiplier = fireVelocityMultiplier;
    this.projectileMaxTicksLived = maxTicksLived;
    this.projectile = projectile;
    this.projectileMaterial = projectileMaterial;
    this.isCharged = isCharged;
    this.projectileCount = projectileCount;
    this.burstCount = burstCount;
    this.burstDelayTicks = burstDelayTicks;
    this.projectileSplit = splitCount;
  }

  public boolean isPrimaryAction() {
    return reloadingKey == WeaponRegistry.getPrimaryFireReloadAfterKey();
  }

  public boolean hasFireEffect() {
    return !fireEffects.isEmpty();
  }

  public boolean hasCastEffect() {
    return !castEffects.isEmpty();
  }

  public boolean hasReloadEffect() {
    return !reloadEffects.isEmpty();
  }

  public boolean hasEffect() {
    return hasCastEffect() || hasFireEffect() || hasReloadEffect();
  }

  public void resetReloadIfInvalid(ItemStack item) {
    int reloadLeft = Weapon.getReloadTimeLeft(this, item);
    if (reloadLeft > fireReloadTicks) {
      item.editPersistentDataContainer(pdc -> {
        pdc.remove(reloadingKey);
      });
    }
  }

  public static WeaponAction.Builder builder() {
    return new WeaponAction.Builder();
  }

  public static WeaponAction empty() {
    return new WeaponAction(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
  }

  public static class Builder {
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
    int splitCount = 0;

    public Builder() {
      fireEffects = new ArrayList<>();
      castEffects = new ArrayList<>();
      reloadEffects = new ArrayList<>();
    }

    public Builder withReloadTime(int ticks) {
      this.fireReloadTicks = ticks;
      return this;
    }

    public Builder withProjectiles(int projectileCount) {
      this.projectileCount = projectileCount;
      return this;
    }

    public Builder withBurstCount(int burstCount) {
      this.burstCount = burstCount;
      return this;
    }

    public Builder withBurstDelayTicks(int burstDelayTicks) {
      this.burstDelayTicks = burstDelayTicks;
      return this;
    }

    public Builder withProjectileMaxTicksLived(int ticks) {
      projectileMaxTicksLived = ticks;
      return this;
    }

    public Builder withVelocityMultiplier(float velocityMultiplier) {
      this.fireVelocityMultiplier = velocityMultiplier;
      return this;
    }

    public Builder addEffect(WeaponEffect effect) {
      this.fireEffects.add(effect);
      return this;
    }

    public Builder addCastEffect(WeaponEffect effect) {
      this.castEffects.add(effect);
      return this;
    }

    public Builder addReloadEffect(WeaponEffect effect) {
      this.reloadEffects.add(effect);
      return this;
    }

    public Builder withProjectileMaterial(Material projectileMaterial) {
      this.projectileMaterial = projectileMaterial;
      return this;
    }

    public Builder withProjectile(@NotNull Class<? extends Projectile> projectile) {
      this.projectile = projectile;
      return this;
    }

    public Builder isCharged(boolean isCharged) {
      this.isCharged = isCharged;
      return this;
    }

    public Builder withProjectileSplittingCount(int splitCount) {
      this.splitCount = splitCount;
      return this;
    }

    public WeaponAction build() {
      if (projectileMaterial == null) {
        projectileMaterial = Material.EGG;
      }

      return new WeaponAction(fireEffects, castEffects, reloadEffects, fireReloadTicks, projectileMaxTicksLived,
          fireVelocityMultiplier,
          projectile, projectileMaterial, isCharged, projectileCount, burstCount, burstDelayTicks, splitCount);
    }
  }
}
