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
}
