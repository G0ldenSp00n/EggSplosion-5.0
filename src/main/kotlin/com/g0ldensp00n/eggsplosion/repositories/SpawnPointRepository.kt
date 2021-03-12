package com.g0ldensp00n.eggsplosion.repositories

import com.g0ldensp00n.eggsplosion.entities.SpawnPoint
import com.g0ldensp00n.eggsplosion.entities.Position
import com.g0ldensp00n.eggsplosion.entities.GameMap
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.IOException
import java.io.File

public class SpawnPointRepository {
  private val gameMapToTeamSpawnPointMap: HashMap<String, HashMap<String, ArrayList<SpawnPoint>>> = HashMap<String, HashMap<String, ArrayList<SpawnPoint>>>()

  fun addSpawnPointToTeam(mapName: String, teamName: String, spawnPoint: SpawnPoint) {
    val spawnPointExists = findSpawnPointByMapAndTeamNameAndPosition(mapName, teamName, spawnPoint.position.x.toInt(), spawnPoint.position.y.toInt(), spawnPoint.position.z.toInt()) ?: run {
      val teamSpawnPointsMap = gameMapToTeamSpawnPointMap.get(mapName) ?: HashMap<String, ArrayList<SpawnPoint>>()
      val teamSpawnPoints = teamSpawnPointsMap.get(teamName) ?: ArrayList<SpawnPoint>()
      teamSpawnPoints.add(spawnPoint)
      teamSpawnPointsMap.set(teamName, teamSpawnPoints)
      gameMapToTeamSpawnPointMap.set(mapName, teamSpawnPointsMap)
    }
  }

  fun findSpawnPointByMapAndTeamNameAndPosition(mapName: String, teamName: String, x: Int, y: Int, z: Int): SpawnPoint? {
    val spawnPoints: ArrayList<SpawnPoint>? = findSpawnPointsByMapAndTeamName(mapName, teamName)
    var matchingSpawnPoint: SpawnPoint? = null
    spawnPoints?.forEach { spawnPoint ->
      if (spawnPoint.position.x.toInt() == x && spawnPoint.position.y.toInt() == y && spawnPoint.position.z.toInt() == z) {
        matchingSpawnPoint = spawnPoint
      }
    }
    return matchingSpawnPoint
  }

  fun findSpawnPointsByMapAndTeamName(mapName: String, teamName: String): ArrayList<SpawnPoint>? {
    return gameMapToTeamSpawnPointMap.get(mapName)?.get(teamName)
  }

  fun removeSpawnPointFromTeam(mapName: String, teamName: String, spawnPoint: SpawnPoint) {
    val spawnPoints: ArrayList<SpawnPoint>? = findSpawnPointsByMapAndTeamName(mapName, teamName)
    spawnPoints?.removeAll {
      it == spawnPoint
    }
  }

  fun saveToFile() {
    for ((mapName, teamSpawnMap) in gameMapToTeamSpawnPointMap) {
      Bukkit.getLogger().info("[EggSplosion:ME] Saving Spawns for Map " + mapName)
      for((teamName, spawnList) in teamSpawnMap) {
        val teamSpawnFile: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), mapName + "/teams/" + teamName + "/spawns.yaml")
        val teamSpawnConfig = YamlConfiguration.loadConfiguration(teamSpawnFile)

        val positionList = ArrayList<Position>()
        spawnList.forEach { spawn ->
          positionList.add(spawn.position)
          spawn.hide()
        }

        teamSpawnConfig?.let {
          teamSpawnConfig.set("spawns", positionList)
        }

        try {
          teamSpawnConfig.save(teamSpawnFile)
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }
    }
  }

  fun loadFromFile(gameMaps: ArrayList<GameMap>) {
    for (gameMap in gameMaps) {
      val teamParentFolder: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), gameMap.name + "/teams")
      teamParentFolder?.listFiles()?.let { teamConfigFolders ->
        Bukkit.getLogger().info("[EggSplosion:ME] Loading Spawn for Map " + gameMap.name)
        for (teamConfigFolder in teamConfigFolders) {
          val teamName = teamConfigFolder.name
          val spawnConfigFile: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), gameMap.name + "/teams/" + teamName + "/spawns.yaml")
          val spawnConfig: YamlConfiguration = YamlConfiguration.loadConfiguration(spawnConfigFile)

          spawnConfig?.let {
            val positions: List<*>? = spawnConfig.getList("spawns")
            positions?.let {
              val spawns = ArrayList<SpawnPoint>()
              positions.forEach { position ->
                if (position is Position) {
                  spawns.add(SpawnPoint(position))
                }
              }

              val teamSpawnMap = gameMapToTeamSpawnPointMap.get(gameMap.name) ?: HashMap<String, ArrayList<SpawnPoint>>()
              teamSpawnMap.set(teamName, spawns)
              gameMapToTeamSpawnPointMap.set(gameMap.name, teamSpawnMap)
            }
          }
        }
      }
    }
  }
}
