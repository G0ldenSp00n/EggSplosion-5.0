package com.g0ldensp00n.eggsplosion.handlers.LobbyManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.MainLobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.WaitingLobby;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreManager;
import com.g0ldensp00n.eggsplosion.handlers.ScoreManager.ScoreType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class LobbyManager implements Listener {
  private Plugin plugin;
  private Hashtable<String, Lobby> lobbies;
  private Lobby mainLobby;
  private MapManager mapManager;
  private static LobbyManager lobbyManager;

  public static LobbyManager getInstance(Plugin plugin, MapManager mapManager) {
    if (lobbyManager == null) {
      lobbyManager = new LobbyManager(plugin, mapManager);
    }
    return lobbyManager;
  }

  public LobbyManager(Plugin plugin, MapManager mapManager) {
    this.plugin = plugin;
    this.mapManager = mapManager;
    this.lobbies = new Hashtable<String, Lobby>();

    Bukkit.getPluginManager().registerEvents(this, plugin);
    mainLobby = (Lobby) new MainLobby(plugin);
    if (Bukkit.getOnlinePlayers().size() > 0) {
      mainLobby.addPlayers(Bukkit.getOnlinePlayers());
    }
  }

  public Lobby getPlayersLobby(Player player) {
    Collection<Lobby> lobbiesCollection = lobbies.values();
    for (Lobby lobby : lobbiesCollection) {
      for (Player playerToCheck : lobby.getPlayers()) {
        if (playerToCheck.getUniqueId() == player.getUniqueId()) {
          return lobby;
        }
      }
    }
    return getMainLobby();
  }

  public Lobby getMainLobby() {
    return mainLobby;
  }

  public void replaceLobby(String lobbyName, Lobby lobby) {
    lobbies.put(lobbyName, lobby);
  }

  public Lobby addLobby(Integer maxPlayers, String lobbyName, GameMap map) {
    Lobby addedLobby = new WaitingLobby(this.plugin, maxPlayers, mapManager, lobbyName);
    lobbies.put(lobbyName, addedLobby);
    return addedLobby;
  }

  public void closeLobby(String lobbyName) {
    Lobby lobbyToRemove = lobbies.get(lobbyName);
    if (lobbyToRemove != null) {
      List<Player> playersInLobby = lobbyToRemove.getPlayers();
      this.getMainLobby().addPlayers(playersInLobby);
      lobbies.remove(lobbyName);
    }
  }

  @EventHandler
  public void playerJoin(PlayerJoinEvent playerLoginEvent) {
    Player player = playerLoginEvent.getPlayer();
    if (getPlayersLobby(player) == null) {
      getMainLobby().addPlayer(player);
      getMainLobby().broadcastMessage(player.getDisplayName() + " Joined Lobby");
    }
  }

  public void joinLobby(Lobby lobby, Player player) {
    if (getPlayersLobby(player) instanceof MainLobby) {
      getPlayersLobby(player).removePlayer(player);
    } else {
      List<Lobby> oldLobbies = new ArrayList<Lobby>(lobbies.values());
      oldLobbies.remove(lobby);
      for (Lobby oldLobby : oldLobbies) {
        oldLobby.removePlayer(player);
        if (oldLobby != getMainLobby() && oldLobby.getPlayers().size() == 0) {
          lobbies.remove(oldLobby.getLobbyName());
        }
      }
    }
    lobby.addPlayer(player);
  }

  public void cleanupLobbies() {
    for (Lobby oldLobby : lobbies.values()) {
      if (oldLobby != getMainLobby()) {
        getMainLobby().addPlayers(oldLobby.getPlayers());
        oldLobby.removeAllPlayers();
      }
    }

    lobbies = new Hashtable<>();
  }

  public Boolean canPlayerAttackPlayer(Player victim, Player attacker) {
    Lobby playerLobby = lobbyManager.getPlayersLobby(victim);
    Lobby damagerLobby = lobbyManager.getPlayersLobby(attacker);
    if (playerLobby.getScoreManager() != null) {
      ScoreManager scoreManager = playerLobby.getScoreManager();
      if (scoreManager.getScoreType() == ScoreType.TEAM) {
        return !scoreManager.getPlayerTeam(victim).equals(scoreManager.getPlayerTeam(attacker));
      }
    }
    if (victim.equals(attacker)) {
      return false;
    }
    return playerLobby == damagerLobby;
  }

  @EventHandler
  public void PlayerLogout(PlayerQuitEvent playerQuitEvent) {
    Player player = playerQuitEvent.getPlayer();
    if (getPlayersLobby(player) == getMainLobby()) {
      getMainLobby().removePlayer(player);
    } else {
      Lobby playerLobby = getPlayersLobby(player);
      if (playerLobby != null && playerLobby != getMainLobby() && !playerLobby.anyOnlinePlayersExcluding(player)) {
        playerLobby.removeAllPlayers();
        lobbies.remove(playerLobby.getLobbyName());
      }
    }
  }

  public static LiteralCommandNode<CommandSourceStack> createLobbyCommands() {
    LobbyManager lobbyManager = EggSplosion.getInstance().getLobbyManager();
    LiteralCommandNode<CommandSourceStack> listCommand = Commands.literal("list")
        .requires(ctx -> lobbyManager.lobbies.size() > 0)
        .executes(LobbyManager::executeListLobbies)
        .build();

    LiteralCommandNode<CommandSourceStack> createCommand = Commands.literal("create")
        .then(Commands.argument("lobby_name", StringArgumentType.word())
            .executes(LobbyManager::executeCreateLobby))
        .build();

    LiteralCommandNode<CommandSourceStack> leaveCommand = Commands.literal("leave")
        .requires(ctx -> ctx.getExecutor() instanceof Player player
            && !lobbyManager.getPlayersLobby(player).equals(lobbyManager.getMainLobby()))
        .executes(LobbyManager::executeLeaveLobby)
        .build();

    LiteralCommandNode<CommandSourceStack> joinCommand = Commands.literal("join")
        .requires(ctx -> ctx.getExecutor() instanceof Player && lobbyManager.lobbies.size() > 0)
        .executes(LobbyManager::executeJoinFirstLobby)
        .then(Commands.argument("lobby_name", StringArgumentType.word()).suggests(LobbyManager::getLobbyNameSuggestions)
            .executes(LobbyManager::executeJoinLobby))
        .build();

    LiteralCommandNode<CommandSourceStack> lobbyCommands = Commands.literal("lobby")
        .build();
    lobbyCommands.addChild(listCommand);
    lobbyCommands.addChild(createCommand);
    lobbyCommands.addChild(joinCommand);
    lobbyCommands.addChild(leaveCommand);
    return lobbyCommands;
  }

  private static CompletableFuture<Suggestions> getLobbyNameSuggestions(
      final CommandContext<CommandSourceStack> ctx,
      final SuggestionsBuilder builder) {
    LobbyManager lobbyManager = EggSplosion.getInstance().getLobbyManager();
    lobbyManager.lobbies.keySet()
        .stream()
        .filter(entry -> entry
            .toLowerCase()
            .startsWith(builder
                .getRemainingLowerCase()))
        .forEach(entry -> {
          builder.suggest(entry);
        });
    return builder.buildFuture();
  }

  private static int executeCreateLobby(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    final CommandSender sender = ctx.getSource().getSender();
    final String lobbyName = StringArgumentType.getString(ctx, "lobby_name");
    GameMap waiting_room = EggSplosion.getInstance().getMapManager().getMapByName("WAITING_ROOM");
    LobbyManager lobbyManager = EggSplosion.getInstance().getLobbyManager();
    if (waiting_room != null) {
      Lobby createdLobby = lobbyManager.addLobby(10, lobbyName, waiting_room);
      sender.sendRichMessage(
          "[EggSplosion] Lobby <aqua><lobby_name></aqua> created!",
          Placeholder.component("lobby_name", Component.text(lobbyName)));

      if (sender instanceof Player player) {
        createdLobby.addAdmin(player);
        lobbyManager.joinLobby(createdLobby, player);
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
          onlinePlayer.updateCommands();
        }
      }
    } else {
      sender.sendRichMessage(
          "<red>[EggSplosion]</red> No waiting room found, create a WAITING_ROOM map before starting a lobby");
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int executeJoinFirstLobby(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return Command.SINGLE_SUCCESS;
    }
    LobbyManager lobbyManager = EggSplosion.getInstance().getLobbyManager();

    if (lobbyManager.lobbies.size() > 0) {
      lobbyManager.joinLobby(lobbyManager.lobbies.elements().nextElement(), player);
    } else {
      player.sendRichMessage(
          "<red>[EggSplosion]</red> No Game Lobbies Currently Exist, create one with /lobby create");
    }

    player.updateCommands();
    return Command.SINGLE_SUCCESS;
  }

  private static int executeJoinLobby(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return Command.SINGLE_SUCCESS;
    }

    LobbyManager lobbyManager = EggSplosion.getInstance().getLobbyManager();
    final String lobbyName = StringArgumentType.getString(ctx, "lobby_name");

    Lobby lobby = lobbyManager.lobbies.get(lobbyName);
    if (lobby != null) {
      lobbyManager.joinLobby(lobby, player);
    } else {
      player.sendRichMessage("<red>[EggSplosion]</red> Lobby " + lobbyName + " does not exist");
    }

    player.updateCommands();
    return Command.SINGLE_SUCCESS;
  }

  private static int executeLeaveLobby(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return Command.SINGLE_SUCCESS;
    }

    LobbyManager lobbyManager = EggSplosion.getInstance().getLobbyManager();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    lobbyManager.joinLobby(lobbyManager.getMainLobby(), player);

    player.sendMessage("[EggSplosion] Left Lobby " + ChatColor.AQUA + playerLobby.getLobbyName());
    if (playerLobby.getPlayers().size() == 0) {
      lobbyManager.lobbies.remove(playerLobby.getLobbyName());
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        onlinePlayer.updateCommands();
      }

    }

    player.updateCommands();
    return Command.SINGLE_SUCCESS;
  }

  private static int executeListLobbies(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    final CommandSender sender = ctx.getSource().getSender();
    Hashtable<String, Lobby> lobbies = EggSplosion.getInstance().getLobbyManager().lobbies;
    String lobbiesList = "[EggSplosion] Current Lobbies - ";
    Iterator<String> lobbiesIterator = lobbies.keys().asIterator();
    while (lobbiesIterator.hasNext()) {
      String nextLobby = lobbiesIterator.next();
      lobbiesList += ChatColor.AQUA + nextLobby + ChatColor.RESET;
      if (lobbiesIterator.hasNext()) {
        lobbiesList += ", ";
      }
    }

    sender.sendMessage(lobbiesList);
    return Command.SINGLE_SUCCESS;
  }

}
