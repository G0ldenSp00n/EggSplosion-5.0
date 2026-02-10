package com.g0ldensp00n.eggsplosion.handlers.Core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.MainLobby;

public class PlayerLeaveHandler implements Listener {
  protected Plugin plugin;
  protected LobbyManager lobbyManager;

  public PlayerLeaveHandler(Plugin plugin, LobbyManager lobbyManager) {
    Bukkit.getPluginManager().registerEvents(this, plugin);
    this.plugin = plugin;
    this.lobbyManager = lobbyManager;
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (!(lobbyManager.getPlayersLobby(player) instanceof MainLobby)) {
      lobbyManager.joinLobby(lobbyManager.getMainLobby(), player);
    }
  }
}
