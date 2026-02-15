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
  Sound teleportSound = Sound.ENTITY_ENDERMAN_TELEPORT;
  Particle teleportParticle = Particle.PORTAL;

  @Override
  public void activateEffect(Location location, Player shooter) {
    float yaw = shooter.getYaw();
    float pitch = shooter.getPitch();
    shooter.teleport(location, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y,
        TeleportFlag.Relative.VELOCITY_Z);
    shooter.setRotation(yaw, pitch);

    shooter.getWorld().spawnParticle(teleportParticle, shooter.getLocation(), 1);
    Random random = new Random();
    shooter.getLocation().getWorld().playSound(shooter.getEyeLocation(), teleportSound,
        SoundCategory.HOSTILE, 1.0f, random.nextFloat(0.8f, 1.2f));

  }
}
