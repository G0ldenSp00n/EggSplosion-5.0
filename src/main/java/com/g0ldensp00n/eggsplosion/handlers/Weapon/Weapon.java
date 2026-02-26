package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;

public class Weapon implements Listener {
  NamespacedKey weaponID;
  Component displayName;
  Material item;
  WeaponAction primaryAction;
  WeaponAction secondaryAction;
  WeaponAction sneakAction;

  protected Weapon(NamespacedKey weaponID, Material item, WeaponAction primaryAction,
      WeaponAction secondaryAction) {
    this.weaponID = weaponID;
    this.primaryAction = primaryAction;
    this.secondaryAction = secondaryAction;
    this.item = item;
    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  public static Weapon.Builder builder(String weaponID) {
    return new Weapon.Builder(weaponID);
  }

  protected Weapon(NamespacedKey weaponID, Component displayName, Material item, WeaponAction primaryAction,
      WeaponAction secondaryAction, WeaponAction sneakAction) {
    this.weaponID = weaponID;
    this.primaryAction = primaryAction;
    this.secondaryAction = secondaryAction;
    this.sneakAction = sneakAction;
    this.item = item;
    this.displayName = displayName;

    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  public ItemStack getItem() {
    ItemStack weapon = new ItemStack(item);
    weapon.editPersistentDataContainer(pdc -> {
      pdc.set(WeaponRegistry.getWeaponIDKey(),
          PersistentDataType.STRING,
          weaponID.asString());
    });
    ItemMeta weaponMeta = weapon.getItemMeta();
    UseCooldownComponent useCooldownComponent = weaponMeta.getUseCooldown();
    useCooldownComponent.setCooldownGroup(weaponID);
    weaponMeta.displayName(displayName);
    weaponMeta.setUseCooldown(useCooldownComponent);
    weaponMeta.setUnbreakable(true);

    weapon.setItemMeta(weaponMeta);
    return weapon;
  }

  private void setReloading(Player player, ItemStack weaponItem, WeaponAction action) {
    weaponItem.editPersistentDataContainer(pdc -> {
      pdc.set(action.reloadingKey, PersistentDataType.INTEGER,
          Bukkit.getServer().getCurrentTick() + action.fireReloadTicks);
    });
    if (action.isPrimaryAction()) {
      player.setCooldown(weaponID, primaryAction.fireReloadTicks);
    }
    if (action.hasReloadEffect()) {
      new BukkitRunnable() {
        @Override
        public void run() {
          for (WeaponEffect effect : action.reloadEffects) {
            effect.activateEffect(player.getLocation(), player);
          }
        }
      }.runTaskLater(EggSplosion.getInstance(), action.fireReloadTicks);
    }

  }

  public void spawnActionProjectile(Player player, WeaponAction action, Location launchLocation, Vector fireVelocity,
      int splitCount) {
    Projectile projectile = (Projectile) player.getWorld().spawn(launchLocation, action.projectile);
    projectile.setVelocity(fireVelocity);
    projectile.setShooter(player);

    if (action.hasTrailEffect()) {
      new BukkitRunnable() {
        @Override
        public void run() {
          if (projectile != null && !projectile.isDead()) {
            for (WeaponEffect trailEffect : action.trailEffects) {
              trailEffect.activateEffect(projectile.getLocation(), player);
            }
          } else {
            cancel();
          }
        }
      }.runTaskTimer(EggSplosion.getInstance(), action.trailEffectDelay, action.trailEffectTimer);
    }

    if (projectile instanceof Egg) {
      Egg egg = (Egg) projectile;
      egg.setItem(new ItemStack(action.projectileMaterial));
    } else if (projectile instanceof WitherSkull) {
      WitherSkull skull = (WitherSkull) projectile;
      skull.setCharged(action.isCharged);
    }

    projectile.setRotation(player.getYaw(), player.getPitch());
    projectile.getPersistentDataContainer().set(WeaponRegistry.getWeaponIDKey(), PersistentDataType.STRING,
        weaponID.asString());
    projectile.getPersistentDataContainer().set(WeaponRegistry.getIsWeaponPrimaryFireKey(),
        PersistentDataType.BOOLEAN,
        action.isPrimaryAction());
    projectile.getPersistentDataContainer().set(WeaponRegistry.getSplitCountKey(),
        PersistentDataType.INTEGER,
        splitCount);

    if (action.projectileMaxTicksLived != -1) {
      new BukkitRunnable() {
        @Override
        public void run() {
          if (projectile != null && !projectile.isDead()) {
            for (WeaponEffect effect : action.fireEffects) {
              effect.activateEffect(projectile.getLocation(), player);
            }
            projectile.remove();
          }
        }
      }.runTaskLater(EggSplosion.getInstance(), action.projectileMaxTicksLived);
    }
  }

  private void fire(Player player, WeaponAction action) {
    for (int projectileCount = 0; projectileCount < action.projectileCount; projectileCount += 1) {
      Vector fireAngle = player.getLocation().getDirection();
      if (action.projectileCount > 1) {
        Random random = new Random();
        // TODO: CUSTOMIZABLEW??
        float deflectionAmount = 0.14f;
        Vector offset = new Vector(random.nextFloat(-deflectionAmount, deflectionAmount),
            random.nextFloat(-deflectionAmount, deflectionAmount),
            random.nextFloat(-deflectionAmount, deflectionAmount));
        fireAngle = fireAngle.add(offset);
      }
      spawnActionProjectile(player, action, player.getEyeLocation(), fireAngle.multiply(action.fireVelocityMultiplier),
          0);
    }
  }

  public void fireBurst(int burstsRemaining, Player player, WeaponAction action) {
    new BukkitRunnable() {
      @Override
      public void run() {
        fire(player, action);
        if ((burstsRemaining - 1) > 0) {
          fireBurst(burstsRemaining - 1, player, action);
        }
      }
    }.runTaskLater(EggSplosion.getInstance(), action.burstDelayTicks);
  }

  public void fireWeapon(Player player, ItemStack weaponItem, WeaponAction action) {
    activateCastAction(player, weaponItem, action);
    if (action.hasFireEffect()) {
      fire(player, action);
      if (action.burstCount > 1) {
        fireBurst(action.burstCount - 1, player, action);
      }
    }
    setReloading(player, weaponItem, action);

  }

  private void activateCastAction(Player player, ItemStack weaponItem,
      WeaponAction action) {
    for (WeaponEffect effect : action.castEffects) {
      effect.activateEffect(player.getLocation(), player);
    }
  }

  public static boolean isWeapon(ItemStack item) {
    if (item.hasItemMeta() && item.getPersistentDataContainer().has(WeaponRegistry.getWeaponIDKey())) {
      return true;
    }
    return false;
  }

  public static NamespacedKey getWeaponID(ItemStack item) {
    if (isWeapon(item)) {
      NamespacedKey weaponID = NamespacedKey.fromString(
          item.getPersistentDataContainer().get(WeaponRegistry.getWeaponIDKey(), PersistentDataType.STRING));
      return weaponID;
    }
    return null;
  }

  public static boolean isReloading(WeaponAction action, ItemStack item) {
    int reloadingTimeLeft = getReloadTimeLeft(action, item);
    if (reloadingTimeLeft > 0) {
      return true;
    }
    return false;
  }

  public static int getReloadTimeLeft(WeaponAction action, ItemStack item) {
    if (item.getPersistentDataContainer().has(action.reloadingKey)) {
      int tickReloaded = item.getPersistentDataContainer().get(action.reloadingKey,
          PersistentDataType.INTEGER);
      return tickReloaded - Bukkit.getCurrentTick();
    }
    return 0;
  }

  public static class Builder {
    NamespacedKey weaponID;
    Component displayName;
    Material item;
    WeaponAction primaryAction;
    WeaponAction secondaryAction;
    WeaponAction sneakAction;

    public Builder(String weaponID) {
      this.weaponID = new NamespacedKey(EggSplosion.getInstance(), weaponID);
    }

    public Builder withDisplayName(Component displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder withWeaponItemMaterial(Material weaponItem) {
      this.item = weaponItem;
      return this;
    }

    public Builder withPrimaryAction(WeaponAction primaryAction) {
      this.primaryAction = primaryAction;
      return this;
    }

    public Builder withSecondaryAction(WeaponAction secondaryAction) {
      this.secondaryAction = secondaryAction;
      return this;
    }

    public Builder withSneakAction(WeaponAction sneakAction) {
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
}
