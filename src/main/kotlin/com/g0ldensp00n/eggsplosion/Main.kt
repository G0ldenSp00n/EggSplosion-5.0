package com.g0ldensp00n.eggsplosion

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command

import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.commands.CreateGameMapCommand
import com.g0ldensp00n.eggsplosion.commands.EditGameMapCommand
import com.g0ldensp00n.eggsplosion.commands.SavePlayerStateCommand
import com.g0ldensp00n.eggsplosion.commands.LoadPlayerStateCommand
import com.g0ldensp00n.eggsplosion.commands.CreateSpawnPointCommand
import com.g0ldensp00n.eggsplosion.commands.GivePlayerMapToolCommand
import com.g0ldensp00n.eggsplosion.commands.CreateMapTeamCommand
import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.PlayerStateRepository
import com.g0ldensp00n.eggsplosion.repositories.SpawnPointRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import com.g0ldensp00n.eggsplosion.handlers.MapCommandHandler
import com.g0ldensp00n.eggsplosion.handlers.PlayerJoinHandler
import com.g0ldensp00n.eggsplosion.handlers.PlayerWorldSwitchHandler
import com.g0ldensp00n.eggsplosion.handlers.PlayerUseMapToolHandler
import com.g0ldensp00n.eggsplosion.handlers.PlayerEditSpawnHandler

class Main: JavaPlugin() {
  private val pluginConfigFolderAbsolutePath: String = getDataFolder().getAbsolutePath()
  private val gameMapRepository: GameMapRepository = GameMapRepository()
  private val playerStateRepository: PlayerStateRepository = PlayerStateRepository()
  private val spawnPointRepository: SpawnPointRepository = SpawnPointRepository()
  private val teamConfigRepository: TeamConfigRepository = TeamConfigRepository()

  private val createGameMapCommand: CreateGameMapCommand = CreateGameMapCommand(gameMapRepository, teamConfigRepository)
  private val editGameMapCommand: EditGameMapCommand = EditGameMapCommand(gameMapRepository, playerStateRepository)
  private val savePlayerStateCommand: SavePlayerStateCommand = SavePlayerStateCommand(playerStateRepository, gameMapRepository)
  private val loadPlayerStateCommand: LoadPlayerStateCommand = LoadPlayerStateCommand(playerStateRepository)
  private val createSpawnPointCommand: CreateSpawnPointCommand = CreateSpawnPointCommand(gameMapRepository, spawnPointRepository, teamConfigRepository)
  private val createMapTeamCommand: CreateMapTeamCommand = CreateMapTeamCommand(gameMapRepository, teamConfigRepository)
  private val givePlayerMapToolCommand: GivePlayerMapToolCommand = GivePlayerMapToolCommand(gameMapRepository, teamConfigRepository)

  private val mapCommandHandler = MapCommandHandler(createGameMapCommand, editGameMapCommand, createMapTeamCommand, givePlayerMapToolCommand)
  private val playerJoinHandler = PlayerJoinHandler(editGameMapCommand)
  private val playerWorldSwitchHandler = PlayerWorldSwitchHandler(savePlayerStateCommand, loadPlayerStateCommand)
  private val playerUseMapToolHandler = PlayerUseMapToolHandler(createSpawnPointCommand)
  private val playerEditSpawnHandler = PlayerEditSpawnHandler()

  override fun onEnable() {
    getLogger().info("Enabled EggSplosion v5.0")

    gameMapRepository.loadFromFile(pluginConfigFolderAbsolutePath)
    playerStateRepository.loadEditorsFromFile(gameMapRepository.gameMaps)
    teamConfigRepository.loadFromFile(gameMapRepository.gameMaps)

    createSpawnPointCommand.register(this)

    playerJoinHandler.register(this)
    playerWorldSwitchHandler.register(this)
    playerUseMapToolHandler.register(this)
    playerEditSpawnHandler.register(this)

    getCommand("map")?.setExecutor(mapCommandHandler)
  }

  override fun onDisable() {
    Bukkit.getOnlinePlayers().forEach { player -> {
      getLogger().info("Saving Player " + player.name + " State")
      savePlayerStateCommand.execute(player)
    }}
    playerStateRepository.saveEditorsToFile()
    teamConfigRepository.saveToFile()
    gameMapRepository.saveToFile(pluginConfigFolderAbsolutePath)
    getLogger().info("Disabled EggSplosion v5.0")
  }

}
