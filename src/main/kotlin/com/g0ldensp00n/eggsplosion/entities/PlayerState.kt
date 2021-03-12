package com.g0ldensp00n.eggsplosion.entities

import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.entity.Player
import org.bukkit.entity.HumanEntity
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.serialization.ConfigurationSerializable

public data class Position(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float): ConfigurationSerializable {
  companion object {
    @JvmStatic
    fun deserialize(serializedPosition: Map<String, Object>): Position {
      serializedPosition.get("x")?.toString()?.toDouble()?.let { x ->
        serializedPosition.get("y")?.toString()?.toDouble()?.let { y ->
          serializedPosition.get("z")?.toString()?.toDouble()?.let { z ->
            serializedPosition.get("pitch")?.toString()?.toFloat()?.let { pitch ->
              serializedPosition.get("yaw")?.toString()?.toFloat()?.let { yaw ->
                return Position(x, y, z, pitch, yaw)
              }
            }
          }
        }
      }
      throw IllegalArgumentException("Invalid Serialized Position passed to serialize position constructor")
    }
  }

  fun toLocation(world: World): Location {
    val location = Location(world, x, y, z, yaw, pitch)
    return location;
  }

  override fun serialize(): Map<String, Object> {
    return mapOf("x" to x as Object, "y" to y as Object, "z" to z as Object, "pitch" to pitch as Object, "yaw" to yaw as Object);
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

