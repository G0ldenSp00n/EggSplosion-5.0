package com.g0ldensp00n.eggsplosion.handlers

import com.g0ldensp00n.eggsplosion.commands.CreateWeaponCommand
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.Command
import org.bukkit.entity.Player
import org.bukkit.Material

public class WeaponCommandHandler (val createWeaponCommand: CreateWeaponCommand): CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
    if (label == "weapon") {
      when(args[0]) {
        "create" -> {
          if (args.size > 3) {
            if (sender is Player) {
              createWeaponCommand.execute(sender, args[1], Material.WOODEN_HOE, args[2].toFloat(), args[3].toBoolean(), args[4].toFloat())
              return true
            }
          }
        }
      }
    }
    return false
  }
}
