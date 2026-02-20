package com.g0ldensp00n.eggsplosion.handlers.Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

public class DeathMessages implements Listener {
  private LobbyManager lobbyManager;
  private List<String> deathMessagesPlayerOnPlayer;
  private List<String> deathMessagesPlayerOnPlayerFall;

  public DeathMessages(Plugin plugin, LobbyManager lobbyManager) {
    this.lobbyManager = lobbyManager;
    Bukkit.getPluginManager().registerEvents(this, plugin);

    deathMessagesPlayerOnPlayer = new ArrayList<>();
    deathMessagesPlayerOnPlayer.add("was scrambled by");
    deathMessagesPlayerOnPlayer.add("was poached by");
    deathMessagesPlayerOnPlayer.add("was shelled by");

    deathMessagesPlayerOnPlayerFall = new ArrayList<>();
    deathMessagesPlayerOnPlayerFall.add("was Humpty Dumptied by");
  }

  @EventHandler
  public void playerTakeDamage(EntityDamageByEntityEvent entityDamageEvent) {
    DamageCause cause = entityDamageEvent.getCause();
    if (entityDamageEvent.getEntity() instanceof Player
        && entityDamageEvent.getDamageSource().getCausingEntity() instanceof Player) {
      Player player = (Player) entityDamageEvent.getEntity();
      Player damager = (Player) entityDamageEvent.getDamageSource().getCausingEntity();

      if (!lobbyManager.canPlayerAttackPlayer(player, damager)) {
        entityDamageEvent.setCancelled(true);
        return;
      }

      if (damager != null && player != null) {
        if (entityDamageEvent.getCause() == DamageCause.ENTITY_EXPLOSION) {
          if ((player.getHealth() - entityDamageEvent.getFinalDamage()) <= 0) {
            damager.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
          } else {
            damager.playSound(damager.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
          }
        }
      }
    }
  }

  @EventHandler
  public void playerDeathEvent(PlayerDeathEvent playerDeathEvent) {

    Player victim = playerDeathEvent.getEntity();
    Player killer = playerDeathEvent.getEntity().getKiller();

    Lobby playerLobby = lobbyManager.getPlayersLobby(victim);
    if (killer != null && victim != null) {
      DamageType damageType = playerDeathEvent.getDamageSource().getDamageType();
      if (damageType == DamageType.PLAYER_EXPLOSION) {
        killer.playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

        Random random = new Random();
        String deathMessage = deathMessagesPlayerOnPlayer.get(random.nextInt(deathMessagesPlayerOnPlayer.size()));

        playerLobby.broadcastCustomDeathMessage(deathMessage, victim, killer);
      } else if (damageType == DamageType.FALL) {
        killer.playSound(victim.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

        Random random = new Random();
        String deathMessage = deathMessagesPlayerOnPlayerFall
            .get(random.nextInt(deathMessagesPlayerOnPlayerFall.size()));

        playerLobby.broadcastCustomDeathMessage(deathMessage, victim, killer);
      } else {
        playerLobby.broadcastDefaultDeathMessage(playerDeathEvent.deathMessage(), victim, killer);
      }
    } else {
      playerLobby.broadcastDefaultDeathMessage(playerDeathEvent.deathMessage(), victim, killer);
    }
  }
}
