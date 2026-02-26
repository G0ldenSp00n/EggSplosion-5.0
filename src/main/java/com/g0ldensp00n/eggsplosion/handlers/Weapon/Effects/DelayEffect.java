package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

public class DelayEffect extends WeaponEffect {
  int tickDelay;
  Collection<WeaponEffect> effectsToDelay;

  public DelayEffect(int tickDelay, Collection<WeaponEffect> effectsToDelay) {
    this.tickDelay = tickDelay;
    this.effectsToDelay = effectsToDelay;
  }

  @Override
  public void activateEffect(Location location, Player shooter) {
    new BukkitRunnable() {
      @Override
      public void run() {
        for (WeaponEffect effect : effectsToDelay) {
          effect.activateEffect(location, shooter);
        }
      }
    }.runTaskLater(EggSplosion.getInstance(), tickDelay);
  }
}
