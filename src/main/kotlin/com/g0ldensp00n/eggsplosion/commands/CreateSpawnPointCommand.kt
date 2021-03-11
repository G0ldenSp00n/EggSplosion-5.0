package com.g0ldensp00n.eggsplosion.commands;

import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.Position
import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.SpawnPointRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import com.g0ldensp00n.eggsplosion.entities.SpawnPoint
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.commands.ListenerCommand
import com.g0ldensp00n.eggsplosion.Main
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Bukkit

private data class CreateSpawnPointPrepareResponse(val gameMap: GameMap?, val position: Position, val teamConfig: TeamConfig?, val spawnPoints: ArrayList<SpawnPoint>);

public class CreateSpawnPointCommand(val gameMapRepository: GameMapRepository, val spawnPointRepository: SpawnPointRepository, val teamConfigRepository: TeamConfigRepository): ListenerCommand() {
  private fun prepare(player: Player, x: Double, y: Double, z: Double, pitch: Float, yaw: Float, teamName: String): CreateSpawnPointPrepareResponse {
    val gameMap = gameMapRepository.findGameMapByPlayer(player)
    val position = Position(x, y, z, pitch, yaw)
    val teamConfig = teamConfigRepository.findTeamsByMapNameAndTeamName(gameMap?.name ?: "", teamName)
    var spawnPoints: ArrayList<SpawnPoint> = ArrayList<SpawnPoint>()
    gameMap?.name?.let { mapName ->
      spawnPoints = spawnPointRepository.findSpawnPointsByMapAndTeamName(gameMap?.name ?: "", teamName) ?: ArrayList<SpawnPoint>()
    }
    return CreateSpawnPointPrepareResponse(gameMap, position, teamConfig, spawnPoints);
  }

  companion object {
    @JvmStatic
    fun run(gameMap: GameMap, position: Position): SpawnPoint {
      val spawnPoint = SpawnPoint(position);
      return spawnPoint
    }
  }

  private fun persist(gameMap: GameMap, teamConfig: TeamConfig, spawnPoint: SpawnPoint) {
    gameMap.world?.let { world ->
      plugin?.let { plugin -> 
        spawnPointRepository.addSpawnPointToTeam(gameMap.name, teamConfig.name, spawnPoint)
      }
    }
  }

  fun execute(player: Player, x: Double, y: Double, z: Double, pitch: Float, yaw: Float, teamName: String) {
    val prepareResponse = prepare(player, x, y, z, pitch, yaw, teamName)
    prepareResponse.gameMap?.let { gameMap ->
      prepareResponse.teamConfig?.let { teamConfig ->
        val spawnPoint = CreateSpawnPointCommand.run(gameMap, prepareResponse.position)
        prepareResponse.spawnPoints.add(spawnPoint)
        ShowMapSpawnPointsCommand.run(prepareResponse.spawnPoints, gameMap, teamConfig, plugin)
        persist(gameMap, teamConfig, spawnPoint)
      } ?: {
        player.sendMessage("Team " + teamName + " does not exist")
      }
    }
  }
}
