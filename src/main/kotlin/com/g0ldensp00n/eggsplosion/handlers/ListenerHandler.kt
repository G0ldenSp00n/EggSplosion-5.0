package com.g0ldensp00n.eggsplosion.handlers

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import com.g0ldensp00n.eggsplosion.Main

abstract class ListenerHandler: Listener {
  public fun register(plugin: Main) {
    var pluginManager: PluginManager = Bukkit.getServer().getPluginManager()
    pluginManager.registerEvents(this, plugin)
  }
}
