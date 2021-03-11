package com.g0ldensp00n.eggsplosion.repositories

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.GameMode
import org.bukkit.inventory.ItemStack
import com.g0ldensp00n.eggsplosion.entities.MapPlayerMode
import com.g0ldensp00n.eggsplosion.entities.PlayerState
import com.g0ldensp00n.eggsplosion.entities.Position
import com.g0ldensp00n.eggsplosion.entities.GameMap
import java.io.File
import java.io.IOException

public class PlayerStateRepository {
  private val gameMapToEditorStateMap: HashMap<String, HashMap<String, PlayerState>> = HashMap<String, HashMap<String, PlayerState>>()

  fun findPlayerStateByGameMapAndUUID(mapName: String, UUID: String): PlayerState? { 
    return gameMapToEditorStateMap.get(mapName)?.get(UUID)
  }

  fun addPlayerStateByGameMapAndUUID(mapName: String, UUID: String, playerState: PlayerState) {
    gameMapToEditorStateMap.get(mapName)?.put(UUID, playerState) ?: run {
      val editorStateByUUID = HashMap<String, PlayerState>()
      editorStateByUUID.put(UUID, playerState)
      gameMapToEditorStateMap.put(mapName, editorStateByUUID)
    }
  }

  fun saveEditorsToFile() {
    for ((mapName, editorStateByUUIDs) in gameMapToEditorStateMap) {
      Bukkit.getLogger().info("[EggSplosion:ME] Saving Editors PlayerState for Map " + mapName)
      for ((editorUUID, editorState) in editorStateByUUIDs) {
        val editorConfigFile: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), mapName + "/player-state/editors/" + editorUUID + ".yaml")
        val editorConfig = YamlConfiguration.loadConfiguration(editorConfigFile)

        editorState?.let {
          editorConfig.set("inventoryContents", editorState.inventoryContents)
          editorConfig.set("armorContents", editorState.armorContents)
          editorConfig.set("gameMode", editorState.gameMode.toString())
          editorConfig.set("flying", editorState.flying)
          editorState.position?.let {
            editorConfig.set("position.x", it.x)
            editorConfig.set("position.y", it.y)
            editorConfig.set("position.z", it.z)
            editorConfig.set("position.pitch", it.pitch)
            editorConfig.set("position.yaw", it.yaw)
          }

          try {
            editorConfig.save(editorConfigFile)
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
      }
    }
  }

  fun loadEditorsFromFile(gameMaps: ArrayList<GameMap>) {
    for (gameMap in gameMaps) {
      val editorsConfigFolder: File = File(Bukkit.getServer().getWorldContainer().getAbsolutePath(), gameMap.name + "/player-state/editors")
      editorsConfigFolder?.listFiles()?.let {
        Bukkit.getLogger().info("[EggSplosion:ME] Loading Editors PlayerState for Map " + gameMap.name)
        for (editorConfigFile in editorsConfigFolder.listFiles()) {
          val editorUUID = editorConfigFile.name.split(".")[0]
          val editorConfig = YamlConfiguration.loadConfiguration(editorConfigFile)

          editorConfig?.let {
            val editorPlayerState = readEditorConfigFile(editorConfig)
            addPlayerStateByGameMapAndUUID(gameMap.name, editorUUID, editorPlayerState)
          }
        }
      }
    }
  }

  private fun readEditorConfigFile(editorConfig: FileConfiguration): PlayerState {
    var editorGameMode: GameMode;
    when(editorConfig.getString("gameMode")?.toLowerCase()) {
      "creative" -> editorGameMode = GameMode.CREATIVE
      "spectator" -> editorGameMode = GameMode.SPECTATOR
      "adventure" -> editorGameMode = GameMode.ADVENTURE
      else -> editorGameMode = GameMode.SURVIVAL
    }

    val x: Double? = editorConfig.getDouble("position.x")
    val y: Double? = editorConfig.getDouble("position.y")
    val z: Double? = editorConfig.getDouble("position.z")
    val pitch: Float? = editorConfig.getDouble("position.pitch")?.toFloat()
    val yaw: Float? = editorConfig.getDouble("position.yaw")?.toFloat()

    var position: Position? = null
    x?.let {
      y?.let {
        z?.let {
          pitch?.let {
            yaw?.let {
              position = Position(x, y, z, pitch, yaw)
            }
          }
        }
      }
    }

    var editorIsFlying = editorConfig.getBoolean("flying")

    var editorInventoryContents: List<ItemStack?> = editorConfig.getList("inventoryContents")?.filterIsInstance<ItemStack?>() ?: List<ItemStack?>(40) { null }
    var editorArmorContents: List<ItemStack?> = editorConfig.getList("armorContents")?.filterIsInstance<ItemStack?>() ?: List<ItemStack?>(4) { null }

    return PlayerState(
      MapPlayerMode.EDITOR,
      editorInventoryContents.toTypedArray(),
      editorArmorContents.toTypedArray(),
      editorGameMode,
      position,
      editorIsFlying
    )
  }
}
