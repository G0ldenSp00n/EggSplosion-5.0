package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;
//SOUNDS ?
//
// Sound dashSound = Sound.ENTITY_ILLUSIONER_MIRROR_MOVE;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects.EffectListeners.KnockbackEffectListener;

public class DashEffect extends WeaponEffect {
  float dashPower;

  public DashEffect(float dashPower) {
    this.dashPower = dashPower;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    shooter.setVelocity(shooter.getLocation().getDirection().multiply(dashPower));
    shooter.setFallDistance(0);

    shooter.getPersistentDataContainer().set(KnockbackEffectListener.getWindChargeAnchorKey(),
        PersistentDataType.DOUBLE,
        location.getY());
  }
}
