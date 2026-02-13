package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

class SplashPotionEffect extends WeaponEffect {
  Collection<PotionEffect> potionEffects;
  double potionRadius = 4.0;

  public SplashPotionEffect(Collection<PotionEffect> potionEffects) {
    this.potionEffects = potionEffects;
  }

  public SplashPotionEffect(Collection<PotionEffect> potionEffects, double potionRadius) {
    this.potionEffects = potionEffects;
    this.potionRadius = potionRadius;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    double potionRadiusSq = potionRadius * potionRadius;
    for (LivingEntity entity : location.getWorld().getNearbyLivingEntities(location, potionRadius, potionRadius,
        potionRadius)) {
      if (entity.getLocation().distanceSquared(location) > potionRadiusSq)
        continue;

      if (entity instanceof Player) {
        Player victim = (Player) entity;

        double intensity = 1.0 - (entity.getLocation().distance(location) / potionRadius);
        int originalDurration = 100;
        int newDuration = (int) (originalDurration * intensity);

        if (EggSplosion.getInstance().getLobbyManager().canPlayerAttackPlayer(victim,
            shooter)) {
          for (PotionEffect effect : potionEffects) {
            effect.withDuration(newDuration).apply(entity);
          }
        }
      }
    }
  }
}
