package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.commands.SavePlayerStateCommand
import com.g0ldensp00n.eggsplosion.commands.LoadPlayerStateCommand
import com.g0ldensp00n.eggsplosion.handlers.ListenerHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.EventHandler
import org.bukkit.Bukkit

public class PlayerWorldSwitchHandler(val savePlayerStateCommand: SavePlayerStateCommand, val loadPlayerStateCommand: LoadPlayerStateCommand): ListenerHandler() {
  @EventHandler
  public fun onPlayerTeleport(playerTeleportEvent: PlayerTeleportEvent) {
    if (playerTeleportEvent.from.world?.name !== playerTeleportEvent.to?.world?.name) {
      savePlayerStateCommand.execute(playerTeleportEvent.player)
      playerTeleportEvent.to?.world?.name?.let {
        loadPlayerStateCommand.execute(playerTeleportEvent.player, it)
      }
    }
  }
}
