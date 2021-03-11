package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.PlayerStateRepository
import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.MapPlayerMode
import com.g0ldensp00n.eggsplosion.entities.PlayerState
import org.bukkit.entity.Player
import org.bukkit.command.CommandSender

private data class PrepareResponse(val gameMap: GameMap?, val currentGameMap: GameMap?, val mapPlayerState: PlayerState?)

public class EditGameMapCommand(val gameMapRepository: GameMapRepository, val playerStateRepository: PlayerStateRepository) {
  private fun prepare(name: String, player: Player): PrepareResponse {
    val gameMap = gameMapRepository.findGameMapByName(name)
    val currentGameMap = gameMapRepository.findGameMapByPlayer(player)
    val mapPlayerState = playerStateRepository.findPlayerStateByGameMapAndUUID(name, player.uniqueId.toString())
    return PrepareResponse(gameMap, currentGameMap, mapPlayerState)
  }

  companion object {
    @JvmStatic
    fun run(gameMapToJoin: GameMap, currentGameMap: GameMap?, player: Player, mapPlayerState: PlayerState?) {
      currentGameMap?.let {
        if (currentGameMap.name == gameMapToJoin.name) {
          throw IllegalStateException("Player should never edit a gamemap they are already editing")
        }
      }
      gameMapToJoin.addPlayerToMap(player, MapPlayerMode.EDITOR, mapPlayerState)
    }
  }

  private fun persist(currentGameMap: GameMap?, oldPlayerState: PlayerState, player: Player) {
    currentGameMap?.let {
      playerStateRepository.addPlayerStateByGameMapAndUUID(currentGameMap.name, player.uniqueId.toString(), oldPlayerState)
    }
  }

  fun execute(name: String, sender: CommandSender) {
    if (sender is Player) {
      val prepareResponse = prepare(name, sender)

      val currentState = SavePlayerStateCommand.run(sender)
      try {
        prepareResponse.gameMap?.let {
          EditGameMapCommand.run(prepareResponse.gameMap, prepareResponse.currentGameMap, sender, prepareResponse.mapPlayerState)
        } ?: run {
          sender.sendMessage("[Map Editor] Could not find map " + name)
        }
      } catch (e: IllegalStateException) {
        sender.sendMessage("[Map Editor] You are already editing that map")
      }

      persist(prepareResponse.currentGameMap, currentState, sender)
    }
  }
}

