package com.g0ldensp00n.eggsplosion.entities

import org.bukkit.scoreboard.Team
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color
import kotlin.properties.Delegates

public data class TeamConfig (val name: String, val colorString: String) {
  val team: Team? = null
  var color: ChatColor by Delegates.notNull()

  init {
    enumValues<ChatColor>().forEach { chatColor ->
      if (chatColor.name.toLowerCase() == colorString.toLowerCase()) {
        this.color = chatColor
      }
    }
    requireNotNull(color) {
      "Team Requires a Valid Minecraft Color"
    }
  }

  fun bukkitColor (): Color {
    when (color) {
      ChatColor.BLACK -> return Color.BLACK
      ChatColor.DARK_BLUE -> return Color.NAVY
      ChatColor.DARK_GREEN -> return Color.GREEN
      ChatColor.DARK_AQUA -> return Color.TEAL
      ChatColor.DARK_RED -> return Color.MAROON
      ChatColor.DARK_PURPLE -> return Color.FUCHSIA
      ChatColor.GRAY -> return Color.GRAY
      ChatColor.DARK_GRAY -> return Color.GRAY
      ChatColor.BLUE -> return Color.BLUE
      ChatColor.GREEN -> return Color.GREEN
      ChatColor.AQUA -> return Color.AQUA
      ChatColor.RED -> return Color.RED
      ChatColor.LIGHT_PURPLE -> return Color.PURPLE
      ChatColor.YELLOW -> return Color.YELLOW
      ChatColor.WHITE -> return Color.WHITE
    }
    return Color.WHITE
  }

  fun createTeam(scoreboard: Scoreboard) {
    val team: Team = scoreboard.registerNewTeam(name);
    team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM)
    team.color = color
  }
}

