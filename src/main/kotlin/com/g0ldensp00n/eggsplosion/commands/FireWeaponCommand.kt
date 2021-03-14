package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.entities.Weapon
import org.bukkit.entity.Egg
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

public class FireWeaponCommand() {
  private fun prepare(itemStack: ItemStack): Weapon? {
    return Weapon.deserialize(itemStack)
  }

  companion object {
    @JvmStatic
    public fun run(player: Player, weapon: Weapon): Entity? {
      if (!weapon.hitscan) {
        val loc = player.location
        loc.world?.spawnEntity(loc, EntityType.EGG)?.let { entity ->
          if (entity is Egg) {
            entity.velocity = loc.direction.multiply(2)
          }
          return entity
        }
      }
      return null
    }
  }

  private fun persists() {
  }
}
