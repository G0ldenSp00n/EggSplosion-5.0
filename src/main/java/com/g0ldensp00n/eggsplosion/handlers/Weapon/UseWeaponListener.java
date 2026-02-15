package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

class UseWeaponListener implements Listener {
  public UseWeaponListener() {
    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  @EventHandler
  public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {
    if (playerInteractEvent.getItem() != null) {
      ItemStack item = playerInteractEvent.getItem();

      if (Weapon.isWeapon(item)) {
        playerInteractEvent.setCancelled(true);
        NamespacedKey weaponID = Weapon.getWeaponID(item);
        Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);
        Action interaction = playerInteractEvent.getAction();
        if (interaction.isLeftClick() ||
            interaction.isRightClick()) {
          Player player = playerInteractEvent.getPlayer();
          if (interaction.isLeftClick()
              && (Weapon.isReloading(weapon.primaryAction, item) || !weapon.primaryAction.hasEffect())) {
            return;
          } else if (interaction.isRightClick()
              && (Weapon.isReloading(weapon.secondaryAction, item) || !weapon.secondaryAction.hasEffect())) {
            return;
          }

          if (!(player.hasPotionEffect(PotionEffectType.INVISIBILITY)
              && player.hasPotionEffect(PotionEffectType.REGENERATION))) {
            if (interaction.isLeftClick()) {
              weapon.fireWeapon(player, item, weapon.primaryAction);
            } else if (interaction.isRightClick()) {
              weapon.fireWeapon(player, item, weapon.secondaryAction);
            }
          } else {
            Component spawnProtectionWarning = MiniMessage.miniMessage()
                .deserialize("Can't Fire Weapon with Spawn Protection");
            player.sendActionBar(spawnProtectionWarning);
          }
        }
      }
    }
  }

  @EventHandler
  public void playerCrouchEvent(PlayerToggleSneakEvent playerToggleSneakEvent) {
    Player player = playerToggleSneakEvent.getPlayer();
    if (!playerToggleSneakEvent.isSneaking()) {
      return;
    }

    ItemStack heldItem = player.getInventory().getItemInMainHand();
    if (Weapon.isWeapon(heldItem)) {
      NamespacedKey weaponID = Weapon.getWeaponID(heldItem);
      Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);
      if ((Weapon.isReloading(weapon.sneakAction, heldItem) || !weapon.sneakAction.hasEffect())) {
        return;
      }
      if (!(player.hasPotionEffect(PotionEffectType.INVISIBILITY)
          && player.hasPotionEffect(PotionEffectType.REGENERATION))) {
        weapon.fireWeapon(player, heldItem, weapon.sneakAction);
      }

    }
  }

  private void resetWeaponIfInvalid(ItemStack item) {
    if (Weapon.isWeapon(item)) {
      NamespacedKey weaponID = Weapon.getWeaponID(item);
      Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);

      weapon.primaryAction.resetReloadIfInvalid(item);
      weapon.secondaryAction.resetReloadIfInvalid(item);
      weapon.sneakAction.resetReloadIfInvalid(item);
    }
  }

  @EventHandler
  public void playerChangeHeldItem(PlayerItemHeldEvent playerItemHeldEvent) {
    Player player = playerItemHeldEvent.getPlayer();
    int newSlot = playerItemHeldEvent.getNewSlot();
    ItemStack heldItem = player.getInventory().getItem(newSlot);
    if (heldItem == null) {
      return;
    }
    resetWeaponIfInvalid(heldItem);
  }

  @EventHandler
  public void playerJoin(PlayerJoinEvent playerJoinEvent) {
    Player player = playerJoinEvent.getPlayer();
    ItemStack heldItem = player.getInventory().getItemInMainHand();
    if (heldItem == null) {
      return;
    }
    resetWeaponIfInvalid(heldItem);
  }

}
