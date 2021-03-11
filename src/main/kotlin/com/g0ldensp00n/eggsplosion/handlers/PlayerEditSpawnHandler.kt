package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.handlers.ListenerHandler
import com.g0ldensp00n.eggsplosion.commands.RemoveSpawnPointCommand
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.Bukkit

public class PlayerEditSpawnHandler(val removeSpawnPointsCommand: RemoveSpawnPointCommand): ListenerHandler() {
  @EventHandler
  public fun onPlayerDestroyArmorStand(entityDeathEvent: EntityDeathEvent) {
    val spawnLocation = entityDeathEvent.entity.location
    entityDeathEvent?.entity?.getMetadata("gameMap")?.let { gameMapMeta ->
      if (gameMapMeta.size > 0) {
        gameMapMeta.get(0)?.asString()?.let { mapName ->
          entityDeathEvent?.entity?.getMetadata("team")?.let { teamMeta ->
            if (teamMeta.size > 0) {
              teamMeta?.get(0)?.asString()?.let { teamName ->
                removeSpawnPointsCommand.execute(mapName, teamName, spawnLocation.blockX, spawnLocation.blockY, spawnLocation.blockZ)
              }
            }
          }
        }
      }
    }
  }
}
