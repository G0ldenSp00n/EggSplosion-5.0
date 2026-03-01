package com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.GameModeLobbyTypes;

import java.util.List;
import java.util.Map;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

public class GameLobby_TeamDeathMatch extends GameLobby implements Listener {
  public GameLobby_TeamDeathMatch(Plugin plugin, MapManager mapManager, String lobbyName, GameMap gameMap,
      List<Player> playersInLobby, Map<Player, Team> player_teams) {
    super(plugin, mapManager, lobbyName, GameMode.TEAM_DEATH_MATCH, gameMap, playersInLobby, player_teams);

    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  public void initializeGameLobby() {
    setScoreManager(
        new ScoreManager(getMap().getPointsToWinTDM(), ScoreType.TEAM, this, ChatColor.RED, ChatColor.BLUE, true));
    randomizeTeams();
    getMap().randomizeTeamSides(scoreManager.getTeams());
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
