package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

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

  public WeaponAction(Collection<WeaponEffect> fireEffects,
      Collection<WeaponEffect> castEffects, Collection<WeaponEffect> reloadEffects) {
    this(fireEffects, castEffects, reloadEffects, 10, -1, 1.0f, Egg.class, Material.EGG,
        false, 1);
  }

  public WeaponAction(Collection<WeaponEffect> fireEffects, Collection<WeaponEffect> castEffects,
      Collection<WeaponEffect> reloadEffects, int fireReloadTicks,
      int maxTicksLived, float fireVelocityMultiplier, Class<? extends Projectile> projectile,
      Material projectileMaterial, boolean isCharged, int projectileCount) {
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

  public static WeaponActionBuilder builder() {
    return new WeaponActionBuilder();
  }

  public static WeaponAction empty() {
    return new WeaponAction(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
  }
}
