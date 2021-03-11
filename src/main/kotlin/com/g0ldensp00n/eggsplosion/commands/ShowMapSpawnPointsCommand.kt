package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.commands.ListenerCommand
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.SpawnPoint
import com.g0ldensp00n.eggsplosion.repositories.SpawnPointRepository
import com.g0ldensp00n.eggsplosion.Main
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Bukkit

public class ShowMapSpawnPointsCommand (val spawnPointRepository: SpawnPointRepository): ListenerCommand() {
  private fun prepare(gameMap: GameMap, team: TeamConfig) {
  }

  companion object {
    @JvmStatic
    fun run (spawnPoints: ArrayList<SpawnPoint>, gameMap: GameMap, teamConfig: TeamConfig, plugin: Main?) {
      plugin?.let {
        gameMap.world?.let { world ->
          spawnPoints.forEach { spawnPoint ->
            val hideEvent = object : BukkitRunnable() {
              override fun run() {
                  spawnPoint.hide()
              }
            }.runTaskLater(plugin, 20 * 20);
            spawnPoint.show(gameMap, teamConfig, hideEvent, plugin)
          }
        }
      }
    }
  }
}
