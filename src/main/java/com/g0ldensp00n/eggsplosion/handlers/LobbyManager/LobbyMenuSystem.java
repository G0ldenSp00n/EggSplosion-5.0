package com.g0ldensp00n.eggsplosion.handlers.LobbyManager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class LobbyMenuSystem implements Listener {

    private Map<Player, Inventory> Screen_Personal_lobbyMain;
    private Inventory Screen_adminMainMenu;
    private Inventory Screen_gameModeSelect;
    private Inventory Screen_mapSelect;
    private Inventory Screen_teamSelect;
    private ItemStack UI_Button_gameModeSelect;
    private ItemStack UI_Button_mapSelect;
    private ItemStack UI_Button_teamSelect;
    private ItemStack UI_Button_ctfGameMode;
    private ItemStack UI_Button_tdmGameMode;
    private ItemStack UI_Button_cpGameMode;
    private ItemStack UI_Button_dmGameMode;
    private ItemStack UI_Button_blueTeam;
    private ItemStack UI_Button_redTeam;
    private LobbyManager lobbyManager;
    private MapManager mapManager;

    public LobbyMenuSystem(Plugin plugin, LobbyManager lobbyManager, MapManager mapManager) {
      this.mapManager = mapManager;
      this.lobbyManager = lobbyManager;
      this.Screen_Personal_lobbyMain = new Hashtable<>();
      Bukkit.getPluginManager().registerEvents(this, plugin);

      // Game Mode Select Screen
      Screen_gameModeSelect = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Game Mode Select Menu");
      
      UI_Button_ctfGameMode = createMenuButton(Material.BLUE_BANNER, "Capture the Flag");
      UI_Button_tdmGameMode = createMenuButton(Material.LEATHER_CHESTPLATE, "Team Death Match");
      UI_Button_dmGameMode = createMenuButton(Material.LEATHER_HELMET, "Death Match");
      UI_Button_cpGameMode = createMenuButton(Material.BEACON, "Capture Point");

      //Configure TDM Button
      LeatherArmorMeta tdmMeta = (LeatherArmorMeta) UI_Button_tdmGameMode.getItemMeta();
      tdmMeta.setColor(Color.fromRGB(3949738));
      UI_Button_tdmGameMode.setItemMeta(tdmMeta);

      Screen_gameModeSelect.setItem(12, UI_Button_ctfGameMode);
      Screen_gameModeSelect.setItem(13, UI_Button_cpGameMode);
      Screen_gameModeSelect.setItem(14, UI_Button_tdmGameMode);
      Screen_gameModeSelect.setItem(22, UI_Button_dmGameMode);

      // Admin Screen
      Screen_adminMainMenu = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Admin Menu");

      // Map Select Screen
      Screen_mapSelect = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Map Select Menu");

      //Team Select Screen
      Screen_teamSelect = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Team Select Menu");

      UI_Button_blueTeam = createMenuButton(Material.BLUE_BANNER, "Blue Team");
      UI_Button_redTeam = createMenuButton(Material.RED_BANNER, "Red Team");

      Screen_teamSelect.setItem(12, UI_Button_blueTeam);
      Screen_teamSelect.setItem(14, UI_Button_redTeam);
    }

    private ItemStack createMenuButton(Material material, String displayName, List<String> lore) {
      List<String> formattedLore = new ArrayList<String>();
      for(String loreLine: lore) {
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
      return Screen_Personal_lobbyMain.containsValue(inventory) || inventory == Screen_gameModeSelect || inventory == Screen_mapSelect || inventory == Screen_teamSelect;
    }

    private boolean setupPersonalMenu(Inventory lobbyToSetup, Player player) {
      UI_Button_gameModeSelect = createMenuButton(Material.FIREWORK_ROCKET, "Game Mode Select");
      UI_Button_mapSelect = createMenuButton(Material.MAP, "Map Select");
      UI_Button_teamSelect = createMenuButton(Material.LEATHER_HELMET, "Team Select");
      ItemStack UI_Button_ReadyUnready;
      Lobby lobby = lobbyManager.getPlayersLobby(player);
      if (lobby instanceof WaitingLobby) {
        WaitingLobby waitingLobby = (WaitingLobby) lobby;
        if (waitingLobby.isPlayerReady(player)) {
          UI_Button_ReadyUnready = createMenuButton(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "Unready");
        } else {
          UI_Button_ReadyUnready = createMenuButton(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Ready");
        }

        lobbyToSetup.setItem(12, UI_Button_gameModeSelect);
        lobbyToSetup.setItem(13, UI_Button_teamSelect);
        lobbyToSetup.setItem(14, UI_Button_mapSelect);
        lobbyToSetup.setItem(22, UI_Button_ReadyUnready);
        return true;
      }
      return false;
    }

    @EventHandler
    public void menuOpen(PlayerInteractEvent playerInteractEvent) {
        if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK) || playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_AIR)) {
          if (playerInteractEvent.getItem() != null) {
            if (playerInteractEvent.getItem().getType().equals(Material.EMERALD)) {
              Player player = playerInteractEvent.getPlayer();
              Lobby lobby = lobbyManager.getPlayersLobby(player);

              if (!(lobby instanceof MainLobby)) {
                Inventory Screen_lobbyMain;

                if (lobby.isAdmin(player)) {
                  Screen_lobbyMain = Screen_adminMainMenu;
                }
                // Main Screen
                else if(Screen_Personal_lobbyMain.containsKey(player)) {
                  Screen_lobbyMain = Screen_Personal_lobbyMain.get(player);
                } else {
                  Screen_lobbyMain = Bukkit.getServer().createInventory(null, InventoryType.CHEST, "Lobby Main Menu");
                  Screen_Personal_lobbyMain.put(player, Screen_lobbyMain);
                }

                if (lobby instanceof WaitingLobby) {
                  if (!lobby.isAdmin(player)) {
                    setupPersonalMenu(Screen_lobbyMain, player);
                  }
                  playerInteractEvent.getPlayer().openInventory(Screen_lobbyMain);
                } else {
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Lobby Menu can only be opened in the waiting room"));
                }
              } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Lobby Menu can't be opened in the Main Lobby"));
              }
            }
          }
        }
    }

    private void loadMapsScreen() {
      Screen_mapSelect.clear();

      Map<String, GameMap> maps = mapManager.getMaps();
      for (String mapName: maps.keySet()) {
        if (!mapName.equalsIgnoreCase("WAITING_ROOM")) {
          ItemStack mapSelectButton;
          if (maps.get(mapName).getMapIcon() != null) {
            mapSelectButton = createMenuButton(maps.get(mapName).getMapIcon(), mapName);
          } else {
            mapSelectButton = createMenuButton(Material.MAP, mapName);
          }
          Screen_mapSelect.addItem(mapSelectButton);
        }
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
            loadMapsScreen();
            player.openInventory(Screen_mapSelect);
          } else if (clickedItem.equals(UI_Button_teamSelect)) {
            player.openInventory(Screen_teamSelect);
          } else if (clickedItem.equals(UI_Button_ctfGameMode)) {
            player.closeInventory();
            waitingLobby.registerGameModeVote(GameMode.CAPTURE_THE_FLAG, player);
          } else if (clickedItem.equals(UI_Button_tdmGameMode)) {
            player.closeInventory();
            waitingLobby.registerGameModeVote(GameMode.TEAM_DEATH_MATCH, player);
          } else if (clickedItem.equals(UI_Button_dmGameMode)) {
            player.closeInventory();
            waitingLobby.registerGameModeVote(GameMode.DEATH_MATCH, player);
          } else if (clickedItem.equals(UI_Button_cpGameMode)) {
            player.closeInventory();
            waitingLobby.registerGameModeVote(GameMode.CAPTURE_POINT, player);
          } else if (clickedItem.equals(UI_Button_blueTeam)) {
            player.closeInventory();
            waitingLobby.registerBlueTeamSelection(player);
          } else if (clickedItem.equals(UI_Button_redTeam)) {
            player.closeInventory();
            waitingLobby.registerRedTeamSelection(player);
          } else if (inventoryClickEvent.getSlot() == 22) {
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
          player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        }
      }
    }
}
