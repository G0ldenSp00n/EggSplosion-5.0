package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.EffectListeners;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

public class KnockbackEffectListener implements Listener {
  private static NamespacedKey windChargeAnchorKey = new NamespacedKey(EggSplosion.getInstance(),
      "wind_charge_anchor");

  public KnockbackEffectListener() {
    Bukkit.getPluginManager().registerEvents(this, EggSplosion.getInstance());
  }

  public static NamespacedKey getWindChargeAnchorKey() {
    return windChargeAnchorKey;
  }

  @EventHandler
  public void onFallDamage(EntityDamageEvent event) {
    if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
      return;
    if (!(event.getEntity() instanceof LivingEntity))
      return;

    LivingEntity entity = (LivingEntity) event.getEntity();

    if (entity.getPersistentDataContainer().has(windChargeAnchorKey)) {
      double startY = entity.getPersistentDataContainer().get(windChargeAnchorKey,
          PersistentDataType.DOUBLE);
      double landY = entity.getLocation().getY();

      entity.getPersistentDataContainer().remove(windChargeAnchorKey);
      if (landY >= startY) {
        event.setCancelled(true);
      } else {
        double distanceFallen = startY - landY;
        double newDamage = distanceFallen - 3.0;

        if (newDamage <= 0) {
          event.setCancelled(true);
        } else {
          event.setDamage(newDamage);
        }
      }

    }
  }
}
