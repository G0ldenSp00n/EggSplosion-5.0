package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import org.bukkit.entity.Player

public class CreateMapTeamCommand(val gameMapRepository: GameMapRepository, val teamConfigRepository: TeamConfigRepository){
  private fun prepare(gameMapName: String?, player: Player): GameMap {
    gameMapName?.let {
      val gameMap = gameMapRepository.findGameMapByName(gameMapName);
      gameMap?.let {
        return gameMap
      } ?: run {
        throw IllegalArgumentException("Supplied Game Map " + gameMapName + " could not be found")
      }
    } ?: run {
      return gameMapRepository.findGameMapByPlayer(player) ?: run {
        throw IllegalStateException("Supplied Player not found in any map")
      }
    }
  }

  companion object {
    @JvmStatic
    fun run(teamName: String, teamColor: String): TeamConfig {
      val teamConfig = TeamConfig(teamName, teamColor)
      return teamConfig
    }
  }

  private fun persist(gameMap: GameMap, teamConfig: TeamConfig) {
    teamConfigRepository.addTeamToGameMap(gameMap.name, teamConfig)
  }

  fun execute(player: Player, gameMapName: String?, teamName: String, teamColorHex: String) {
    try {
      val gameMap = prepare(gameMapName, player)
      val teamConfig = CreateMapTeamCommand.run(teamName, teamColorHex)
      persist(gameMap, teamConfig)
    } catch (illegalArgumentException: IllegalArgumentException) {
      player.sendMessage("[Map Editor] " + illegalArgumentException.message)
    } catch (illegalStateException: IllegalStateException) {
      player.sendMessage("[Map Editor] " + illegalStateException.message)
    }
  }
}
