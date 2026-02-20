package com.g0ldensp00n.eggsplosion.handlers.Weapon.Effects;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponEffect;

import io.papermc.paper.entity.TeleportFlag;

public class TeleportEffect extends WeaponEffect {
  Particle teleportParticle = Particle.PORTAL;

  @Override
  public void activateEffect(Location location, Player shooter) {
    float yaw = shooter.getYaw();
    float pitch = shooter.getPitch();
    shooter.teleport(location, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y,
        TeleportFlag.Relative.VELOCITY_Z);
    shooter.setRotation(yaw, pitch);

    shooter.getWorld().spawnParticle(teleportParticle, shooter.getLocation(), 1);
  }
}
