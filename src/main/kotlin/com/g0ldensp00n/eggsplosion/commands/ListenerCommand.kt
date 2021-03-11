package com.g0ldensp00n.eggsplosion.commands

import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.Bukkit
import com.g0ldensp00n.eggsplosion.Main

abstract class ListenerCommand: Listener {
  var plugin: Main? = null
  public fun register(plugin: Main) {
    this.plugin = plugin
    var pluginManager: PluginManager = Bukkit.getServer().getPluginManager()
    pluginManager.registerEvents(this, plugin)
  }
}
