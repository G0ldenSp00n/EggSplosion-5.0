package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.commands.FireWeaponCommand
import com.g0ldensp00n.eggsplosion.services.Util
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.Bukkit

public class PlayerUseWeaponHandler(val fireWeaponCommand: FireWeaponCommand): ListenerHandler() {
  @EventHandler
  public fun onPlayerInteract(playerInteractEvent: PlayerInteractEvent) {
    when(playerInteractEvent.action) {
      Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> run {
        playerInteractEvent.item?.let { item ->
          if (Util.getLoreValue("function", item) == "weapon") {
            fireWeaponCommand.execute(playerInteractEvent.player, item)
            playerInteractEvent.setCancelled(true)
          }
        }
      }
    }
  }
}
