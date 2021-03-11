package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.repositories.PlayerStateRepository
import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.entities.PlayerState
import com.g0ldensp00n.eggsplosion.entities.GameMap
import org.bukkit.entity.Player


public class LoadPlayerStateCommand (val playerStateRepository: PlayerStateRepository) {
  fun prepare(player: Player, gameMapName: String): PlayerState? {
    val playerState = playerStateRepository.findPlayerStateByGameMapAndUUID(gameMapName, player.uniqueId.toString())

    return playerState
  }

  companion object {
    @JvmStatic
    fun run(player: Player, playerState: PlayerState) {
      playerState.applyToPlayer(player, null)
    }
  }

  public fun execute(player: Player, gameMapName: String) {
    val playerState = prepare(player, gameMapName)

    playerState?.let {
     LoadPlayerStateCommand.run(player, playerState)
    }
  }
}
