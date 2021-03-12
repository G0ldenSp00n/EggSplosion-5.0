package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.commands.ListenerCommand
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.SpawnPoint
import com.g0ldensp00n.eggsplosion.repositories.SpawnPointRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.Main
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Bukkit
import org.bukkit.entity.Player

private data class ShowMapSpawnPointsPrepareResponse(val gameMap: GameMap, val teamConfig: TeamConfig, val spawnPoints: ArrayList<SpawnPoint>?)

public class ShowMapSpawnPointsCommand (val gameMapRepository: GameMapRepository, val teamConfigRepository: TeamConfigRepository, val spawnPointRepository: SpawnPointRepository, val pluginMain: Main): ListenerCommand() {
  private fun prepare(player: Player, teamName: String): ShowMapSpawnPointsPrepareResponse {
    val gameMap = gameMapRepository.findGameMapByPlayer(player) ?: throw IllegalArgumentException("Player not in any valid map")
    val teamConfig = teamConfigRepository.findTeamsByMapNameAndTeamName(gameMap.name, teamName) ?: throw IllegalArgumentException("Supplied Team " + teamName + " could not be found")
    val spawns = spawnPointRepository.findSpawnPointsByMapAndTeamName(gameMap.name, teamName)

    return ShowMapSpawnPointsPrepareResponse(gameMap, teamConfig, spawns)
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

  public fun execute(player: Player, teamName: String) {
    try {
      prepare(player, teamName)?.let { prepareResponse ->
        prepareResponse?.spawnPoints?.let { spawns ->
          ShowMapSpawnPointsCommand.run(spawns, prepareResponse.gameMap, prepareResponse.teamConfig, pluginMain)
        }
      }
    } catch (e: IllegalArgumentException) {
      e.message?.let { message ->
        player.sendMessage("[Map Editor] " + message)
      }
    }
  }
}
