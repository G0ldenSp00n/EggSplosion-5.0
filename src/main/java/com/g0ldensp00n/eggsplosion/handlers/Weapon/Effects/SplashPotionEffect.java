package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.Collection;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class SplashPotionEffect extends WeaponEffect {
  Collection<PotionEffect> potionEffects;
  double potionRadius = 4.0;
  boolean affectsOwnTeam = false;
  boolean affectsEnemyTeam = true;
  boolean showSplashEffect = true;
  boolean doFalloff = true;

  public SplashPotionEffect(Collection<PotionEffect> potionEffects) {
    this.potionEffects = potionEffects;
  }

  public SplashPotionEffect(Collection<PotionEffect> potionEffects, double potionRadius) {
    this.potionEffects = potionEffects;
    this.potionRadius = potionRadius;
  }

  public SplashPotionEffect affectsEnemyTeam(boolean affectsEnemyTeam) {
    this.affectsEnemyTeam = affectsEnemyTeam;
    return this;
  }

  public SplashPotionEffect affectsOwnTeam(boolean affectsOwnTeam) {
    this.affectsOwnTeam = affectsOwnTeam;
    return this;
  }

  public SplashPotionEffect withHiddenSplashEffect() {
    this.showSplashEffect = false;
    return this;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    double potionRadiusSq = potionRadius * potionRadius;
    if (showSplashEffect) {
      for (PotionEffect effect : potionEffects) {
        location.getWorld().playEffect(location, Effect.POTION_BREAK, effect.getType().getColor());
      }
    }

    for (LivingEntity entity : location.getWorld().getNearbyLivingEntities(location, potionRadius, potionRadius,
        potionRadius)) {
      if (entity.getLocation().distanceSquared(location) > potionRadiusSq)
        continue;

      if (entity instanceof Player) {
        Player victim = (Player) entity;
        double intensity = 1.0;
        if (!doFalloff) {
          intensity = 1.0 - (entity.getLocation().distance(location) / potionRadius);
        }

        for (PotionEffect effect : potionEffects) {
          int originalDurration = effect.getDuration();
          int newDuration = (int) (originalDurration * intensity);

          if ((affectsOwnTeam && !EggSplosion.getInstance().getLobbyManager().canPlayerAttackPlayer(victim,
              shooter))
              || (affectsEnemyTeam && EggSplosion.getInstance().getLobbyManager().canPlayerAttackPlayer(victim,
                  shooter))) {

            effect.withDuration(newDuration).apply(entity);
          }
        }
      }
    }
  }
}
