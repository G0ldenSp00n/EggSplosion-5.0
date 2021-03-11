package com.g0ldensp00n.eggsplosion.entities

import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Player
import org.bukkit.entity.HumanEntity
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World

public data class Position(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float) {
  fun toLocation(world: World): Location {
    val location = Location(world, x, y, z, yaw, pitch)
    return location;
  }
}

public data class PlayerState(
  val mapPlayerMode: MapPlayerMode, 
  var inventoryContents: Array<ItemStack?>, 
  var armorContents: Array<ItemStack?>,
  var gameMode: GameMode, 
  var position: Position?, 
  var flying: Boolean
) {
  fun applyToPlayer(player: Player, world: World?) {
    if (player is HumanEntity) {
      player.gameMode = gameMode
      player.setFlying(flying)
      var playerInventory: PlayerInventory = player.inventory
      playerInventory.setContents(inventoryContents)
      playerInventory.setArmorContents(armorContents)
      world?.let { w ->
        position?.let { it ->
          player.teleport(it.toLocation(w))
        }
      }
    }
  }
}

