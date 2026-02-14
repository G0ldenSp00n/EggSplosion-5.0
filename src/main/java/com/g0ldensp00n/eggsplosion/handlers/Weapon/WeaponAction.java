package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Sound;

public class WeaponAction {
  Collection<WeaponEffect> fireEffects;
  Collection<WeaponEffect> castEffects;
  int fireReloadTicks;
  float fireVelocityMultiplier;
  Sound fireSound;
  Particle trailParticle;

  public WeaponAction(Collection<WeaponEffect> fireEffects) {
    this(fireEffects, 10, 1.0f, Sound.ENTITY_CHICKEN_EGG);
  }

  public WeaponAction(Collection<WeaponEffect> fireEffects, int fireReloadTicks,
      float fireVelocityMultiplier) {
    this(fireEffects, fireReloadTicks, fireVelocityMultiplier, Sound.ENTITY_CHICKEN_EGG);
  }

  public WeaponAction(Collection<WeaponEffect> fireEffects, int fireReloadTicks,
      float fireVelocityMultiplier, Sound fireSound) {
    this.fireEffects = fireEffects;
    this.fireReloadTicks = fireReloadTicks;
    this.fireSound = fireSound;
    this.fireVelocityMultiplier = fireVelocityMultiplier;
  }

  public static WeaponAction empty() {
    return new WeaponAction(new ArrayList<>());
  }
}
