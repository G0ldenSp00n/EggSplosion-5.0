package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.commands.CreateSpawnPointCommand
import com.g0ldensp00n.eggsplosion.commands.ShowMapSpawnPointsCommand
import com.g0ldensp00n.eggsplosion.commands.LoadPaginatedMenuCommand
import com.g0ldensp00n.eggsplosion.services.Util
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Inventory
import org.bukkit.Bukkit

public class PlayerUseMapToolHandler(val createSpawnPointCommand: CreateSpawnPointCommand, val showMapSpawnPointsCommand: ShowMapSpawnPointsCommand, val loadPaginatedMenuCommand: LoadPaginatedMenuCommand): ListenerHandler() {
  @EventHandler
  public fun onPlayerInteract(playerInteractEvent: PlayerInteractEvent) { 
    try {
      playerInteractEvent.item?.let { item: ItemStack ->
        when(item.type) {
          Material.LEATHER_BOOTS -> run {
            if (Util.getLoreValue("function", item) == "spawnTool") {
              when(playerInteractEvent.action) {
                Action.RIGHT_CLICK_BLOCK -> {
                  playerInteractEvent.clickedBlock?.let { clickedBlock ->
                    val player = playerInteractEvent.player
                    createSpawnPointCommand.execute(player, clickedBlock.x.toDouble(), clickedBlock.y.toDouble() + 1, clickedBlock.z.toDouble(), player.location.pitch, player.location.yaw, Util.getLoreValue("team", item) ?: throw IllegalArgumentException("Invalid Spawn Tool"))
                    playerInteractEvent.setCancelled(true)
                  }
                }
                Action.LEFT_CLICK_AIR -> {
                  Util.getLoreValue("team", item)?.let { teamName ->
                    showMapSpawnPointsCommand.execute(playerInteractEvent.player, teamName)
                  }
                }
              }
            }
          }
          Material.EMERALD -> run {
            val inv = Bukkit.getServer().createInventory(null, 27) 
            val itemStacks = ArrayList<ItemStack>()
            val itemStack = ItemStack(Material.MAP)
            for (i in 0..35) {
              itemStacks.add(itemStack)
            }

            loadPaginatedMenuCommand.execute(itemStacks, inv, 0, playerInteractEvent.player)
          }
          else -> {}
        }
      }
    } catch (illegalArgumentException: IllegalAccessException) {
      playerInteractEvent.player?.sendMessage(illegalArgumentException.message ?: "")
    }
  }
}
