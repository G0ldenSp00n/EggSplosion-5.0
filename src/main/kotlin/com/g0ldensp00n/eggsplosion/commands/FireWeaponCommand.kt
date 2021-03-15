package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.Main
import com.g0ldensp00n.eggsplosion.entities.Weapon
import org.bukkit.entity.Egg
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector

public class FireWeaponCommand(val plugin: Main) {
  private fun prepare(itemStack: ItemStack): Weapon? {
    return Weapon.deserialize(itemStack)
  }

  companion object {
    @JvmStatic
    public fun run(player: Player, weapon: Weapon, plugin: Main?): Entity? {
      plugin?.let {
        if (!weapon.hitscan) {
          val loc = player.location.add(player.location.direction.add(Vector(0.0, 1.5, 0.0)))
          loc.world?.spawnEntity(loc, EntityType.EGG)?.let { entity ->
            if (entity is Egg) {
              entity.velocity = loc.direction.multiply(2)
              entity.setMetadata("function", FixedMetadataValue(plugin, "weaponProjectile"))
              entity.setMetadata("explosionSize", FixedMetadataValue(plugin, weapon.explosionSize))
              if (weapon.fuse > 0) {
                object : BukkitRunnable() {
                  override fun run() {
                    entity.remove()
                  }
                }.runTaskLater(plugin, (20 * weapon.fuse).toLong())
              }
            }
            return entity
          }
        }
      }
      return null
    }
  }

  private fun persists() {
  }

  fun execute(player: Player, itemStack: ItemStack) {
    prepare(itemStack)?.let { weapon ->
      FireWeaponCommand.run(player, weapon, plugin)
    }
  }
}
