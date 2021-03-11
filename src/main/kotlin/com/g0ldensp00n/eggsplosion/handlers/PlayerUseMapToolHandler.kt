package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.commands.CreateSpawnPointCommand
import com.g0ldensp00n.eggsplosion.handlers.ListenerHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

public class PlayerUseMapToolHandler(val createSpawnPointCommand: CreateSpawnPointCommand): ListenerHandler() {
  @EventHandler
  public fun onPlayerInteract(playerInteractEvent: PlayerInteractEvent) { 
    try {
      playerInteractEvent.item?.let { item: ItemStack ->
        when(item.type) {
          Material.LEATHER_BOOTS -> {
            item.itemMeta?.lore?.let { lore: List<String?> ->
              if (lore.getOrNull(0) == "function:spawnTool") {
                if (playerInteractEvent.action == Action.RIGHT_CLICK_BLOCK) {
                  playerInteractEvent.clickedBlock?.let { clickedBlock ->
                    val player = playerInteractEvent.player
                    createSpawnPointCommand.execute(player, clickedBlock.x.toDouble(), clickedBlock.y.toDouble() + 1, clickedBlock.z.toDouble(), player.location.pitch, player.location.yaw, lore.getOrNull(2)?.split(":".toRegex())?.getOrNull(1) ?: throw IllegalArgumentException("Invalid Spawn Tool"))
                    playerInteractEvent.setCancelled(true)
                  }
                }
              }
            }
          }
          else -> {}
        }
      }
    } catch (illegalArgumentException: IllegalAccessException) {
      playerInteractEvent.player?.sendMessage(illegalArgumentException.message ?: "")
    }
  }
}
