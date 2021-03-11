package com.g0ldensp00n.eggsplosion.handlers

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.Bukkit
import org.bukkit.plugin.PluginManager
import org.bukkit.command.CommandSender
import com.g0ldensp00n.eggsplosion.commands.EditGameMapCommand
import com.g0ldensp00n.eggsplosion.handlers.ListenerHandler

public class PlayerJoinHandler(val editGameMapCommand: EditGameMapCommand): ListenerHandler() {
  @EventHandler
  public fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
    val worldName = playerJoinEvent.player.getLocation().world?.name
    worldName?.let {
      if (playerJoinEvent.player is CommandSender) {
        editGameMapCommand.execute(worldName, playerJoinEvent.player)
      }
    }
  }
}
