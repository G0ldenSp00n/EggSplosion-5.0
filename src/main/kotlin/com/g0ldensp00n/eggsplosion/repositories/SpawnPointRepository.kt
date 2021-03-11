package com.g0ldensp00n.eggsplosion.repositories

import com.g0ldensp00n.eggsplosion.entities.SpawnPoint

public class SpawnPointRepository {
  private val gameMapToTeamSpawnPointMap: HashMap<String, HashMap<String, ArrayList<SpawnPoint>>> = HashMap<String, HashMap<String, ArrayList<SpawnPoint>>>()

  fun addSpawnPointToTeam(mapName: String, teamName: String, spawnPoint: SpawnPoint) {
    val teamSpawnPointsMap = gameMapToTeamSpawnPointMap.get(mapName) ?: HashMap<String, ArrayList<SpawnPoint>>()
    val teamSpawnPoints = teamSpawnPointsMap.get(teamName) ?: ArrayList<SpawnPoint>()
    teamSpawnPoints.add(spawnPoint)
    teamSpawnPointsMap.set(teamName, teamSpawnPoints)
    gameMapToTeamSpawnPointMap.set(mapName, teamSpawnPointsMap)
  }

  fun findSpawnPointsByMapAndTeamName(mapName: String, teamName: String): ArrayList<SpawnPoint>? {
    return gameMapToTeamSpawnPointMap.get(mapName)?.get(teamName)
  }
}
