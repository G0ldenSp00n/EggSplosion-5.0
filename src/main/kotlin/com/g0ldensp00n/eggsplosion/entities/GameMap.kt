package com.g0ldensp00n.eggsplosion.entities

import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.Location
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.BaseComponent
import java.awt.TextComponent

enum class MapPlayerMode {
  EDITOR, PLAYER
}

public data class GameMap(val name: String, var world: World?) {
  init {
    require(name.trim().isNotEmpty()) {
      "GameMap name cannot be empty"
    }
  }

  fun playerInGameMap(player: Player): Boolean {
    val worldPlayers = world?.getPlayers()
    worldPlayers?.let {
      for (worldPlayer in worldPlayers) {
        if (player.uniqueId == worldPlayer.uniqueId) {
          return true;
        }
      }
    }
    return false;
  }

  fun loadWorld() {
    world = Bukkit.getServer().createWorld(WorldCreator(name))
    Bukkit.getServer().getWorlds().add(world)
  }

  fun addPlayerToMap(player: Player, mapPlayerMode: MapPlayerMode, playerState: PlayerState?) {
    world ?: loadWorld()
    when(mapPlayerMode) {
      MapPlayerMode.EDITOR -> {
        playerState?.let {
          player.sendMessage("[Map Editor] Continuing where you left off")
          world?.let {
            playerState.applyToPlayer(player, it)
          }?: run {
            player.sendMessage("[Map Editor] Couldn't load world")
          }
        } ?:run {
          val spawnLocation = world?.spawnLocation
          spawnLocation?.let {
            player.teleport(spawnLocation)
          } ?: run {
            player.sendMessage("[Map Editor] Couldn't load world")
          }
        }
      }
      MapPlayerMode.PLAYER -> {
        throw NotImplementedError("addPlayerToMap doesn't support MapPlayerMode.PLAYER")
      }
    }
  }
}
