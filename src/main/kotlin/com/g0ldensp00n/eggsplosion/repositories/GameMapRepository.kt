package com.g0ldensp00n.eggsplosion.repositories

import com.g0ldensp00n.eggsplosion.entities.GameMap
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.file.FileConfiguration
import java.io.File
import java.io.IOException

public class GameMapRepository {
  val gameMaps: ArrayList<GameMap> = ArrayList<GameMap>()

  fun addGameMap(gameMap: GameMap) {
    Bukkit.getWorlds().add(gameMap.world)
    gameMaps.add(gameMap);
  }

  fun findGameMapByName(name: String): GameMap? {
    for (gameMap in gameMaps) {
      if (gameMap.name.toLowerCase() == name.toLowerCase()) {
        return gameMap
      }
    }
    return null
  }

  fun findGameMapByPlayer(player: Player): GameMap? {
    for (gameMap in gameMaps) {
      if (gameMap.playerInGameMap(player)) {
        return gameMap
      }
    }
    return null
  }

  fun saveToFile(dataFolderAbsolutePath: String) {
    val gameMapConfigFile: File = File(dataFolderAbsolutePath, "mapList.yaml")
    val gameMapConfig: FileConfiguration = YamlConfiguration.loadConfiguration(gameMapConfigFile)

    val gameMapNames = ArrayList<String>()
    for (gameMap in gameMaps) {
      gameMapNames.add(gameMap.name);
    }
    gameMapConfig.set("maps", gameMapNames);

    Bukkit.getLogger().info("[EggSplosion:ME] Saving Map List")
    try {
      gameMapConfig.save(gameMapConfigFile);
    } catch (e: IOException) {
      e.printStackTrace();
    } 
  }

  fun loadFromFile(dataFolderAbsolutePath: String) {
    val gameMapConfigFile: File = File(dataFolderAbsolutePath, "mapList.yaml")
    val gameMapConfig: FileConfiguration = YamlConfiguration.loadConfiguration(gameMapConfigFile)

    val gameMapNames: List<String>? = gameMapConfig.getList("maps")?.filterIsInstance<String>() ?: null

    gameMapNames?.let {
      for (mapName in gameMapNames) {
        Bukkit.getLogger().info("[EggSplosion:ME] Thinly Loading Map " + mapName)
        gameMaps.add(GameMap(mapName, null))
      }
    }
  }
}
