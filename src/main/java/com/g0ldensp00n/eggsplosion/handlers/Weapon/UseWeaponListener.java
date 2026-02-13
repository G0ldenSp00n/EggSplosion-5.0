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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

class UseWeaponListener implements Listener {
  @EventHandler
  public void playerInteractEvent(PlayerInteractEvent playerInteractEvent) {
    if (playerInteractEvent.getItem() != null) {
      ItemStack item = playerInteractEvent.getItem();
      NamespacedKey weaponIDKey = new NamespacedKey(EggSplosion.getInstance(), "weapon_id");
      NamespacedKey primaryFireReloadKey = new NamespacedKey(EggSplosion.getInstance(), "primary_fire_reloaded_after");
      NamespacedKey secondaryFireReloadKey = new NamespacedKey(EggSplosion.getInstance(),
          "secondary_fire_reloaded_after");

      if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(weaponIDKey)) {
        NamespacedKey weaponID = NamespacedKey.fromString(
            item.getItemMeta().getPersistentDataContainer().get(weaponIDKey, PersistentDataType.STRING));
        Weapon weapon = WeaponRegistry.getInstance().getWeaponByID(weaponID);
        Action interaction = playerInteractEvent.getAction();
        if (interaction.isLeftClick() ||
            interaction.isRightClick()) {
          Player player = playerInteractEvent.getPlayer();
          if (!(player.hasPotionEffect(PotionEffectType.INVISIBILITY)
              && player.hasPotionEffect(PotionEffectType.REGENERATION))) {
            playerInteractEvent.setCancelled(true);
            Egg egg = player.launchProjectile(Egg.class);

            egg.setItem(new ItemStack(weapon.projectile));

            egg.getPersistentDataContainer().set(weaponIDKey, PersistentDataType.STRING, weaponID.asString());
            NamespacedKey isWeaponPrimaryFireKey = new NamespacedKey(EggSplosion.getInstance(),
                "is_weapon_primary_fire");
            if (interaction.isLeftClick()) {
              player.playSound(player.getLocation(), weapon.primaryFireSound, SoundCategory.PLAYERS, 0.2f, 1f);
              egg.getPersistentDataContainer().set(isWeaponPrimaryFireKey, PersistentDataType.BOOLEAN, true);
              ItemMeta meta = item.getItemMeta();
              meta.getPersistentDataContainer().set(primaryFireReloadKey, PersistentDataType.INTEGER,
                  Bukkit.getServer().getCurrentTick() + weapon.primaryFireReloadTicks);
              item.setItemMeta(meta);
            } else if (interaction.isRightClick()) {
              player.playSound(player.getLocation(), weapon.secondaryFireSound, SoundCategory.PLAYERS, 0.2f, 1f);
              egg.getPersistentDataContainer().set(isWeaponPrimaryFireKey, PersistentDataType.BOOLEAN, false);
              item.getItemMeta().getPersistentDataContainer().set(secondaryFireReloadKey, PersistentDataType.INTEGER,
                  Bukkit.getServer().getCurrentTick() + weapon.secondaryFireReloadTicks);

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
