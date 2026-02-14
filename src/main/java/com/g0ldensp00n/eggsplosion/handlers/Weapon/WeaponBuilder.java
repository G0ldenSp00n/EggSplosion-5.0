package com.g0ldensp00n.eggsplosion.handlers.Weapon;

class WeaponBuilder {
  NamespacedKey weaponID;
  Material projectile;
  Material item;
  WeaponAction primaryAction;
  WeaponAction secondaryAction;


  public WeaponBuilder(String weaponID) {
    weaponID = new NamespacedKey(EggSplosion.getInstance(), "weapon_id");
  }

  public static WeaponBuilder builder(String weaponID) {
    return new WeaponBuilder(weaponID);
  }

  public WeaponBuilder withProjectile(Material projectile) {
    this.projectile = projectile;
    return this;
  }

  public WeaponBuilder withWeaponItemMaterial(Material wepaonItem) {
    this.item = weaponItem;
    return this;
  }

  public WeaponBuilder withPrimaryAction(WeaponAction primaryAction) {
    this.primaryAction = primaryAction;
    return this;
  }

  public WeaponBuilder withSecondaryAction(WeaponAction secondaryAction) {
    this.secondaryAction = secondaryAction;
    return this;
  }

  public Weapon build() {
    if (item == null) {
      item = Material.WOODEN_HOE;
    }

    if (primaryAction == null) {
      primaryAction = WeaponAction.empty();
    }

    if (secondaryAction == null) {
      secondaryAction = WeaponAction.empty();
    }

    if (projectile == null) {
      projectile = Material.EGG;
    }

    return new Weapon(weaponID, item, primaryAction, secondaryAction, projectile);
  }
}
