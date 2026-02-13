package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.EffectListeners.KnockbackEffectListener;

public class WeaponRegistry implements CommandExecutor {
  private static WeaponRegistry instance;

  private HashMap<NamespacedKey, Weapon> weapons;

  public WeaponRegistry() {
    instance = this;

    new EffectListener();
    new ReloadAnimation();
    new UseWeaponListener();
    new KnockbackEffectListener();

    weapons = new HashMap<>();
  }

  protected void registerWeapons() {
    NamespacedKey woodenHoeID = new NamespacedKey(EggSplosion.getInstance(), "wooden_hoe");
    Weapon woodenHoe = new Weapon(woodenHoeID, new ArrayList<>(), new ArrayList<>());
  }

  public static WeaponRegistry getInstance() {
    return instance;
  }

  public void register(Weapon weapon) {
    this.weapons.put(weapon.weaponID, weapon);
  }

  public Weapon getWeaponByID(NamespacedKey key) {
    return weapons.get(key);
  }

  @Override
  public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String commandLabel,
      @NotNull String @NotNull [] args) {
    if (commandLabel.equalsIgnoreCase("weapon")) {
    }
    return true;
  }
}
