package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.Collection;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class SelfPotionEffect extends WeaponEffect {
  Collection<PotionEffect> potionEffects;
  boolean showSplashEffect = true;

  public SelfPotionEffect(Collection<PotionEffect> potionEffects) {
    this.potionEffects = potionEffects;
  }

  public SelfPotionEffect withHiddenSplashEffect() {
    this.showSplashEffect = false;
    return this;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    for (PotionEffect effect : potionEffects) {
      if (this.showSplashEffect) {
        location.getWorld().playEffect(location, Effect.POTION_BREAK, effect.getType().getColor());
      }
      effect.apply(shooter);
    }
  }
}
