package com.g0ldensp00n.eggsplosion.repositories

import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.entities.GameMap
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.IOException
import java.io.File

public class TeamConfigRepository {
  private val gameMapToTeamMap: HashMap<String, HashMap<String, TeamConfig>> = HashMap<String, HashMap<String, TeamConfig>>()

  fun addTeamToGameMap(mapName: String, teamConfig: TeamConfig) {
    val gameMapTeamMap = gameMapToTeamMap.get(mapName) ?: HashMap<String, TeamConfig>()
    gameMapTeamMap.set(teamConfig.name, teamConfig)
    gameMapToTeamMap.set(mapName, gameMapTeamMap)
  }

  fun findTeamsByMapNameAndTeamName(mapName: String, teamName: String): TeamConfig? {
    return gameMapToTeamMap.get(mapName)?.get(teamName)
  }

  fun saveToFile() {
    for ((mapName, teamConfigMap) in gameMapToTeamMap) {
      Bukkit.getLogger().info("[EggSplosion:ME] Saving Teams for Map " + mapName)
      for ((teamName, teamConfig: TeamConfig) in teamConfigMap) {
        val teamConfigFile: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), mapName + "/teams/" + teamName + "/config.yaml")
        val teamConfigYaml = YamlConfiguration.loadConfiguration(teamConfigFile)

        teamConfigYaml?.let {
          teamConfigYaml.set("color", teamConfig.colorString)
        }

        try {
          teamConfigYaml.save(teamConfigFile)
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }
    }
  }

  fun loadFromFile(gameMaps: ArrayList<GameMap>) {
    for (gameMap in gameMaps) {
      val teamConfigParentFolder: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), gameMap.name + "/teams")
      teamConfigParentFolder?.listFiles()?.let { teamConfigFolders ->
        Bukkit.getLogger().info("[EggSplosion:ME] Loading Team Configs for Map " + gameMap.name)
        for (teamConfigFolder in teamConfigFolders) {
          val teamName = teamConfigFolder.name
          val teamConfigFile: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), gameMap.name + "/teams/" + teamName + "/config.yaml")
          val teamConfigYaml = YamlConfiguration.loadConfiguration(teamConfigFile)

          teamConfigYaml?.let {
            teamConfigYaml.getString("color")?.let { teamColorString ->
              val teamConfig = TeamConfig(teamName, teamColorString)
              addTeamToGameMap(gameMap.name, teamConfig)
            }
          }
        }
      }
    }
  }
}
