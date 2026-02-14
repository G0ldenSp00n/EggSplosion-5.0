package com.g0ldensp00n.eggsplosion.handlers.Weapon;

class WeaponActionBuilder {
  Collection<WeaponEffect> fireEffects;
  int fireReloadTicks;
  float fireVelocityMultiplier;
  Sound fireSound;

  public WeaponActionBuilder() {
    fireEffects = new ArrayList<>();
  }

  public static WeaponActionBuilder builder() {
    return new WeaponActionBuilder();
  }

  public void withReloadTime(int ticks) {
    this.fireReloadTicks = ticks;
  }

  public void withVelocityMultiplier(float velocityMultiplier) {
    this.velocityMultiplier = velocityMultiplier;
  }

  public void withFireSound(Sound sound) {
    this.fireSound = sound;
  }

  public void addEffect(WeaponEffect effect) {
    this.fireEffects.add(effect);
  }

  public WeaponAction build() {
    if (fireReloadTicks == null) {
      fireReloadTicks = 10;
    }

    if (fireVelocityMultiplier == null) {
      fireVelocityMultiplier = 1.0f;
    }

    if (fireSound == null) {
      fireSound = Sound.ENTITY_CHICKEN_EGG;
    }

    return new WeaponAction(fireEffects, fireReloadTicks, fireVelocityMultiplier, fireSound);
  }
}
