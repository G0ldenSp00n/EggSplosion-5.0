package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.ExplosionEffect;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.KnockbackEffect;
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
    registerLegacyWeapons();
  }

  protected void registerLegacyWeapons() {
    NamespacedKey woodenHoeID = new NamespacedKey(EggSplosion.getInstance(), "wooden_hoe");
    ArrayList<WeaponEffect> woodSecondaryFireEffects = new ArrayList<>();
    woodSecondaryFireEffects.add(
        new ExplosionEffect(1.25f, Particle.EXPLOSION));
    WeaponAction woodSecondaryAction = new WeaponAction(woodSecondaryFireEffects, 3, 2.75f, Sound.ENTITY_CHICKEN_EGG);
    Weapon woodenHoe = new Weapon(woodenHoeID, Material.WOODEN_HOE, WeaponAction.empty(), woodSecondaryAction);
    register(woodenHoe);

    NamespacedKey stoneHoeID = new NamespacedKey(EggSplosion.getInstance(),
        "stone_hoe");
    ArrayList<WeaponEffect> stoneSecondaryFireEffects = new ArrayList<>();
    stoneSecondaryFireEffects.add(
        new ExplosionEffect(2.4f, Particle.EXPLOSION));
    stoneSecondaryFireEffects.add(
        new KnockbackEffect(2));
    WeaponAction stoneSecondaryAction = new WeaponAction(stoneSecondaryFireEffects, 50, 1.5f,
        Sound.ENTITY_CHICKEN_EGG);
    Weapon stoneHoe = new Weapon(stoneHoeID, Material.STONE_HOE,
        WeaponAction.empty(), stoneSecondaryAction);
    register(stoneHoe);

    NamespacedKey copperHoeID = new NamespacedKey(EggSplosion.getInstance(), "copper_hoe");
    ArrayList<WeaponEffect> copperSecondaryFireEffects = new ArrayList<>();
    copperSecondaryFireEffects.add(
        new ExplosionEffect(2.5f, Particle.EXPLOSION));
    copperSecondaryFireEffects.add(
        new KnockbackEffect(1));
    WeaponAction copperSecondaryAction = new WeaponAction(copperSecondaryFireEffects, 20, 4.2f,
        Sound.ENTITY_CHICKEN_EGG);
    Weapon copperHoe = new Weapon(copperHoeID, Material.COPPER_HOE, WeaponAction.empty(), copperSecondaryAction);
    register(copperHoe);

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

      if (sender instanceof Player) {
        Player player = (Player) sender;
        NamespacedKey woodenHoeID = new NamespacedKey(EggSplosion.getInstance(), "wooden_hoe");
        player.give(getWeaponByID(woodenHoeID).getItem());

        NamespacedKey stoneHoeID = new NamespacedKey(EggSplosion.getInstance(), "stone_hoe");
        player.give(getWeaponByID(stoneHoeID).getItem());

        NamespacedKey copperHoeID = new NamespacedKey(EggSplosion.getInstance(), "copper_hoe");
        player.give(getWeaponByID(copperHoeID).getItem());

      }
    }
    return true;
  }
}
