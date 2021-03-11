package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import com.g0ldensp00n.eggsplosion.repositories.SpawnPointRepository

public class RemoveSpawnPointCommand(val gameMapRepository: GameMapRepository, val teamConfigRepository: TeamConfigRepository, val spawnPointRepository: SpawnPointRepository)
