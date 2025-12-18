package com.g0ldensp00n.eggsplosion.handlers.LobbyManager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.MainLobby;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.WaitingLobby;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.GameMap;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class LobbyMenuSystem implements Listener {

  private Map<Player, Inventory> Screen_Personal_lobbyMain;
  private Inventory Screen_adminMainMenu;
  private Inventory Screen_adminGameModeSelect;
  private Inventory Screen_adminMapSelect;
  private ArrayList<Inventory> Screen_adminReadyPlayer;
  private ArrayList<Inventory> Screen_adminSetTeams;
  private Inventory Screen_gameModeSelect;
  private Inventory Screen_mapSelect;
  private ItemStack UI_Button_gameModeSelect;
  private ItemStack UI_Button_mapSelect;
  private ItemStack UI_Button_ctfGameMode;
  private ItemStack UI_Button_tdmGameMode;
  private ItemStack UI_Button_cpGameMode;
  private ItemStack UI_Button_dmGameMode;
  private LobbyManager lobbyManager;
  private MapManager mapManager;

  public LobbyMenuSystem(Plugin plugin, LobbyManager lobbyManager, MapManager mapManager) {
    this.mapManager = mapManager;
    this.lobbyManager = lobbyManager;
    this.Screen_Personal_lobbyMain = new Hashtable<>();
    Bukkit.getPluginManager().registerEvents(this, plugin);

    // Game Mode Select Screen
    Screen_adminGameModeSelect = Bukkit.getServer().createInventory(null, InventoryType.CHEST,
        "Admin Game Mode Select Menu");
    Screen_gameModeSelect = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Game Mode Select Menu");

    UI_Button_ctfGameMode = createMenuButton(Material.BLUE_BANNER, "Capture the Flag");
    UI_Button_tdmGameMode = createMenuButton(Material.LEATHER_CHESTPLATE, "Team Death Match");
    UI_Button_dmGameMode = createMenuButton(Material.LEATHER_HELMET, "Death Match");
    UI_Button_cpGameMode = createMenuButton(Material.BEACON, "Capture Point");

    // Configure TDM Button
    LeatherArmorMeta tdmMeta = (LeatherArmorMeta) UI_Button_tdmGameMode.getItemMeta();
    tdmMeta.setColor(Color.fromRGB(3949738));
    UI_Button_tdmGameMode.setItemMeta(tdmMeta);

    Screen_gameModeSelect.setItem(12, UI_Button_ctfGameMode);
    Screen_gameModeSelect.setItem(13, UI_Button_cpGameMode);
    Screen_gameModeSelect.setItem(14, UI_Button_tdmGameMode);
    Screen_gameModeSelect.setItem(22, UI_Button_dmGameMode);

    Screen_adminGameModeSelect.setItem(12, UI_Button_ctfGameMode);
    Screen_adminGameModeSelect.setItem(13, UI_Button_cpGameMode);
    Screen_adminGameModeSelect.setItem(14, UI_Button_tdmGameMode);
    Screen_adminGameModeSelect.setItem(22, UI_Button_dmGameMode);

    // Admin Screen
    Screen_adminMainMenu = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Admin Menu");
    Screen_adminReadyPlayer = new ArrayList<>();
    Screen_adminReadyPlayer
        .add(Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Admin Ready Player - Page 1"));
    setupAdminMenu(null);
    Screen_adminSetTeams = new ArrayList<>();
    Screen_adminSetTeams
        .add(Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Admin Set Teams - Page 1"));
    setupAdminMenu(null);

    // Map Select Screen
    Screen_mapSelect = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Map Select Menu");
    Screen_adminMapSelect = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Admin Map Select Menu");
  }

  private ItemStack createMenuButton(Material material, String displayName, List<String> lore) {
    List<String> formattedLore = new ArrayList<String>();
    for (String loreLine : lore) {
      formattedLore.add(ChatColor.RESET + loreLine);
    }
    ItemStack UI_Button = new ItemStack(material);
    ItemMeta UI_Button_Meta = UI_Button.getItemMeta();
    UI_Button_Meta.setDisplayName(ChatColor.RESET + displayName);
    UI_Button_Meta.setLore(formattedLore);
    UI_Button_Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    UI_Button.setItemMeta(UI_Button_Meta);
    return UI_Button;
  }

  private ItemStack createMenuButton(Material material, String displayName) {
    return createMenuButton(material, displayName, new ArrayList<String>());
  }

  private boolean isMenuInventory(Inventory inventory) {
    return Screen_Personal_lobbyMain.containsValue(inventory) || Screen_adminReadyPlayer.contains(inventory)
        || Screen_adminSetTeams.contains(inventory)
        || inventory == Screen_gameModeSelect
        || inventory == Screen_mapSelect || inventory == Screen_adminMainMenu || inventory == Screen_adminGameModeSelect
        || inventory == Screen_adminMapSelect;
  }

  private void setupPersonalMenu(Inventory lobbyToSetup, Player player) {
    lobbyToSetup.clear();
    UI_Button_gameModeSelect = createMenuButton(Material.FIREWORK_ROCKET, "Game Mode Select");
    UI_Button_mapSelect = createMenuButton(Material.MAP, "Map Select");
    ItemStack UI_Button_ReadyUnready;
    Lobby lobby = lobbyManager.getPlayersLobby(player);
    if (lobby instanceof WaitingLobby) {
      WaitingLobby waitingLobby = (WaitingLobby) lobby;
      if (waitingLobby.isPlayerReady(player)) {
        UI_Button_ReadyUnready = createMenuButton(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Unready");
      } else {
        UI_Button_ReadyUnready = createMenuButton(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Ready");
      }

      ItemStack player_indicator = createMenuButton(Material.PLAYER_HEAD, "G0ldenSp00n");
      SkullMeta player_skull_meta = (SkullMeta) player_indicator.getItemMeta();
      player_skull_meta.setOwningPlayer(player);
      player_indicator.setItemMeta(player_skull_meta);

      lobbyToSetup.setItem(12, UI_Button_gameModeSelect);
      lobbyToSetup.setItem(13, player_indicator);
      lobbyToSetup.setItem(14, UI_Button_mapSelect);
      lobbyToSetup.setItem(22, UI_Button_ReadyUnready);
    }
  }

  ItemStack UI_Button_adminSetPlayerTeams;
  ItemStack UI_Button_adminSetMap;
  ItemStack UI_Button_adminSetGameMode;
  ItemStack UI_Button_adminReadyPlayers;

  private void setupAdminMenu(Player player) {
    UI_Button_adminSetGameMode = createMenuButton(Material.FIREWORK_ROCKET, "Set Game Mode");
    UI_Button_adminSetMap = createMenuButton(Material.MAP, "Set Map");
    UI_Button_adminSetPlayerTeams = createMenuButton(Material.LEATHER_HELMET, "Set Teams");
    UI_Button_adminReadyPlayers = createMenuButton(Material.PLAYER_HEAD, "Set Players Ready State");

    Screen_adminMainMenu.clear();

    Screen_adminMainMenu.setItem(12, UI_Button_adminSetGameMode);
    Screen_adminMainMenu.setItem(13, UI_Button_adminSetPlayerTeams);
    Screen_adminMainMenu.setItem(14, UI_Button_adminSetMap);
    Screen_adminMainMenu.setItem(22, UI_Button_adminReadyPlayers);
  }

  @EventHandler
  public void menuOpen(PlayerInteractEvent playerInteractEvent) {
    if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)
        || playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_AIR)) {
      if (playerInteractEvent.getItem() != null) {
        if (playerInteractEvent.getItem().getType().equals(Material.EMERALD)) {
          Player player = playerInteractEvent.getPlayer();
          Lobby lobby = lobbyManager.getPlayersLobby(player);

          if (!(lobby instanceof MainLobby)) {
            Inventory Screen_lobbyMain;

            if (lobby.isAdmin(player) && !player.isSneaking()) {
              Screen_lobbyMain = Screen_adminMainMenu;
              setupAdminMenu(player);
            }
            // Main Screen
            else if (Screen_Personal_lobbyMain.containsKey(player)) {
              Screen_lobbyMain = Screen_Personal_lobbyMain.get(player);
            } else {
              Screen_lobbyMain = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Lobby Main Menu");
              Screen_Personal_lobbyMain.put(player, Screen_lobbyMain);
            }

            if (lobby instanceof WaitingLobby) {
              if (!Screen_lobbyMain.equals(Screen_adminMainMenu)) {
                setupPersonalMenu(Screen_lobbyMain, player);
              }
              playerInteractEvent.getPlayer().openInventory(Screen_lobbyMain);
            } else {
              player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                  new TextComponent("Lobby Menu can only be opened in the waiting room"));
            }
          } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                new TextComponent("Lobby Menu can't be opened in the Main Lobby"));
          }
        }
      }
    }
  }

  private void loadMapsScreen(Inventory map_screen) {
    map_screen.clear();

    Map<String, GameMap> maps = mapManager.getMaps();
    for (String mapName : maps.keySet()) {
      if (!mapName.equalsIgnoreCase("WAITING_ROOM")) {
        ItemStack mapSelectButton;
        if (maps.get(mapName).getMapIcon() != null) {
          mapSelectButton = createMenuButton(maps.get(mapName).getMapIcon(), mapName);
        } else {
          mapSelectButton = createMenuButton(Material.MAP, mapName);
        }
        map_screen.addItem(mapSelectButton);
      }
    }
  }

  private interface PlayerValueTester {
    public boolean op(Player player);
  };

  private void create_player_selector_pages(ArrayList<Inventory> page_array, Lobby lobby, ItemStack item_a,
      ItemStack item_b, PlayerValueTester player_test, String page_title) {
    List<Player> players = lobby.getPlayers();
    int index = 0;
    int page = 0;
    for (Player player : players) {
      ItemStack player_head_display = createMenuButton(Material.PLAYER_HEAD, player.getDisplayName());
      ItemStack UI_Button_ReadyUnreadyPlayer;
      if (player_test.op(player)) {
        UI_Button_ReadyUnreadyPlayer = item_a;
      } else {
        UI_Button_ReadyUnreadyPlayer = item_b;
      }

      SkullMeta player_head_meta = (SkullMeta) player_head_display.getItemMeta();
      player_head_meta.setOwningPlayer(player);
      player_head_display.setItemMeta(player_head_meta);

      page_array.get(page).setItem(index % 9, player_head_display);
      page_array.get(page).setItem(index % 9 + 9, UI_Button_ReadyUnreadyPlayer);

      index += 1;
      if (index % 9 == 0 && index != 0) {
        next_page = createMenuButton(Material.ARROW, "Next Page");
        page_array.get(page).setItem(26, next_page);
        page += 1;
        if (page_array.size() - 1 < page) {
          page_array
              .add(
                  Bukkit.getServer().createInventory(null, InventoryType.CHEST,
                      page_title + " - Page " + (page + 1)));
          previous_page = createMenuButton(Material.ARROW, "Previous Page");
        }
        page_array.get(page).setItem(18, previous_page);
      }
    }

  }

  ItemStack next_page;
  ItemStack previous_page;

  private void loadAdminSetTeams(Lobby lobby) {
    if (lobby instanceof WaitingLobby) {
      WaitingLobby waitingLobby = (WaitingLobby) lobby;
      for (Inventory inventory : Screen_adminSetTeams) {
        inventory.clear();
      }
      PlayerValueTester player_tester = (player) -> waitingLobby.getAdminSetTeamIsRed(player);
      create_player_selector_pages(Screen_adminSetTeams, lobby, createMenuButton(Material.RED_BANNER,
          ChatColor.RED + "Red Team"),
          createMenuButton(Material.BLUE_BANNER,
              ChatColor.BLUE + "Blue Team"),
          player_tester, "Admin Set Teams");
    }
  }

  private void loadAdminReadyPlayerScreen(Lobby lobby) {
    if (lobby instanceof WaitingLobby) {
      WaitingLobby waitingLobby = (WaitingLobby) lobby;
      for (Inventory inventory : Screen_adminReadyPlayer) {
        inventory.clear();
      }
      PlayerValueTester player_tester = (player) -> waitingLobby.isPlayerReady(player);
      create_player_selector_pages(Screen_adminReadyPlayer, lobby, createMenuButton(Material.RED_STAINED_GLASS_PANE,
          ChatColor.RED + "Unready "),
          createMenuButton(Material.GREEN_STAINED_GLASS_PANE,
              ChatColor.GREEN + "Ready "),
          player_tester, "Admin Ready Player");
    }
  }

  @EventHandler
  public void menuInteraction(InventoryClickEvent inventoryClickEvent) {
    if (isMenuInventory(inventoryClickEvent.getInventory())) {
      inventoryClickEvent.setCancelled(true);
      Player player = (Player) inventoryClickEvent.getWhoClicked();
      ItemStack clickedItem = inventoryClickEvent.getCurrentItem();
      Lobby playersLobby = lobbyManager.getPlayersLobby(player);

      if (clickedItem != null && playersLobby instanceof WaitingLobby) {
        WaitingLobby waitingLobby = (WaitingLobby) playersLobby;
        if (clickedItem.equals(UI_Button_gameModeSelect)) {
          player.openInventory(Screen_gameModeSelect);
        } else if (clickedItem.equals(UI_Button_mapSelect)) {
          loadMapsScreen(Screen_mapSelect);
          player.openInventory(Screen_mapSelect);
        } else if (clickedItem.equals(UI_Button_adminSetGameMode)) {
          player.openInventory(Screen_adminGameModeSelect);
        } else if (clickedItem.equals(UI_Button_adminSetMap)) {
          loadMapsScreen(Screen_adminMapSelect);
          player.openInventory(Screen_adminMapSelect);
        } else if (clickedItem.equals(UI_Button_ctfGameMode)) {
          player.closeInventory();
          if (inventoryClickEvent.getInventory() == Screen_adminGameModeSelect) {
            waitingLobby.setAdminGameModeSelection(GameMode.CAPTURE_THE_FLAG);
          } else {
            waitingLobby.registerGameModeVote(GameMode.CAPTURE_THE_FLAG, player);
          }
        } else if (clickedItem.equals(UI_Button_tdmGameMode)) {
          player.closeInventory();
          if (inventoryClickEvent.getInventory() == Screen_adminGameModeSelect) {
            waitingLobby.setAdminGameModeSelection(GameMode.TEAM_DEATH_MATCH);
          } else {
            waitingLobby.registerGameModeVote(GameMode.TEAM_DEATH_MATCH, player);
          }
        } else if (clickedItem.equals(UI_Button_dmGameMode)) {
          player.closeInventory();
          if (inventoryClickEvent.getInventory() == Screen_adminGameModeSelect) {
            waitingLobby.setAdminGameModeSelection(GameMode.DEATH_MATCH);
          } else {
            waitingLobby.registerGameModeVote(GameMode.DEATH_MATCH, player);
          }
        } else if (clickedItem.equals(UI_Button_cpGameMode)) {
          player.closeInventory();
          if (inventoryClickEvent.getInventory() == Screen_adminGameModeSelect) {
            waitingLobby.setAdminGameModeSelection(GameMode.CAPTURE_POINT);
          } else {
            waitingLobby.registerGameModeVote(GameMode.CAPTURE_POINT, player);
          }
        } else if (clickedItem.equals(UI_Button_adminReadyPlayers)) {
          loadAdminReadyPlayerScreen(playersLobby);
          player.openInventory(Screen_adminReadyPlayer.get(0));
        } else if (clickedItem.equals(UI_Button_adminSetPlayerTeams)) {
          loadAdminSetTeams(playersLobby);
          player.openInventory(Screen_adminSetTeams.get(0));
        } else if (inventoryClickEvent.getSlot() == 22 && inventoryClickEvent.getInventory() != Screen_adminMainMenu) {
          waitingLobby.togglePlayerReady(player);
          ItemStack UI_Button_ReadyUnready;
          if (waitingLobby.isPlayerReady(player)) {
            UI_Button_ReadyUnready = createMenuButton(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Unready");
          } else {
            UI_Button_ReadyUnready = createMenuButton(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Ready");
          }
          inventoryClickEvent.getInventory().setItem(22, UI_Button_ReadyUnready);
        }

        if (inventoryClickEvent.getInventory().equals(Screen_mapSelect)) {
          String mapName = clickedItem.getItemMeta().getDisplayName();
          player.closeInventory();
          waitingLobby.registerMapVote(mapName, player);
        }

        if (inventoryClickEvent.getInventory().equals(Screen_adminMapSelect)) {
          String mapName = clickedItem.getItemMeta().getDisplayName();
          player.closeInventory();
          waitingLobby.setAdminMapSelection(mapName);
        }

        if (Screen_adminReadyPlayer.contains(inventoryClickEvent.getInventory())) {
          if (clickedItem.equals(next_page)) {
            int current_page = Screen_adminReadyPlayer.indexOf(inventoryClickEvent.getInventory());
            player.openInventory(Screen_adminReadyPlayer.get(current_page + 1));
          }

          if (clickedItem.equals(previous_page)) {
            int current_page = Screen_adminReadyPlayer.indexOf(inventoryClickEvent.getInventory());
            player.openInventory(Screen_adminReadyPlayer.get(current_page - 1));
          }

          if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE
              || clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
            ItemStack player_head = inventoryClickEvent.getInventory().getItem(inventoryClickEvent.getSlot() - 9);
            SkullMeta head_meta = (SkullMeta) player_head.getItemMeta();
            OfflinePlayer player_to_toggle = head_meta.getOwningPlayer();
            if (player_to_toggle.isOnline()) {
              Player select_player = Bukkit.getPlayer(player_to_toggle.getUniqueId());
              waitingLobby.togglePlayerReady(select_player);

              ItemStack replace_item;
              if (waitingLobby.isPlayerReady(select_player)) {
                replace_item = createMenuButton(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Unready");
              } else {
                replace_item = createMenuButton(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Ready");
              }
              inventoryClickEvent.getInventory().setItem(inventoryClickEvent.getSlot(), replace_item);

            }
          }
        }

        if (Screen_adminSetTeams.contains(inventoryClickEvent.getInventory())) {
          if (clickedItem.equals(next_page)) {
            int current_page = Screen_adminSetTeams.indexOf(inventoryClickEvent.getInventory());
            player.openInventory(Screen_adminSetTeams.get(current_page + 1));
          }

          if (clickedItem.equals(previous_page)) {
            int current_page = Screen_adminSetTeams.indexOf(inventoryClickEvent.getInventory());
            player.openInventory(Screen_adminSetTeams.get(current_page - 1));
          }

          if (clickedItem.getType() == Material.RED_BANNER
              || clickedItem.getType() == Material.BLUE_BANNER) {
            ItemStack player_head = inventoryClickEvent.getInventory().getItem(inventoryClickEvent.getSlot() - 9);
            SkullMeta head_meta = (SkullMeta) player_head.getItemMeta();
            OfflinePlayer player_to_toggle = head_meta.getOwningPlayer();
            if (player_to_toggle.isOnline()) {
              Player select_player = Bukkit.getPlayer(player_to_toggle.getUniqueId());
              waitingLobby.adminToggleTeam(select_player);

              ItemStack replace_item;
              if (waitingLobby.getAdminSetTeamIsRed(select_player)) {
                replace_item = createMenuButton(Material.RED_BANNER, ChatColor.RED + "Red Team");
              } else {
                replace_item = createMenuButton(Material.BLUE_BANNER, ChatColor.BLUE + "Blue Team");
              }
              inventoryClickEvent.getInventory().setItem(inventoryClickEvent.getSlot(), replace_item);

            }
          }

        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
      }
    }
  }
}
