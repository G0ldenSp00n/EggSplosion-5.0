package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import org.bukkit.Bukkit;
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.World.Environment
import org.bukkit.WorldType

public class CreateGameMapCommand(val gameMapRepository: GameMapRepository, val teamConfigRepository: TeamConfigRepository) {
  fun prepare(name: String, seed: Long?, environment: Environment, type: WorldType): World {
    val worldCreator = WorldCreator(name)
    seed?.let {
      worldCreator.seed(seed)
    }
    worldCreator.environment(environment)
    worldCreator.type(type)
    return worldCreator.createWorld() ?: throw NullPointerException("World creator didn't create world")
  }

  companion object {
    @JvmStatic
    fun run(name: String, world: World): GameMap {
      return GameMap(name, world)
    }
  }

  fun persist(gameMap: GameMap, teamConfig: TeamConfig) {
    gameMapRepository.addGameMap(gameMap)
    teamConfigRepository.addTeamToGameMap(gameMap.name, teamConfig)
  }

  fun execute(name: String, seedString: String?, environmentString: String?, typeString: String?) { 
    var seed: Long? = null;
    seedString?.let{
      if (seedString.trim().isNotEmpty()) {
        seed = seedString.toLongOrNull() ?: seedString.hashCode().toLong()
      }
    }
    var environment: Environment;
    when (environmentString?.toLowerCase()) {
      "normal" -> environment = Environment.NORMAL
      "nether" -> environment = Environment.NETHER
      "end" -> environment = Environment.THE_END
      else -> environment = Environment.NORMAL
    }

    var type: WorldType;
    when (typeString?.toLowerCase()) {
      "normal" -> type = WorldType.NORMAL
      "flat" -> type = WorldType.FLAT
      "large_biomes" -> type = WorldType.LARGE_BIOMES
      "amplified" -> type = WorldType.AMPLIFIED
      else -> type = WorldType.NORMAL
    }
    val world: World = prepare(name, seed, environment, type);
    val gameMap = CreateGameMapCommand.run(name, world)
    val teamConfig = CreateMapTeamCommand.run("solo", "white")
    persist(gameMap, teamConfig)
  }
}
