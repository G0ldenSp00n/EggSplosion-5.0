package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.entities.MapPlayerMode
import com.g0ldensp00n.eggsplosion.entities.PlayerState
import com.g0ldensp00n.eggsplosion.entities.Position
import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.repositories.PlayerStateRepository
import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.HumanEntity
import org.bukkit.Location


public class SavePlayerStateCommand (val playerStateRepository: PlayerStateRepository, val gameMapRepository: GameMapRepository) {
  fun prepare(player: Player): GameMap? {
    return gameMapRepository.findGameMapByPlayer(player)
  }

  companion object {
    @JvmStatic
    fun run(player: Player): PlayerState {
      if (player is HumanEntity) {
        val playerLocation: Location = player.getLocation()
        return PlayerState(
          MapPlayerMode.EDITOR,
          player.inventory.contents,
          player.inventory.armorContents,
          player.getGameMode(),
          Position(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(), playerLocation.getYaw(), playerLocation.getPitch()),
          player.isFlying
        )
      }
      throw IllegalArgumentException("Supplied Player isn't an instance of a HumanEntity")
    }
  }

  fun persist(currentGameMap: GameMap, player: Player, currentState: PlayerState) {
    playerStateRepository.addPlayerStateByGameMapAndUUID(currentGameMap.name, player.uniqueId.toString(), currentState)
  }

  fun execute(player: Player) {
    val gameMap = prepare(player)

    val playerState = SavePlayerStateCommand.run(player)

    gameMap?.let {
      persist(gameMap, player, playerState)
    }
  }
}
