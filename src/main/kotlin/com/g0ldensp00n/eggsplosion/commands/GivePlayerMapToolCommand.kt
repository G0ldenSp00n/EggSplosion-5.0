package com.g0ldensp00n.eggsplosion.commands

import com.g0ldensp00n.eggsplosion.repositories.GameMapRepository
import com.g0ldensp00n.eggsplosion.repositories.TeamConfigRepository
import com.g0ldensp00n.eggsplosion.entities.GameMap
import com.g0ldensp00n.eggsplosion.entities.TeamConfig
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.Material
import org.bukkit.entity.Player

private data class GivePlayerMapToolCommandPrepareResponse (val gameMap: GameMap, val team: TeamConfig?)

public enum class ToolFunction {
  SPAWN_TOOL
}

public class GivePlayerMapToolCommand (val gameMapRepository: GameMapRepository, val teamConfigRepository: TeamConfigRepository){
  private fun prepare(player: Player, mapName: String?, teamName: String): GivePlayerMapToolCommandPrepareResponse {
    val gameMap = gameMapRepository.findGameMapByName(mapName ?: "") ?: gameMapRepository.findGameMapByPlayer(player) ?: run {
      throw IllegalArgumentException("Supplied map does not exist")
    }
    val teamConfig = teamConfigRepository.findTeamsByMapNameAndTeamName(gameMap.name, teamName)
    return GivePlayerMapToolCommandPrepareResponse(gameMap, teamConfig)
  }

  companion object {
    @JvmStatic
    public fun run(gameMap: GameMap, teamConfig: TeamConfig?, toolFunction: ToolFunction): ItemStack? {
      when (toolFunction) {
        ToolFunction.SPAWN_TOOL -> {
          teamConfig?.let { teamConfig ->
            var itemStack = ItemStack(Material.LEATHER_BOOTS)
            var itemMeta = itemStack.itemMeta
            itemMeta?.let { itemMeta ->
              if (itemMeta is LeatherArmorMeta) {
                itemMeta.setColor(teamConfig.bukkitColor())
              }
              itemMeta.setDisplayName(teamConfig.name.capitalize() + " Team Spawn Tool")
              val loreToAdd = listOf("function:spawnTool", "map:"+gameMap.name, "team:"+teamConfig.name)
              itemMeta.lore = loreToAdd
              itemStack.itemMeta = itemMeta
            }
            return itemStack
          } ?: {
            throw IllegalArgumentException("Team must be defined when creating a spawn tool")
          }
        }
      }
      return null
    }
  }

  private fun persist(player: Player, itemStack: ItemStack) {
    player.inventory.addItem(itemStack)
  }

  fun execute(player: Player, mapName: String?, teamName: String, toolFunction: ToolFunction) {
    try {
      val prepareResponse = prepare(player, mapName, teamName)
      prepareResponse.team?.let { teamConfig ->
        val mapTool = GivePlayerMapToolCommand.run(prepareResponse.gameMap, teamConfig, toolFunction)
        mapTool?.let {
          persist(player, mapTool)
        }
      }
    } catch (illegalArgumentException: IllegalArgumentException) {
      player.sendMessage("[Map Editor]" + illegalArgumentException.message)
    }
  }
}
