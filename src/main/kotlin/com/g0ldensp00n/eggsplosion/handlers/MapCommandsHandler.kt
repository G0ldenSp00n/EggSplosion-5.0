package com.g0ldensp00n.eggsplosion.handlers

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.g0ldensp00n.eggsplosion.commands.CreateGameMapCommand
import com.g0ldensp00n.eggsplosion.commands.EditGameMapCommand
import com.g0ldensp00n.eggsplosion.commands.CreateMapTeamCommand
import com.g0ldensp00n.eggsplosion.commands.CreateMapToolCommand
import com.g0ldensp00n.eggsplosion.commands.ToolFunction

public class MapCommandHandler (val createGameMapCommand: CreateGameMapCommand, val editGameMapCommand: EditGameMapCommand, val createMapTeamCommand: CreateMapTeamCommand, val givePlayerMapToolCommand: CreateMapToolCommand): CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (label == "map") {
      when (args[0]) {
        "create" -> {
          if (args.size > 1) {
            createGameMapCommand.execute(args[1], args.getOrNull(2), args.getOrNull(3), args.getOrNull(4))
            return true
          }
        }
        "edit" -> {
          editGameMapCommand.execute(args[1], sender)
          return true
        }
        "team" -> {
          when (args.getOrNull(1)) {
            "add" -> {
              if (sender is Player) {
                if (args.size > 4) {
                  createMapTeamCommand.execute(sender, args[2], args[3], args[4])
                  return true
                } else if (args.size > 3){
                  createMapTeamCommand.execute(sender, null, args[2], args[3])
                  return true
                }
              }
            }
          }
        }
        "tool" -> {
          when(args.getOrNull(1)) {
            "spawn" -> {
              if (sender is Player) {
                if (args.size > 3) {
                  givePlayerMapToolCommand.execute(sender, args[2], args[3], ToolFunction.SPAWN_TOOL)
                  return true
                } else if (args.size > 2) {
                  givePlayerMapToolCommand.execute(sender, null, args[2], ToolFunction.SPAWN_TOOL)
                  return true
                }
              }
            }
          }
        }
      }
    }

    return false
  }
}
