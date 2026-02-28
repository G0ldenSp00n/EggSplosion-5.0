package com.g0ldensp00n.eggsplosion.handlers.MapManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import com.g0ldensp00n.eggsplosion.EggSplosion;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyTypes.Lobby;
import com.g0ldensp00n.eggsplosion.handlers.Utils.Utils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MapManager implements Listener, CommandExecutor, TabCompleter {
  private ItemStack Map_Tool_boundary;
  private Map<Player, List<Location>> boundaryToolTracker;
  private Map<String, GameMap> gameMaps;
  private Map<String, Inventory> gameMapEquipment;
  private String pluginFolder;
  private LobbyManager lobbyManager;
  private ItemStack disabledSlot;

  public MapManager(Plugin plugin, String pluginFolder) {
    this.lobbyManager = LobbyManager.getInstance(plugin, this);
    this.pluginFolder = pluginFolder;
    Bukkit.getPluginManager().registerEvents(this, plugin);

    boundaryToolTracker = new Hashtable<>();
    gameMaps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    gameMapEquipment = new Hashtable<>();
    loadMapsFromFiles();
  }

  public GameMap getMapByName(String mapName) {
    return gameMaps.get(mapName);
  }

  private List<Location> convertToLocationList(List<?> list) {
    List<Location> locationList = new ArrayList<>();
    if (list != null) {
      Iterator<?> listIterator = list.iterator();
      while (listIterator.hasNext()) {
        Object nextObject = listIterator.next();
        if (nextObject instanceof Location) {
          Location loc = (Location) nextObject;
          locationList.add(loc);
        }
      }
    }
    return locationList;
  }

  public Map<String, GameMap> getMaps() {
    return gameMaps;
  }

  private void loadMapsFromFiles() {
    File mapFolder = new File(pluginFolder, "map");
    if (mapFolder != null && mapFolder.listFiles() != null) {
      for (File file : mapFolder.listFiles()) {
        String fileName = file.getName().substring(0, file.getName().length() - 5);
        FileConfiguration mapConfigFile = YamlConfiguration.loadConfiguration(file);
        GameMap map = new GameMap(mapConfigFile.getLocation("cornerA"), mapConfigFile.getLocation("cornerB"));
        List<?> soloSpawnItems = mapConfigFile.getList("soloSpawnLocations");
        List<?> sideASpawnItems = mapConfigFile.getList("teamASpawnLocations");
        List<?> sideBSpawnItems = mapConfigFile.getList("teamBSpawnLocations");

        List<Location> soloSpawnLocations = convertToLocationList(soloSpawnItems);
        List<Location> sideASpawnLocations = convertToLocationList(sideASpawnItems);
        List<Location> sideBSpawnLocations = convertToLocationList(sideBSpawnItems);
        map.loadMapFromFile(soloSpawnLocations, sideASpawnLocations, sideBSpawnLocations,
            mapConfigFile.getLocation("teamAFlagLocation"), mapConfigFile.getLocation("teamBFlagLocation"));

        map.setDoSideSwitch(mapConfigFile.getBoolean("doSideSwitch"));
        map.setDoFlagMessages(mapConfigFile.getBoolean("doFlagMessages"));
        map.setAllowItemDrop(mapConfigFile.getBoolean("allowItemDrop"));
        map.setAllowItemPickup(mapConfigFile.getBoolean("allowItemPickup"));
        map.setAllowHelmetRemoval(mapConfigFile.getBoolean("allowHelmetRemoval"));
        map.setAllowChestplateRemoval(mapConfigFile.getBoolean("allowChestplateRemoval"));
        map.setAllowLeggingRemoval(mapConfigFile.getBoolean("allowLeggingRemoval"));
        map.setAllowBootRemoval(mapConfigFile.getBoolean("allowBootRemoval"));
        map.setResetInventoryOnSpawn(mapConfigFile.getBoolean("resetInventoryOnSpawn"));
        if (mapConfigFile.getInt("pointsToWinCTF") != 0) {
          map.setPointsToWinCTF(mapConfigFile.getInt("pointsToWinCTF"));
        }

        List<String> capturePointNames = mapConfigFile.getStringList("capturePointNames");
        if (capturePointNames.size() > 0) {
          for (String capturePointName : capturePointNames) {
            Location capturePointLocation = mapConfigFile.getLocation("capturePoint" + capturePointName);
            if (capturePointLocation != null) {
              map.addCapturePoint(capturePointName, capturePointLocation);
            }
          }
        }

        if (mapConfigFile.getInt("pointsToWinTDM") != 0) {
          map.setPointsToWinTDM(mapConfigFile.getInt("pointsToWinTDM"));
        }

        if (mapConfigFile.getInt("pointsToWinDM") != 0) {
          map.setPointsToWinDM(mapConfigFile.getInt("pointsToWinDM"));
        }

        if (mapConfigFile.getInt("flagSpawnDelay") != 0) {
          map.setFlagSpawnDelay(mapConfigFile.getInt("flagSpawnDelay"));
        }

        List<?> mapEffects = mapConfigFile.getList("mapEffects");

        if (mapEffects != null) {
          Iterator<?> mapEffectIterator = mapEffects.iterator();
          while (mapEffectIterator.hasNext()) {
            Object nextObject = mapEffectIterator.next();
            if (nextObject instanceof PotionEffect) {
              map.addMapEffect((PotionEffect) nextObject);
            }
          }
        }

        List<?> playerLoadout = mapConfigFile.getList("playerLoadout");

        if (playerLoadout != null) {
          ItemStack[] playerLoadoutContents = new ItemStack[36];
          for (Integer slot = 0; slot < 36; slot++) {
            Object nextObject = playerLoadout.get(slot);
            if (nextObject instanceof ItemStack) {
              playerLoadoutContents[slot] = (ItemStack) nextObject;
            }
          }
          map.setLoadoutContents(playerLoadoutContents);
        }

        ItemStack helmet = mapConfigFile.getItemStack("helmet");
        ItemStack chestplate = mapConfigFile.getItemStack("chestplate");
        ItemStack leggings = mapConfigFile.getItemStack("leggings");
        ItemStack boots = mapConfigFile.getItemStack("boots");

        if (helmet != null || chestplate != null || leggings != null || boots != null) {
          Inventory mapEquipmentMenu = Bukkit.createInventory(null, InventoryType.HOPPER, "Player Equipment Menu");

          disabledSlot = createMapTool(Material.RED_STAINED_GLASS_PANE, "Disabled");
          mapEquipmentMenu.setItem(4, disabledSlot);
          gameMapEquipment.put(fileName, mapEquipmentMenu);

          if (helmet != null) {
            map.setHelmet(helmet);
            mapEquipmentMenu.setItem(0, helmet);
          }

          if (chestplate != null) {
            map.setChestplate(chestplate);
            mapEquipmentMenu.setItem(1, chestplate);
          }

          if (leggings != null) {
            map.setLeggings(leggings);
            mapEquipmentMenu.setItem(2, leggings);
          }

          if (boots != null) {
            map.setBoots(boots);
            mapEquipmentMenu.setItem(3, boots);
          }
        }

        if (mapConfigFile.getItemStack("mapIcon") != null) {
          map.setMapIcon(mapConfigFile.getItemStack("mapIcon").getType());
        }

        gameMaps.put(fileName, map);
      }
    }
  }

  public void saveMapsToFiles() {
    for (String mapName : gameMaps.keySet()) {
      if (mapName != null) {
        GameMap map = gameMaps.get(mapName);

        File oldConfigToReset = new File(pluginFolder, "map/" + mapName + ".yaml");
        oldConfigToReset.delete();
        File mapFile = new File(pluginFolder, "map/" + mapName + ".yaml");
        FileConfiguration mapConfigFile = YamlConfiguration.loadConfiguration(mapFile);

        if (map.getCornerA() != null) {
          mapConfigFile.set("cornerA", map.getCornerA());
        }

        if (map.getCornerB() != null) {
          mapConfigFile.set("cornerB", map.getCornerB());
        }

        if (map.getMapEffects().size() > 0) {
          mapConfigFile.set("mapEffects", map.getMapEffects());
        }

        if (map.getMapIcon() != null) {
          mapConfigFile.set("mapIcon", createMapTool(map.getMapIcon(), "mapIcon"));
        }

        mapConfigFile.set("doSideSwitch", map.getDoSideSwitch());
        mapConfigFile.set("doFlagMessages", map.getDoFlagMessages());
        mapConfigFile.set("pointsToWinCTF", map.getPointsToWinCTF());
        mapConfigFile.set("pointsToWinTDM", map.getPointsToWinTDM());
        mapConfigFile.set("pointsToWinDM", map.getPointsToWinDM());
        mapConfigFile.set("flagSpawnDelay", map.getFlagSpawnDelay());
        mapConfigFile.set("allowItemDrop", map.getAllowItemDrop());
        mapConfigFile.set("allowItemPickup", map.getAllowItemPickup());
        mapConfigFile.set("allowHelmetRemoval", map.getAllowHelmetRemoval());
        mapConfigFile.set("allowChestplateRemoval", map.getAllowChestplateRemoval());
        mapConfigFile.set("allowLeggingRemoval", map.getAllowLeggingRemoval());
        mapConfigFile.set("allowBootRemoval", map.getAllowBootRemoval());
        mapConfigFile.set("resetInventoryOnSpawn", map.getResetInventoryOnSpawn());

        if (map.getSideAFlagLocation() != null) {
          mapConfigFile.set("teamAFlagLocation", map.getSideAFlagLocation());
        }

        if (map.getSideBFlagLocation() != null) {
          mapConfigFile.set("teamBFlagLocation", map.getSideBFlagLocation());
        }

        if (map.getSoloSpawnLocations() != null) {
          mapConfigFile.set("soloSpawnLocations", map.getSoloSpawnLocations());
        }

        if (map.getSideAFlagLocation() != null) {
          mapConfigFile.set("teamASpawnLocations", map.getSideASpawnLocations());
        }

        if (map.getSideBFlagLocation() != null) {
          mapConfigFile.set("teamBSpawnLocations", map.getSideBSpawnLocations());
        }

        if (map.getHelmet() != null) {
          mapConfigFile.set("helmet", map.getHelmet());
        }

        if (map.getLoadout() != null) {
          mapConfigFile.set("playerLoadout", map.getLoadout().getContents());
        }

        if (map.getChestplate() != null) {
          mapConfigFile.set("chestplate", map.getChestplate());
        }

        if (map.getLeggings() != null) {
          mapConfigFile.set("leggings", map.getLeggings());
        }

        if (map.getBoots() != null) {
          mapConfigFile.set("boots", map.getBoots());
        }

        List<String> capturePointNames = map.getAllCapturePointName();
        if (capturePointNames.size() > 0) {
          mapConfigFile.set("capturePointNames", capturePointNames);
          for (String capturePointName : capturePointNames) {
            Location capturePointLocation = map.getCapturePoint(capturePointName);
            if (capturePointLocation != null) {
              mapConfigFile.set("capturePoint" + capturePointName, capturePointLocation);
            }
          }
        }

        try {
          mapConfigFile.save(mapFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private ItemStack createMapTool(Material material, String displayName, List<String> lore) {
    List<String> formattedLore = new ArrayList<String>();
    for (String loreLine : lore) {
      formattedLore.add(ChatColor.RESET + loreLine);
    }
    ItemStack Map_Tool = new ItemStack(material);
    ItemMeta Map_Tool_Meta = Map_Tool.getItemMeta();
    Map_Tool_Meta.setDisplayName(ChatColor.RESET + displayName);
    Map_Tool_Meta.setLore(formattedLore);
    Map_Tool_Meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    Map_Tool.setItemMeta(Map_Tool_Meta);
    return Map_Tool;
  }

  private ItemStack createMapTool(Material material, String displayName) {
    return createMapTool(material, displayName, new ArrayList<String>());
  }

  @EventHandler
  public void playerMoveEvent(PlayerMoveEvent playerMoveEvent) {
    Player player = playerMoveEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);
    if (playerLobby != null && playerLobby != lobbyManager.getMainLobby()) {
      if (playerLobby.getMap() != null) {
        if (!playerLobby.getMap().locationInMap(playerMoveEvent.getTo())) {
          playerMoveEvent.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void PlayerTeleportEvent(PlayerTeleportEvent playerTeleportEvent) {
    Player player = playerTeleportEvent.getPlayer();
    Lobby playerLobby = lobbyManager.getPlayersLobby(player);

    if (playerLobby != null && playerLobby != lobbyManager.getMainLobby()) {
      if (playerLobby.getMap() != null) {
        if (!playerLobby.getMap().locationInMap(playerTeleportEvent.getTo())) {
          playerTeleportEvent.setCancelled(true);
          player.sendMessage("[EggSplosion] Teleport out of map cancelled");
        }
      }
    }
  }

  @EventHandler
  public void playerUseMappingTools(PlayerInteractEvent playerInteractEvent) {
    if (playerInteractEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      ItemStack mappingTool = playerInteractEvent.getItem();
      Player player = playerInteractEvent.getPlayer();
      if (mappingTool != null) {
        if (mappingTool.equals(Map_Tool_boundary)) {
          playerInteractEvent.setCancelled(true);
          if (boundaryToolTracker.get(player) == null || boundaryToolTracker.get(player).size() == 2) {
            List<Location> locationList = new ArrayList<>();
            Location cornerA = playerInteractEvent.getClickedBlock().getLocation();
            locationList.add(cornerA);
            boundaryToolTracker.put(player, locationList);
            player.sendMessage("Added Corner A (" + cornerA.getX() + ", " + cornerA.getZ() + ") to Boundary");
          } else {
            List<Location> locationList = boundaryToolTracker.get(player);
            Location cornerB = playerInteractEvent.getClickedBlock().getLocation();
            locationList.add(cornerB);
            boundaryToolTracker.put(player, locationList);
            player.sendMessage("Added Corner B (" + cornerB.getX() + ", " + cornerB.getZ() + ") to Boundary");
            player.sendMessage("Now run the command /map create <mapName>, to create the map");
            player.updateCommands();
          }
        } else if (mappingTool.getType().equals(Material.IRON_AXE)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          if (itemLore.get(0) != null && (itemLore.get(0).split(":").length >= 1)) {
            String mapName = itemLore.get(0).split(":")[1].trim();
            GameMap map = gameMaps.get(mapName);
            if (map != null) {
              Location spawnPoint = playerInteractEvent.getClickedBlock().getLocation();
              spawnPoint.setYaw(player.getLocation().getYaw());
              if (map.locationInMap(spawnPoint)) {
                map.addSoloSpawnpoint(spawnPoint);
                player.sendMessage("Added Solo Spawn Point (" + spawnPoint.getBlockX() + ", " + spawnPoint.getBlockY()
                    + ", " + spawnPoint.getBlockZ() + ")");
              } else {
                player.sendMessage("Spawn Point must be in Map Boundary");
              }
            }
          }
        } else if (mappingTool.getType().equals(Material.GOLDEN_AXE)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          if (itemLore.get(0) != null && (itemLore.get(0).split(":").length >= 1)) {
            String mapName = itemLore.get(0).split(":")[1].trim();
            GameMap map = gameMaps.get(mapName);
            if (map != null) {
              Location spawnPoint = playerInteractEvent.getClickedBlock().getLocation();
              spawnPoint.setYaw(player.getLocation().getYaw());
              if (map.locationInMap(spawnPoint)) {
                map.addSideASpawnPoint(spawnPoint);
                player.sendMessage("Added" + ChatColor.RED + " Team A " + ChatColor.RESET + "Spawn Point ("
                    + spawnPoint.getBlockX() + ", " + spawnPoint.getBlockY() + ", " + spawnPoint.getBlockZ() + ")");
              } else {
                player.sendMessage("Spawn Point must be in Map Boundary");
              }
            }
          }
        } else if (mappingTool.getType().equals(Material.DIAMOND_AXE)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          if (itemLore.get(0) != null && (itemLore.get(0).split(":").length >= 1)) {
            String mapName = itemLore.get(0).split(":")[1].trim();
            GameMap map = gameMaps.get(mapName);
            if (map != null) {
              Location spawnPoint = playerInteractEvent.getClickedBlock().getLocation();
              spawnPoint.setYaw(player.getLocation().getYaw());
              if (map.locationInMap(spawnPoint)) {
                map.addSideBSpawnPoint(spawnPoint);
                player.sendMessage("Added" + ChatColor.BLUE + " Team B " + ChatColor.RESET + "Spawn Point ("
                    + spawnPoint.getBlockX() + ", " + spawnPoint.getBlockY() + ", " + spawnPoint.getBlockZ() + ")");
              } else {
                player.sendMessage("Spawn Point must be in Map Boundary");
              }
            }
          }
        } else if (mappingTool.getType().equals(Material.GOLDEN_SHOVEL)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          if (itemLore.get(0) != null && (itemLore.get(0).split(":").length >= 1)) {
            String mapName = itemLore.get(0).split(":")[1].trim();
            GameMap map = gameMaps.get(mapName);
            if (map != null) {
              Location flagLocation = playerInteractEvent.getClickedBlock().getLocation();
              if (map.locationInMap(flagLocation)) {
                map.setSideAFlagLocation(flagLocation);
                player.sendMessage(
                    "Set" + ChatColor.RED + " Team A " + ChatColor.RESET + "Flag Location (" + flagLocation.getBlockX()
                        + ", " + flagLocation.getBlockY() + ", " + flagLocation.getBlockZ() + ")");
              } else {
                player.sendMessage("Flag Location must be in Map Boundary");
              }
            }
          }
        } else if (mappingTool.getType().equals(Material.DIAMOND_SHOVEL)) {
          List<String> itemLore = mappingTool.getItemMeta().getLore();
          if (itemLore.get(0) != null && (itemLore.get(0).split(":").length >= 1)) {
            String mapName = itemLore.get(0).split(":")[1].trim();
            GameMap map = gameMaps.get(mapName);
            if (map != null) {
              Location flagLocation = playerInteractEvent.getClickedBlock().getLocation();
              if (map.locationInMap(flagLocation)) {
                map.setSideBFlagLocation(flagLocation);
                player.sendMessage(
                    "Set" + ChatColor.BLUE + " Team B " + ChatColor.RESET + "Flag Location (" + flagLocation.getBlockX()
                        + ", " + flagLocation.getBlockY() + ", " + flagLocation.getBlockZ() + ")");
              } else {
                player.sendMessage("Flag Location must be in Map Boundary");
              }
            }
          }
        }
      }
    }
  }

  @EventHandler
  public void playerInteractEvent(InventoryClickEvent inventoryClickEvent) {
    HumanEntity humanEntity = inventoryClickEvent.getWhoClicked();
    if (humanEntity instanceof Player player) {
      if (inventoryClickEvent.getView().getTitle().equals("Player Equipment Menu")
          && inventoryClickEvent.getCurrentItem() != null) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
        if (inventoryClickEvent.getCurrentItem().equals(disabledSlot)) {
          inventoryClickEvent.setCancelled(true);
        }
      }
    }
  }

  @EventHandler
  public void playerCloseEvent(InventoryCloseEvent inventoryCloseEvent) {
    if (inventoryCloseEvent.getView().getTitle().equals("Player Equipment Menu")) {
      String mapName = null;
      for (Entry<String, Inventory> entry : gameMapEquipment.entrySet()) {
        if (entry.getValue().equals(inventoryCloseEvent.getInventory())) {
          mapName = entry.getKey();
        }
      }

      if (mapName != null) {
        GameMap gameMap = gameMaps.get(mapName);
        if (gameMap != null) {
          for (Integer i = 0; i < 4; i++) {
            if (inventoryCloseEvent.getInventory().getItem(i) != null) {
              gameMap.setArmor(i, inventoryCloseEvent.getInventory().getItem(i));
            }
          }
        }
      }
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    if (commandLabel.equalsIgnoreCase("map")) {
      if (args.length > 0) {
        switch (args[0]) {
          case "locate":
            if (args.length > 1) {
              if (sender instanceof Player && gameMaps.get(args[1]) != null) {
                Player player = (Player) sender;
                Location to_teleport = gameMaps.get(args[1]).getSpawnPoint();
                if (to_teleport != null) {
                  player.teleport(to_teleport);
                } else {
                  to_teleport = gameMaps.get(args[1]).getCornerA();
                  player.teleport(to_teleport);
                }
                return true;
              }
            }
            break;
          case "tools":
            if (args.length > 1) {
              if (sender instanceof Player && gameMaps.get(args[1]) != null) {
                Player player = (Player) sender;
                player.getInventory().clear();

                List<String> lore = new ArrayList<>();
                lore.add("Map: " + args[1]);

                ItemStack Map_Tool_spawnPointsSolo = createMapTool(Material.IRON_AXE, "Spawn Point Tool - Solo", lore);
                player.getInventory().addItem(Map_Tool_spawnPointsSolo);

                ItemStack Map_Tool_spawnPointsTeamA = createMapTool(Material.GOLDEN_AXE, "Spawn Point Tool - Team A",
                    lore);
                ItemStack Map_Tool_spawnPointsTeamB = createMapTool(Material.DIAMOND_AXE, "Spawn Point Tool - Team B",
                    lore);

                ItemStack Map_Tool_flagSpawnTeamA = createMapTool(Material.GOLDEN_SHOVEL, "Flag Spawn - Team A", lore);
                ItemStack Map_Tool_flagSpawnTeamB = createMapTool(Material.DIAMOND_SHOVEL, "Flag Spawn - Team B", lore);

                player.getInventory().setItem(2, Map_Tool_spawnPointsTeamA);
                player.getInventory().setItem(3, Map_Tool_spawnPointsTeamB);

                player.getInventory().setItem(5, Map_Tool_flagSpawnTeamA);
                player.getInventory().setItem(6, Map_Tool_flagSpawnTeamB);

                return true;
              }
            } else {
              if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().clear();

                Map_Tool_boundary = createMapTool(Material.WOODEN_SHOVEL, "Boundary Tool");
                player.getInventory().addItem(Map_Tool_boundary);

                player.sendMessage(
                    "[EggSplosion] Select the boudarys of the map by right clicking the shovel, then run the command /map create <mapName>");
                return true;
              }
            }
            break;
          case "create":
            if (args.length > 1) {
              if (sender instanceof Player) {
                Player player = (Player) sender;
                if (boundaryToolTracker.get(player) != null && boundaryToolTracker.get(player).size() == 2) {
                  player.sendMessage("Map " + ChatColor.GRAY + args[1] + ChatColor.RESET + " created");
                  GameMap map = new GameMap(boundaryToolTracker.get(player).get(0),
                      boundaryToolTracker.get(player).get(1));
                  gameMaps.put(args[1], map);
                  boundaryToolTracker.remove(player);

                  player.performCommand("map tools " + args[1]);
                  return true;
                }
              }
            }
            break;
          case "equip":
            if (args.length > 1) {
              GameMap map = gameMaps.get(args[1]);
              if (map != null && sender instanceof Player) {
                Player player = (Player) sender;
                Inventory mapEquipmentMenu;
                if (gameMapEquipment.get(args[1]) != null) {
                  mapEquipmentMenu = gameMapEquipment.get(args[1]);
                } else {
                  mapEquipmentMenu = Bukkit.createInventory(null, InventoryType.HOPPER, "Player Equipment Menu");

                  disabledSlot = createMapTool(Material.RED_STAINED_GLASS_PANE, "Disabled");
                  mapEquipmentMenu.setItem(4, disabledSlot);
                  gameMapEquipment.put(args[1], mapEquipmentMenu);
                }

                player.openInventory(mapEquipmentMenu);
              } else {
                sender.sendMessage("[EggSplosion] Map doesn't exists, create it to set its equipment");
              }
            }
            break;
          case "loadout":
            if (args.length > 1) {
              GameMap map = gameMaps.get(args[1]);
              if (map != null && sender instanceof Player) {
                Player player = (Player) sender;
                Inventory mapLoadoutMenu = map.getLoadout();
                player.openInventory(mapLoadoutMenu);
              } else {
                sender.sendMessage("[EggSplosion] Map doesn't exists, create it to set its equipment");
              }
            }
            break;
          case "gamerule":
            if (args.length > 2) {
              GameMap map = gameMaps.get(args[1]);
              if (map != null) {
                if (args.length == 3) {
                  switch (args[2]) {
                    case "doSideSwitch":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule doSideSwitch is currently set to: " + map.getDoSideSwitch());
                      break;
                    case "doFlagMessages":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule doFlagMessages is currently set to: " + map.getDoFlagMessages());
                      break;
                    case "allowItemDrop":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule allowItemDrop is currently set to: " + map.getAllowItemDrop());
                      break;
                    case "allowItemPickup":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule allowItemPickup is currently set to: " + map.getAllowItemPickup());
                      break;
                    case "pointsToWinCTF":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule pointsToWinCTF is currently set to: " + map.getPointsToWinCTF());
                      break;
                    case "pointsToWinTDM":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule pointsToWinTDM is currently set to: " + map.getPointsToWinTDM());
                      break;
                    case "pointsToWinDM":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule pointsToWinDM is currently set to: " + map.getPointsToWinDM());
                      break;
                    case "allowHelmetRemoval":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule allowHelmetRemoval is currently set to: " + map.getAllowHelmetRemoval());
                      break;
                    case "allowChestplateRemoval":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule allowChestplateRemoval is currently set to: " + map.getAllowChestplateRemoval());
                      break;
                    case "allowLeggingRemoval":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule allowLeggingRemoval is currently set to: " + map.getAllowLeggingRemoval());
                      break;
                    case "allowBootRemoval":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule allowBootRemoval is currently set to: " + map.getAllowBootRemoval());
                      break;
                    case "flagSpawnDelay":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule flagSpawnDelay is currently set to: " + map.getFlagSpawnDelay());
                      break;
                    case "resetInventoryOnSpawn":
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule resetInventoryOnSpawn is currently set to: " + map.getResetInventoryOnSpawn());
                      break;

                    default:
                      return false;
                  }
                  return true;
                } else if (args.length == 4) {
                  switch (args[2]) {
                    case "doSideSwitch":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setDoSideSwitch(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule doSideSwitch is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setDoSideSwitch(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule doSideSwitch is now set to: false");
                        return true;
                      }
                      break;
                    case "doFlagMessages":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setDoFlagMessages(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule doFlagMessages is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setDoFlagMessages(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule doFlagMessages is now set to: false");
                        return true;
                      }
                      break;
                    case "allowItemDrop":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setAllowItemDrop(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowItemDrop is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setAllowItemDrop(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowItemDrop is now set to: false");
                        return true;
                      }
                      break;
                    case "allowItemPickup":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setAllowItemPickup(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowItemPickup is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setAllowItemPickup(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowItemPickup is now set to: false");
                        return true;
                      }
                      break;
                    case "allowHelmetRemoval":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setAllowHelmetRemoval(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowHelmetRemoval is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setAllowHelmetRemoval(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowHelmetRemoval is now set to: false");
                        return true;
                      }
                      break;
                    case "allowChestplateRemoval":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setAllowChestplateRemoval(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowChestplateRemoval is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setAllowHelmetRemoval(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowChestplateRemoval is now set to: false");
                        return true;
                      }
                      break;
                    case "allowLeggingRemoval":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setAllowLeggingRemoval(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowLeggingRemoval is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setAllowLeggingRemoval(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowLeggingRemoval is now set to: false");
                        return true;
                      }
                      break;
                    case "allowBootRemoval":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setAllowBootRemoval(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowBootRemoval is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setAllowBootRemoval(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule allowBootRemoval is now set to: false");
                        return true;
                      }
                      break;
                    case "resetInventoryOnSpawn":
                      if (args[3].equalsIgnoreCase("true")) {
                        map.setResetInventoryOnSpawn(true);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule resetInventoryOnSpawn is now set to: true");
                        return true;
                      } else if (args[3].equalsIgnoreCase("false")) {
                        map.setResetInventoryOnSpawn(false);
                        sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " Gamerule resetInventoryOnSpawn is now set to: false");
                        return true;
                      }
                      break;

                    case "pointsToWinCTF":
                      Integer pointsToWin;
                      try {
                        pointsToWin = Integer.parseInt(args[3]);
                      } catch (NumberFormatException e) {
                        sender.sendMessage("[EggSplosion] You must specify the points");
                        return true;
                      }
                      map.setPointsToWinCTF(pointsToWin);
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule pointsToWinCTF is now set to: " + pointsToWin);
                      return true;
                    case "pointsToWinTDM":
                      Integer pointsToWinTDM;
                      try {
                        pointsToWinTDM = Integer.parseInt(args[3]);
                      } catch (NumberFormatException e) {
                        sender.sendMessage("[EggSplosion] You must specify the points");
                        return true;
                      }
                      map.setPointsToWinTDM(pointsToWinTDM);
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule pointsToWinCTF is now set to: " + pointsToWinTDM);
                      return true;
                    case "pointsToWinDM":
                      Integer pointsToWinDM;
                      try {
                        pointsToWinDM = Integer.parseInt(args[3]);
                      } catch (NumberFormatException e) {
                        sender.sendMessage("[EggSplosion] You must specify the points");
                        return true;
                      }
                      map.setPointsToWinDM(pointsToWinDM);
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule pointsToWinCTF is now set to: " + pointsToWinDM);
                      return true;
                    case "flagSpawnDelay":
                      Integer flagSpawnDelay;
                      try {
                        flagSpawnDelay = Integer.parseInt(args[3]);
                      } catch (NumberFormatException e) {
                        sender.sendMessage("[EggSplosion] You must specify the points");
                        return true;
                      }
                      map.setFlagSpawnDelay(flagSpawnDelay);
                      sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Gamerule flagSpawnDelay is now set to: " + flagSpawnDelay);
                      return true;
                    default:
                      return false;
                  }

                }
              }
            }
            break;
          case "effect":
            if (args.length > 3) {
              GameMap map = gameMaps.get(args[1]);
              if (map != null) {
                Integer amplifier = 1;
                switch (args.length) {
                  case 5:
                    try {
                      amplifier = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                      // Suppress
                    }
                    // Fall Through
                  case 4:
                    String potionEffectNameFull = args[3];
                    if (potionEffectNameFull.split(":").length > 1) {
                      NamespacedKey potionEffectTypeKey = NamespacedKey.fromString(potionEffectNameFull);
                      if (args[2].equals("add")) {
                        PotionEffect matchingPotionEffect = null;
                        for (PotionEffect potionEffect : map.getMapEffects()) {
                          if (potionEffect.getType().getKey().equals(potionEffectTypeKey)) {
                            matchingPotionEffect = potionEffect;
                          }
                        }

                        if (matchingPotionEffect == null) {
                          PotionEffectType potionEffectType = Registry.MOB_EFFECT.get(potionEffectTypeKey);
                          map.addMapEffect(potionEffectType, amplifier);
                          sender.sendRichMessage(
                              "[EggSplosion] Map <aqua><map></aqua> effect <gray><effect> <amplifier1></gray> <green>added</green>",
                              Placeholder.component("map", Component.text(args[1])),
                              Placeholder.component("effect",
                                  Component.translatable(potionEffectType.translationKey())),
                              Placeholder.component("amplifier1",
                                  Component.translatable("enchantment.level." + amplifier)));
                        } else {
                          map.removeMapEffect(matchingPotionEffect);
                          map.addMapEffect(matchingPotionEffect.withAmplifier(amplifier - 1));
                          sender.sendRichMessage(
                              "[EggSplosion] Map <aqua><map></aqua> effect <gray><effect> <amplifier1></gray> updated to <gray><effect> <amplifier2></gray>",
                              Placeholder.component("map", Component.text(args[1])),
                              Placeholder.component("effect",
                                  Component.translatable(matchingPotionEffect.getType().translationKey())),
                              Placeholder.component("amplifier1",
                                  Component
                                      .translatable("enchantment.level." + (matchingPotionEffect.getAmplifier() + 1))),
                              Placeholder.component("amplifier2",
                                  Component.translatable("enchantment.level." + amplifier)));
                          // sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] +
                          // ChatColor.RESET
                          // + " Effect " + ChatColor.GRAY +
                          // matchingPotionEffect.getType().translationKey() + " "
                          // + amplifier + ChatColor.RESET
                          // + " Updated!");
                        }
                      } else if (args[2].equals("remove")) {
                        PotionEffect potionEffectToRemove = null;
                        PotionEffectType potionEffectType = Registry.MOB_EFFECT.get(potionEffectTypeKey);
                        for (PotionEffect potionEffect : map.getMapEffects()) {
                          if (potionEffect.getType().getKey().equals(potionEffectTypeKey)) {
                            potionEffectToRemove = potionEffect;
                          }
                        }

                        if (potionEffectToRemove != null) {
                          map.removeMapEffect(potionEffectToRemove);
                          sender.sendRichMessage(
                              "[EggSplosion] Map <aqua><map></aqua> effect <gray><effect> <amplifier1></gray> <red>removed</red>",
                              Placeholder.component("map", Component.text(args[1])),
                              Placeholder.component("effect",
                                  Component.translatable(potionEffectToRemove.getType().translationKey())),
                              Placeholder.component("amplifier1",
                                  Component.translatable(
                                      "enchantment.level." + (potionEffectToRemove.getAmplifier() + 1))));
                        } else if (potionEffectType != null) {
                          sender.sendRichMessage(
                              "<red>[EggSplosion]</red> No effect <gray><effect></gray> on map <aqua><map></aqua>",
                              Placeholder.component("map", Component.text(args[1])),
                              Placeholder.component("effect",
                                  Component.translatable(potionEffectType.translationKey())));
                        } else {
                          sender.sendRichMessage(
                              "<red>[EggSplosion]</red> Unknown effect <gray><effect></gray> on map <aqua><map></aqua>",
                              Placeholder.component("map", Component.text(args[1])),
                              Placeholder.component("effect",
                                  Component.text(potionEffectNameFull)));
                        }
                      }
                      return true;
                    }
                }
              }
            }
            break;
          case "icon": {
            GameMap map = gameMaps.get(args[1]);
            if (map != null) {
              if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.getInventory().getItemInMainHand() != null) {
                  map.setMapIcon(player.getInventory().getItemInMainHand().getType());
                  player.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                      + " Icon Set to: " + ChatColor.DARK_GRAY
                      + player.getInventory().getItemInMainHand().getType().name().toLowerCase());
                } else {
                  player.sendMessage("[EggSplosion] Hold an item in your hand to set it as the map icon");
                }
                return true;
              }
            }
            break;
          }
          case "capturepoint":
            GameMap map = gameMaps.get(args[1]);
            if (map != null) {
              if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length >= 4) {
                  switch (args[2]) {
                    case "add":
                      Location playerLocation = player.getLocation();
                      if (!map.locationInMap(playerLocation)) {
                        player.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                            + " point not inside map, move into map to add capture point");
                        return true;
                      }
                      map.addCapturePoint(args[3], playerLocation);
                      player.sendMessage(
                          "[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET + " Added Capture Point "
                              + ChatColor.GREEN + args[3] + ChatColor.RESET + " (" + playerLocation.getBlockX() + ", "
                              + playerLocation.getBlockY() + ", " + playerLocation.getBlockZ() + ")");
                      break;
                    case "remove":
                      Location capturePointLocation = map.getCapturePoint(args[3]);
                      map.removeCapturePoint(args[3]);
                      player.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                          + " Removed Capture Point " + ChatColor.GREEN + args[3] + ChatColor.RESET + " ("
                          + capturePointLocation.getBlockX() + ", " + capturePointLocation.getBlockY() + ", "
                          + capturePointLocation.getBlockZ() + ")");
                      break;
                  }
                } else if (args.length == 3) {
                  sender.sendMessage(
                      "[EggSplosion] Capture Point Name Unspecified /map capturepoint <mapName> <add|remove> <capturePointName>");
                } else if (args.length == 2) {
                  Iterator<String> capturePointNames = map.getAllCapturePointName().iterator();
                  String formattedString = "";
                  while (capturePointNames.hasNext()) {
                    String capturePointName = capturePointNames.next();
                    formattedString += ChatColor.GREEN + capturePointName + ChatColor.RESET;
                    if (capturePointNames.hasNext()) {
                      formattedString += ", ";
                    }
                  }

                  if (formattedString != "") {
                    sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                        + " Capture Points: " + formattedString);
                  } else {
                    sender.sendMessage("[EggSplosion] Map " + ChatColor.AQUA + args[1] + ChatColor.RESET
                        + " has no capture points! Create one with /map capturepoint add <capturePointName>");
                  }
                }
                return true;
              }
            }
        }
      }
    }
    return false;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
    if (cmd.getName().equalsIgnoreCase("map")) {
      switch (args.length) {
        case 1:
          List<String> commands = new ArrayList<>();
          commands.add("create");
          commands.add("tools");
          commands.add("equip");
          commands.add("gamerule");
          commands.add("effect");
          commands.add("loadout");
          commands.add("icon");
          commands.add("capturepoint");
          commands.add("locate");
          return Utils.FilterTabComplete(args[0], commands);
        case 2:
          switch (args[0]) {
            case "equip":
            case "gamerule":
            case "tools":
            case "effect":
            case "loadout":
            case "icon":
            case "capturepoint":
            case "locate":
              List<String> mapNames = new ArrayList<>();
              Iterator<String> mapNamesIterator = gameMaps.keySet().iterator();
              while (mapNamesIterator.hasNext()) {
                mapNames.add(mapNamesIterator.next());
              }
              return Utils.FilterTabComplete(args[1], mapNames);
            case "create":
            default:
              return new ArrayList<>();
          }
        case 3:
          switch (args[0]) {
            case "gamerule":
              List<String> gameRules = new ArrayList<>();
              gameRules.add("doSideSwitch");
              gameRules.add("doFlagMessages");
              gameRules.add("pointsToWinCTF");
              gameRules.add("pointsToWinTDM");
              gameRules.add("pointsToWinDM");
              gameRules.add("allowItemPickup");
              gameRules.add("allowItemDrop");
              gameRules.add("allowHelmetRemoval");
              gameRules.add("allowChestplateRemoval");
              gameRules.add("allowLeggingRemoval");
              gameRules.add("allowBootRemoval");
              gameRules.add("resetInventoryOnSpawn");
              gameRules.add("flagSpawnDelay");
              return Utils.FilterTabComplete(args[2], gameRules);
            case "effect":
            case "capturepoint":
              List<String> effectCommands = new ArrayList<>();
              effectCommands.add("add");
              effectCommands.add("remove");
              return Utils.FilterTabComplete(args[2], effectCommands);
            default:
              return new ArrayList<>();
          }
        case 4:
          switch (args[0]) {
            case "gamerule":
              switch (args[2]) {
                case "doSideSwitch":
                case "doFlagMessages":
                case "allowItemPickup":
                case "allowItemDrop":
                case "allowHelmetRemoval":
                case "allowChestplateRemoval":
                case "allowLeggingRemoval":
                case "allowBootRemoval":
                case "resetInventoryOnSpawn":
                  List<String> trueFalse = new ArrayList<>();
                  trueFalse.add("true");
                  trueFalse.add("false");
                  return Utils.FilterTabComplete(args[3], trueFalse);
                case "pointsToWinCTF":
                case "pointsToWinTDM":
                case "pointsToWinDM":
                case "flagSpawnDelay":
                  return new ArrayList<>();
              }
            case "effect":
              switch (args[2]) {
                case "add":
                case "remove":
                  List<String> potionNames = new ArrayList<>();
                  Registry.MOB_EFFECT.forEach((effect) -> {
                    potionNames.add(effect.getKey().asString());
                  });
                  return Utils.FilterTabComplete(args[3], potionNames);
              }
            default:
              return new ArrayList<>();
          }
        case 5:
          return new ArrayList<>();
      }
    }
    return null;
  }

  public static LiteralCommandNode<CommandSourceStack> createMapCommands() {
    LiteralCommandNode<CommandSourceStack> locateCommand = Commands.literal("locate")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .then(Commands.argument("map_name", new MapNameArgument())
            .executes(MapManager::executeMapLocate))
        .build();

    LiteralCommandNode<CommandSourceStack> iconCommand = Commands.literal("icon")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .then(Commands.argument("icon", ArgumentTypes.resource(RegistryKey.ITEM))
            .executes(MapManager::executeMapIcon))
        .build();

    LiteralCommandNode<CommandSourceStack> equipmentCommand = Commands.literal("equipment")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .executes(MapManager::executeMapEquipment)
        .build();

    LiteralCommandNode<CommandSourceStack> loadoutCommand = Commands.literal("loadout")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .executes(MapManager::executeMapLoadout)
        .build();

    LiteralCommandNode<CommandSourceStack> mapLocationToolsCommand = Commands.literal("tools")
        .executes(MapManager::executeMapLocationTools).build();

    LiteralCommandNode<CommandSourceStack> effectAddCommand = Commands.literal("add")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .then(Commands.argument("effect_type", ArgumentTypes.resource(RegistryKey.MOB_EFFECT))
            .then(Commands.argument("effect_amplifier", IntegerArgumentType.integer(0, 100))
                .executes(MapManager::executeAddMapEffect)))
        .build();

    LiteralCommandNode<CommandSourceStack> effectRemoveCommand = Commands.literal("remove")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .then(Commands.argument("effect_type", ArgumentTypes.resource(RegistryKey.MOB_EFFECT))
            .executes(MapManager::executeRemoveMapEffect))
        .build();

    LiteralCommandNode<CommandSourceStack> effectListCommand = Commands.literal("list")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .executes(MapManager::executeListMapEffect)
        .build();

    LiteralCommandNode<CommandSourceStack> effectCommand = Commands.literal("effects")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .executes(MapManager::executeListMapEffect)
        .build();
    effectCommand.addChild(effectAddCommand);
    effectCommand.addChild(effectRemoveCommand);
    effectCommand.addChild(effectListCommand);

    LiteralCommandNode<CommandSourceStack> capturePointListCommand = Commands.literal("list")
        .executes(MapManager::executeMapCapturepointList)
        .build();

    LiteralCommandNode<CommandSourceStack> capturePointAddCommand = Commands.literal("add")
        .then(Commands.argument("capture_point_name", StringArgumentType.word())
            .executes(MapManager::executeMapCapturepointAdd))
        .build();

    LiteralCommandNode<CommandSourceStack> capturePointRemoveCommand = Commands.literal("remove")
        .then(Commands.argument("capture_point_name", new CapturePointNameArgument())
            .executes(MapManager::executeMapCapturepointRemove))
        .build();

    LiteralCommandNode<CommandSourceStack> capturePointCommand = Commands.literal("capture_point")
        .requires(ctx -> (ctx.getExecutor() instanceof Player))
        .executes(MapManager::executeMapCapturepointList)
        .build();
    capturePointCommand.addChild(capturePointListCommand);
    capturePointCommand.addChild(capturePointAddCommand);
    capturePointCommand.addChild(capturePointRemoveCommand);

    LiteralCommandNode<CommandSourceStack> mapCommands = Commands.literal("map")
        .executes(MapManager::executeListMaps)
        .then(Commands.literal("list")
            .executes(MapManager::executeListMaps))
        .then(locateCommand)
        .then(Commands.literal("edit")
            .then(Commands.argument("map_name", new MapNameArgument())
                .then(effectCommand)
                .then(iconCommand)
                .then(equipmentCommand)
                .then(loadoutCommand)
                .then(capturePointCommand)
                .then(mapLocationToolsCommand)))
        .then(Commands.literal("create")
            .requires((ctx) -> (ctx.getExecutor() instanceof Player))
            .executes(MapManager::executeMapCreationTools)
            .then(Commands.argument("map_name", StringArgumentType.word())
                .requires((ctx) -> (ctx.getExecutor() instanceof Player player
                    && (EggSplosion.getInstance().getMapManager().boundaryToolTracker.get(player) != null
                        && EggSplosion.getInstance().getMapManager().boundaryToolTracker.get(player).size() == 2)))
                .executes(MapManager::executeMapCreate)))
        .then(Commands.literal("remove")
            .then(Commands.argument("map_name", new MapNameArgument())
                // TODO: Build this executor
                .executes(MapManager::executeListMaps)))
        .build();

    return mapCommands;
  }

  private static int executeAddMapEffect(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    final CommandSender sender = ctx.getSource().getSender();

    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);
    final PotionEffectType potionEffectType = ctx.getArgument("effect_type", PotionEffectType.class);
    final int amplifier = IntegerArgumentType.getInteger(ctx, "effect_amplifier");

    GameMap map = mapManager.getMapByName(mapName);

    PotionEffect matchingPotionEffect = null;
    for (PotionEffect potionEffect : map.getMapEffects()) {
      if (potionEffect.getType().equals(potionEffectType)) {
        matchingPotionEffect = potionEffect;
      }
    }

    if (matchingPotionEffect == null) {
      map.addMapEffect(potionEffectType, amplifier);
      sender.sendRichMessage(
          "[EggSplosion] Map <aqua><map></aqua> effect <gray><effect> <amplifier></gray> <green>added</green>",
          Placeholder.component("map", Component.text(mapName)),
          Placeholder.component("effect",
              Component.translatable(potionEffectType.translationKey())),
          Placeholder.component("amplifier",
              amplifier <= 10
                  ? Component.translatable("enchantment.level." + (amplifier))
                  : Component.text(amplifier)));
    } else {
      map.removeMapEffect(matchingPotionEffect);
      map.addMapEffect(matchingPotionEffect.withAmplifier(amplifier - 1));
      sender.sendRichMessage(
          "[EggSplosion] Map <aqua><map></aqua> effect <gray><effect> <amplifier1></gray> updated to <gray><effect> <amplifier2></gray>",
          Placeholder.component("map", Component.text(mapName)),
          Placeholder.component("effect",
              Component.translatable(matchingPotionEffect.getType().translationKey())),
          Placeholder.component("amplifier1",
              matchingPotionEffect.getAmplifier() < 10 ? Component
                  .translatable("enchantment.level." + (matchingPotionEffect.getAmplifier() + 1))
                  : Component.text(matchingPotionEffect.getAmplifier() + 1)),
          Placeholder.component("amplifier2",
              amplifier <= 10
                  ? Component.translatable("enchantment.level." + (amplifier))
                  : Component.text(amplifier)));
    }

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeRemoveMapEffect(final CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    final CommandSender sender = ctx.getSource().getSender();

    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);
    final PotionEffectType potionEffectType = ctx.getArgument("effect_type", PotionEffectType.class);

    GameMap map = mapManager.getMapByName(mapName);

    PotionEffect potionEffectToRemove = null;
    for (PotionEffect potionEffect : map.getMapEffects()) {
      if (potionEffect.getType().equals(potionEffectType)) {
        potionEffectToRemove = potionEffect;
      }
    }

    if (potionEffectToRemove != null) {
      map.removeMapEffect(potionEffectToRemove);
      sender.sendRichMessage(
          "[EggSplosion] Map <aqua><map></aqua> effect <gray><effect> <amplifier></gray> <red>removed</red>",
          Placeholder.component("map", Component.text(mapName)),
          Placeholder.component("effect",
              Component.translatable(potionEffectToRemove.getType().translationKey())),
          Placeholder.component("amplifier",
              potionEffectToRemove.getAmplifier() < 10
                  ? Component.translatable("enchantment.level." + (potionEffectToRemove.getAmplifier() + 1))
                  : Component.text(potionEffectToRemove.getAmplifier() + 1)));
    } else {
      sender.sendRichMessage(
          "<red>[EggSplosion]</red> No effect <gray><effect></gray> on map <aqua><map></aqua>",
          Placeholder.component("map", Component.text(mapName)),
          Placeholder.component("effect",
              Component.translatable(potionEffectType.translationKey())));
    }

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  // TODO: Handle No Maps Edgecase
  private static int executeListMaps(final CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    final CommandSender sender = ctx.getSource().getSender();

    MapManager mapManager = EggSplosion.getInstance().getMapManager();

    Component message = MiniMessage.miniMessage().deserialize("[EggSplosion] Current Maps - ");
    Iterator<String> mapNameIterator = mapManager.getMaps().keySet().iterator();
    while (mapNameIterator.hasNext()) {
      String mapName = mapNameIterator.next();
      message = message.appendNewline().appendSpace().appendSpace()
          .append(MiniMessage.miniMessage().deserialize("<gray><map_name></gray>",
              Placeholder.component("map_name", Component.text(mapName))));
      if (mapNameIterator.hasNext()) {
        message = message.append(Component.text(", "));
      }
    }

    sender.sendMessage(message);
    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  // TODO: Handle No Effects Edgecase
  private static int executeListMapEffect(final CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    final CommandSender sender = ctx.getSource().getSender();

    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);

    GameMap map = mapManager.getMapByName(mapName);

    Component message = MiniMessage.miniMessage().deserialize(
        "[EggSplosion] Current Map <aqua><map_name></aqua> Effects - ",
        Placeholder.component("map_name", Component.text(mapName)));
    Iterator<PotionEffect> mapEffectIterator = map.getMapEffects().iterator();
    while (mapEffectIterator.hasNext()) {
      PotionEffect nextPotionEffect = mapEffectIterator.next();
      message = message.appendNewline().appendSpace().appendSpace()
          .append(MiniMessage.miniMessage().deserialize("<gray><effect_name> <amplifier></gray>",
              Placeholder.component("effect_name", Component.translatable(nextPotionEffect.getType().translationKey())),
              Placeholder.component("amplifier",
                  nextPotionEffect.getAmplifier() < 10
                      ? Component.translatable("enchantment.level." + (nextPotionEffect.getAmplifier() + 1))
                      : Component.text(nextPotionEffect.getAmplifier() + 1))));
      if (mapEffectIterator.hasNext()) {
        message = message.append(Component.text(", "));
      }
    }

    sender.sendMessage(message);
    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapIcon(final CommandContext<CommandSourceStack> ctx) {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = StringArgumentType.getString(ctx, "map_name");
    ItemType icon = ctx.getArgument("icon", ItemType.class);

    GameMap map = mapManager.getMapByName(mapName);

    map.setMapIcon(icon.createItemStack().getType());
    player.sendMessage(
        MiniMessage.miniMessage().deserialize("[EggSplosion] Map <aqua><map_name></aqua> Icon Set to: <item_name>",
            Placeholder.component("map_name", Component.text(mapName)),
            Placeholder.component("item_name",
                Component.translatable(icon.translationKey()))));
    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapCreate(final CommandContext<CommandSourceStack> ctx) {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
    final String mapName = StringArgumentType.getString(ctx, "map_name");

    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    if (mapManager.boundaryToolTracker.get(player) != null && mapManager.boundaryToolTracker.get(player).size() == 2) {
      player.sendMessage("Map " + ChatColor.GRAY + mapName + ChatColor.RESET + " created");
      GameMap map = new GameMap(mapManager.boundaryToolTracker.get(player).get(0),
          mapManager.boundaryToolTracker.get(player).get(1));
      mapManager.gameMaps.put(mapName, map);
      mapManager.boundaryToolTracker.remove(player);
      player.updateCommands();

      player.performCommand("map edit " + mapName + " tools ");
    }

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapCreationTools(final CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    MapManager mapManager = EggSplosion.getInstance().getMapManager();

    player.getInventory().clear();

    mapManager.Map_Tool_boundary = mapManager.createMapTool(Material.WOODEN_SHOVEL, "Boundary Tool");
    player.getInventory().addItem(mapManager.Map_Tool_boundary);

    player.sendMessage(
        "[EggSplosion] Select the boudarys of the map by right clicking the shovel, then run the command /map create <mapName>");

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapLocationTools(final CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);

    player.getInventory().clear();

    List<String> lore = new ArrayList<>();
    lore.add("Map: " + mapName);

    ItemStack Map_Tool_spawnPointsSolo = mapManager.createMapTool(Material.IRON_AXE, "Spawn Point Tool - Solo", lore);
    player.getInventory().addItem(Map_Tool_spawnPointsSolo);

    ItemStack Map_Tool_spawnPointsTeamA = mapManager.createMapTool(Material.GOLDEN_AXE, "Spawn Point Tool - Team A",
        lore);
    ItemStack Map_Tool_spawnPointsTeamB = mapManager.createMapTool(Material.DIAMOND_AXE, "Spawn Point Tool - Team B",
        lore);

    ItemStack Map_Tool_flagSpawnTeamA = mapManager.createMapTool(Material.GOLDEN_SHOVEL, "Flag Spawn - Team A", lore);
    ItemStack Map_Tool_flagSpawnTeamB = mapManager.createMapTool(Material.DIAMOND_SHOVEL, "Flag Spawn - Team B", lore);

    player.getInventory().setItem(2, Map_Tool_spawnPointsTeamA);
    player.getInventory().setItem(3, Map_Tool_spawnPointsTeamB);

    player.getInventory().setItem(5, Map_Tool_flagSpawnTeamA);
    player.getInventory().setItem(6, Map_Tool_flagSpawnTeamB);

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapCapturepointRemove(final CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);
    final String capturePointName = StringArgumentType.getString(ctx, "capture_point_name");

    GameMap map = mapManager.getMapByName(mapName);

    Location capturePointLocation = map.getCapturePoint(capturePointName);
    map.removeCapturePoint(capturePointName);
    player.sendMessage(MiniMessage.miniMessage().deserialize(
        "[EggSplosion] Map <aqua><map_name></aqua> <red>Removed</red> Capture Point <blue><capture_point_name></blue> (<x>, <y>, <z>)",
        Placeholder.component("map_name", Component.text(mapName)),
        Placeholder.component("capture_point_name", Component.text(capturePointName)),
        Placeholder.component("x", Component.text(player.getLocation().getBlockX())),
        Placeholder.component("y", Component.text(player.getLocation().getBlockY())),
        Placeholder.component("z", Component.text(player.getLocation().getBlockZ()))));

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapCapturepointAdd(final CommandContext<CommandSourceStack> ctx)
      throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);
    final String capturePointName = ctx.getArgument("capture_point_name", String.class);

    GameMap map = mapManager.getMapByName(mapName);

    Location playerLocation = player.getLocation();
    if (!map.locationInMap(playerLocation)) {
      player.sendMessage(MiniMessage.miniMessage().deserialize(
          "[EggSplosion] Map <aqua><map_name></aqua> point not inside map, move into the map to add a capture point",
          Placeholder.component("map_name", Component.text(mapName))));
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    map.addCapturePoint(capturePointName, playerLocation);
    player.sendMessage(MiniMessage.miniMessage().deserialize(
        "[EggSplosion] Map <aqua><map_name></aqua> <green>Added</green> Capture Point <blue><capture_point_name></blue> (<x>, <y>, <z>)",
        Placeholder.component("map_name", Component.text(mapName)),
        Placeholder.component("capture_point_name", Component.text(capturePointName)),
        Placeholder.component("x", Component.text(player.getLocation().getBlockX())),
        Placeholder.component("y", Component.text(player.getLocation().getBlockY())),
        Placeholder.component("z", Component.text(player.getLocation().getBlockZ()))));

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapCapturepointList(final CommandContext<CommandSourceStack> ctx) {
    Entity sender = ctx.getSource().getExecutor();

    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);

    GameMap map = mapManager.getMapByName(mapName);

    // Iterator<String> capturePointNames = map.getAllCapturePointName().iterator();
    // String formattedString = "";
    // while (capturePointNames.hasNext()) {
    // String capturePointName = capturePointNames.next();
    // formattedString += ChatColor.GREEN + capturePointName + ChatColor.RESET;
    // if (capturePointNames.hasNext()) {
    // formattedString += ", ";
    // }
    // }

    if (map.getAllCapturePointName().size() > 0) {
      Component message = MiniMessage.miniMessage().deserialize("[EggSplosion] Current Capture Points - ");
      Iterator<String> capturePointsIterator = map.getAllCapturePointName().iterator();
      while (capturePointsIterator.hasNext()) {
        String capturePointName = capturePointsIterator.next();
        Location capturePointLocation = map.getCapturePoint(capturePointName);
        // TODO: Improve this??? properly do this?
        message = message.appendNewline().appendSpace().appendSpace()
            .append(MiniMessage.miniMessage().deserialize(
                "<blue><capture_point_name></blue> (<x>, <y>, <z>) - <red><click:suggest_command:\"/map edit " + mapName
                    + " capture_point remove "
                    + (new NamespacedKey(mapName.toLowerCase(), capturePointName.toLowerCase())).asString()
                    + "\">Click to Remove</click></red>",
                Placeholder.component("capture_point_name", Component.text(capturePointName)),
                Placeholder.component("x", Component.text(capturePointLocation.getBlockX())),
                Placeholder.component("y", Component.text(capturePointLocation.getBlockY())),
                Placeholder.component("z", Component.text(capturePointLocation.getBlockZ()))));
      }

      sender.sendMessage(message);
    } else {
      // TODO: Improve this??? properly do this?
      sender.sendMessage(MiniMessage.miniMessage().deserialize(
          "[EggSplosion] Map <aqua><map_name></aqua> has no capture points! Create one with <click:suggest_command:/map edit "
              + mapName + " capture_point add>/map edit <map_name> capture_point add <capturePointName></click>",
          Placeholder.component("map_name", Component.text(mapName))));
    }

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapLoadout(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);

    GameMap map = mapManager.getMapByName(mapName);

    Inventory mapLoadoutMenu = map.getLoadout();
    player.openInventory(mapLoadoutMenu);

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapEquipment(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);

    Inventory mapEquipmentMenu;
    if (mapManager.gameMapEquipment.get(mapName) != null) {
      mapEquipmentMenu = mapManager.gameMapEquipment.get(mapName);
    } else {
      mapEquipmentMenu = Bukkit.createInventory(null, InventoryType.HOPPER, "Player Equipment Menu");

      mapManager.disabledSlot = mapManager.createMapTool(Material.RED_STAINED_GLASS_PANE, "Disabled");
      mapEquipmentMenu.setItem(4, mapManager.disabledSlot);
      mapManager.gameMapEquipment.put(mapName, mapEquipmentMenu);
    }

    player.openInventory(mapEquipmentMenu);
    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }

  private static int executeMapLocate(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    if (!(ctx.getSource().getExecutor() instanceof Player player)) {
      return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    MapManager mapManager = EggSplosion.getInstance().getMapManager();
    final String mapName = ctx.getArgument("map_name", String.class);

    Location to_teleport = mapManager.gameMaps.get(mapName).getSpawnPoint();
    if (to_teleport != null) {
      player.teleport(to_teleport);
    } else {
      to_teleport = mapManager.gameMaps.get(mapName).getCornerA();
      player.teleport(to_teleport);
    }

    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
  }
}
