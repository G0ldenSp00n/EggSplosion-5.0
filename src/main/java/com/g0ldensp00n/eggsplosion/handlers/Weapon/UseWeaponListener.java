package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
        NamespacedKey weaponID = Weapon.getWeaponID(item);
        Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);
        Action interaction = playerInteractEvent.getAction();
        if (interaction.isLeftClick() ||
            interaction.isRightClick()) {
          Player player = playerInteractEvent.getPlayer();
          if (interaction.isLeftClick() && (Weapon.isPrimaryReloading(item) || !weapon.hasPrimaryEffect())) {
            return;
          } else if (interaction.isRightClick()
              && (Weapon.isSecondaryReloading(item) || !weapon.hasSecondaryEffect())) {
            return;
          }

          if (!(player.hasPotionEffect(PotionEffectType.INVISIBILITY)
              && player.hasPotionEffect(PotionEffectType.REGENERATION))) {
            playerInteractEvent.setCancelled(true);
            if (interaction.isLeftClick()) {
              weapon.firePrimary(player, item);
            } else if (interaction.isRightClick()) {
              weapon.fireSecondary(player, item);
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

  private void resetWeaponIfInvalid(ItemStack item) {
    if (Weapon.isWeapon(item)) {
      NamespacedKey weaponID = Weapon.getWeaponID(item);
      Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);

      NamespacedKey primaryFireReloadKey = new NamespacedKey(EggSplosion.getInstance(), "primary_fire_reloaded_after");
      int primaryReloadLeft = Weapon.getSecondaryReloadTimeLeft(item);
      if (primaryReloadLeft > weapon.primaryAction.fireReloadTicks) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(primaryFireReloadKey, PersistentDataType.INTEGER,
            0);
        item.setItemMeta(meta);
      }

      NamespacedKey secondaryFireReloadKey = new NamespacedKey(EggSplosion.getInstance(),
          "secondary_fire_reloaded_after");
      int secondaryReloadLeft = Weapon.getSecondaryReloadTimeLeft(item);
      if (secondaryReloadLeft > weapon.secondaryAction.fireReloadTicks) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(secondaryFireReloadKey, PersistentDataType.INTEGER,
            0);
        item.setItemMeta(meta);
      }
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
