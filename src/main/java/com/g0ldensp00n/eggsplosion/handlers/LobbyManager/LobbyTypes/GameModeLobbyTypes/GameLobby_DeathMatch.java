package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes;

import java.util.List;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class GameLobby_DeathMatch extends GameLobby implements Listener {
  public GameLobby_DeathMatch(Plugin plugin, MapManager mapManager, String lobbyName, GameMap gameMap,
      List<Player> playersInLobby) {
    super(plugin, mapManager, lobbyName, GameMode.DEATH_MATCH, gameMap, playersInLobby, null);

    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public void initializeGameLobby() {
    setScoreManager(new ScoreManager(getMap().getPointsToWinDM(), ScoreType.SOLO, this));
  }

  @EventHandler
  public void playerDeathEvent(PlayerDeathEvent playerDeathEvent) {
    Player victim = playerDeathEvent.getEntity();
    Player killer = playerDeathEvent.getEntity().getKiller();

    if (killer != null && victim != null && this.playerInLobby(killer) && this.playerInLobby(victim)) {
      this.getScoreManager().addScorePlayer(killer);
    }
  }
}
