package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;

class WeaponBuilder {
  NamespacedKey weaponID;
  Component displayName;
  Material item;
  WeaponAction primaryAction;
  WeaponAction secondaryAction;
  WeaponAction sneakAction;

  public WeaponBuilder(String weaponID) {
    this.weaponID = new NamespacedKey(EggSplosion.getInstance(), weaponID);
  }

  public WeaponBuilder withDisplayName(Component displayName) {
    this.displayName = displayName;
    return this;
  }

  public WeaponBuilder withWeaponItemMaterial(Material weaponItem) {
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

  public WeaponBuilder withSneakAction(WeaponAction sneakAction) {
    this.sneakAction = sneakAction;
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

    if (sneakAction == null) {
      sneakAction = WeaponAction.empty();
    }

    primaryAction.reloadingKey = WeaponRegistry.getPrimaryFireReloadAfterKey();
    secondaryAction.reloadingKey = WeaponRegistry.getSecondaryFireReloadAfterKey();
    sneakAction.reloadingKey = WeaponRegistry.getSneakActionReloadAfterKey();

    return new Weapon(weaponID, displayName, item, primaryAction, secondaryAction, sneakAction);
  }
}
