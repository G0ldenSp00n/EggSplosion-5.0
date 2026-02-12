package com.g0ldensp00n.eggsplosion.handlers.Weapon;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

class Weapon implements Listener {
  UUID weaponUUID;
  WeaponEffect primaryFire;
  WeaponEffect secondaryFire;
  Material projectile;

  protected Weapon(UUID weaponUUID, WeaponEffect primaryEffect, WeaponEffect secondaryEffect) {
    this.weaponUUID = weaponUUID;
    this.primaryFire = primaryEffect;
    this.secondaryFire = secondaryEffect;
  }

  @EventHandler
  public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {

    if (playerInteractEvent.getItem() != null) {
      ItemStack item = playerInteractEvent.getItem();
      NamespacedKey weaponUUIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_uuid");

      if (item.getItemMeta().getPersistentDataContainer().has(weaponUUIDKey)) {
        UUID weaponUUID = UUID
            .fromString(item.getItemMeta().getPersistentDataContainer().get(weaponUUIDKey, PersistentDataType.STRING));
        if (this.weaponUUID == weaponUUID) {
          Action interaction = playerInteractEvent.getAction();
          if (interaction.isLeftClick() ||
              interaction.isRightClick()) {
            Player player = playerInteractEvent.getPlayer();
            if (!(player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && player.hasPotionEffect(PotionEffectType.REGENERATION))) {
              Egg egg = player.launchProjectile(Egg.class);

              egg.setItem(new ItemStack(projectile));

              egg.getPersistentDataContainer().set(weaponUUIDKey, PersistentDataType.STRING, weaponUUID.toString());
              NamespacedKey isWeaponPrimaryFireKey = new NamespacedKey(EggSplosion.getInstance(),
                  "isWeaponPrimaryFireKey");
              if (interaction.isLeftClick()) {
                player.playSound(player.getLocation(), primaryFire.soundEffect, SoundCategory.PLAYERS, 0.2f, 1f);
                egg.getPersistentDataContainer().set(isWeaponPrimaryFireKey, PersistentDataType.BOOLEAN, true);
              } else if (interaction.isRightClick()) {
                player.playSound(player.getLocation(), secondaryFire.soundEffect, SoundCategory.PLAYERS, 0.2f, 1f);
                egg.getPersistentDataContainer().set(isWeaponPrimaryFireKey, PersistentDataType.BOOLEAN, false);
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
  }
}
