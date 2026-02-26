package com.g0ldensp00n.eggsplosion.handlers.Core;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.g0ldensp00n.eggsplosion.EggSplosion;

public class EggExplode implements Listener {

  public EggExplode(Plugin plugin) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void entityCollide(ProjectileHitEvent projectileHitEvent) {
    if (projectileHitEvent.getEntity().getType() == EntityType.EGG) {
      if (projectileHitEvent.getEntity().getName().split(" / ").length >= 2) {

        List<MetadataValue> values = projectileHitEvent.getEntity().getMetadata("rocket_jump_power");

        float explosionPower = Float.parseFloat(projectileHitEvent.getEntity().getName().split(" / ")[1]);
        Entity entityShooter = (Entity) projectileHitEvent.getEntity().getShooter();
        if (entityShooter instanceof Player) {
          Player playerShooter = (Player) entityShooter;
          World world = projectileHitEvent.getEntity().getWorld();
          Location location = projectileHitEvent.getEntity().getLocation();

          // int charge_count = 0;
          // if (explosionPower <= 2.5) {
          // charge_count = 1;
          // } else if (explosionPower < 3) {
          // charge_count = 2;
          // } else {
          // charge_count = 3;
          // }
          int charge_count = 0;
          if (!values.isEmpty()) {
            charge_count = values.get(0).asInt();
          }

          // for (int i = 0; i < charge_count; i++) {
          // WindCharge wind_charge = (WindCharge) world.spawnEntity(location.add(0, .75,
          // 0),
          // EntityType.WIND_CHARGE);
          // wind_charge.setShooter(playerShooter);
          // wind_charge.explode();
          // }

          world.createExplosion(location, explosionPower, false, true, (Entity) playerShooter);
          world.spawnParticle(Particle.EXPLOSION, location, 0);
          world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 3, 1);

          double radius = 4.0;
          double radiusSq = radius * radius;

          for (LivingEntity entity : location.getWorld().getNearbyLivingEntities(location, radius, radius, radius)) {
            // Check actual distance to ensure a spherical radius (bounding box check is
            // cubic)
            if (entity.getLocation().distanceSquared(location) > radiusSq)
              continue;

            // Apply the effects found in the potion meta
            double intensity = 1.0 - (entity.getLocation().distance(location) / radius);
            int originalDurration = 100;
            int newDuration = (int) (originalDurration * intensity);
            // TODO: Lobby can Attack
            if (entity != playerShooter) {
              entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, newDuration, 3, false));
            }

            Vector direction = entity.getLocation().toVector().subtract(location.toVector());

            if (direction.lengthSquared() == 0) {
              direction = new Vector(0, 0.5, 0);
            } else {
              direction.normalize();
            }

            direction.setY(direction.getY() + 0.25);

            double knockbackStrength = 0.75; // Adjust this value
            direction.multiply(knockbackStrength * intensity);

            entity.setVelocity(entity.getVelocity().add(direction));
            if (entity == playerShooter) {
              entity.setFallDistance(0);

            }
          }
        }
      }
    }
  }

}
