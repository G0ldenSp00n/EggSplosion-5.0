package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import com.g0ldensp00n.eggsplosion.repositories.SpawnPointRepository
import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import com.g0ldensp00n.eggsplosion.entities.SpawnPoint

public class RemoveSpawnPointCommand(val spawnPointRepository: SpawnPointRepository) {
  private fun prepare(mapName: String, teamName: String, x: Int, y: Int, z: Int): SpawnPoint? {
    val spawnPoint = spawnPointRepository.findSpawnPointByMapAndTeamNameAndPosition(mapName, teamName, x, y, z)

    return spawnPoint
  }

  private fun persist(mapName: String, teamName: String, spawnPoint: SpawnPoint) {
    spawnPointRepository.removeSpawnPointFromTeam(mapName, teamName, spawnPoint)
  }

  public fun execute(mapName: String, teamName: String, x: Int, y: Int, z: Int) {
    prepare(mapName, teamName, x, y, z)?.let { spawnPoint ->
      persist(mapName, teamName, spawnPoint)
    }
  }
}
