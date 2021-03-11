package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.handlers.ListenerHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.Bukkit

public class PlayerEditSpawnHandler(): ListenerHandler() {
  @EventHandler
  public fun onPlayerDestroyArmorStand(entityDeathEvent: EntityDeathEvent) {
    val gameMapName = entityDeathEvent.entity.getMetadata("gameMap")?.get(0)
    val teamName = entityDeathEvent.entity.getMetadata("team")?.get(0)
    gameMapName?.let {
      teamName?.let {
        Bukkit.getLogger().info(gameMapName.asString()+","+teamName.asString())
      }
    }
  }
}
