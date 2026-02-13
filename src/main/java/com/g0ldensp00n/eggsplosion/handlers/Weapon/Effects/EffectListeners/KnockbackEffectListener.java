package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.EffectListeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

import com.g0ldensp00n.eggsplosion.EggSplosion;

public class KnockbackEffectListener implements Listener {
  @EventHandler
  public void onFallDamage(EntityDamageEvent event) {
    if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
      return;
    if (!(event.getEntity() instanceof LivingEntity))
      return;

    LivingEntity entity = (LivingEntity) event.getEntity();

    NamespacedKey windChargeAnchorKey = new NamespacedKey(EggSplosion.getInstance(),
        "windChargeAnchor");
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
