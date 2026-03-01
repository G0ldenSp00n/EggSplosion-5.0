package com.g0ldensp00n.eggsplosion.handlers.MapManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameMode;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.Arguments.BooleanGameruleKeyArgument;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.Arguments.IntegerGameruleKeyArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

public class GameMap {
  private Location cornerA;
  private Location cornerB;
  private List<Location> soloSpawnLocations;
  private Map<Integer, List<Location>> sideSpawnLocations;
  private Map<Integer, Location> sideFlagLocation;
  private Map<String, Location> capturePointLocations;
  private Map<Team, Integer> teamSide;
  private List<PotionEffect> mapEffects;
  private Inventory playerLoadout;
  private ItemStack helmet;
  private ItemStack chestplate;
  private ItemStack leggings;
  private ItemStack boots;
  private Material icon;

  public static enum BooleanGameRules {
    DO_SIDE_SWITCH,
    DO_FLAG_MESSAGES,
    DO_RESET_INVENTORY_ON_SPAWN,
    ALLOW_ITEM_PICKUP,
    ALLOW_ITEM_DROP,
    ALLOW_HELMET_REMOVAL,
    ALLOW_CHESTPLATE_REMOVAL,
    ALLOW_LEGGING_REMOVAL,
    ALLOW_BOOT_REMOVAL;

    public String asString() {
      switch (this) {
        case DO_SIDE_SWITCH:
          return "doSideSwitch";
        case DO_FLAG_MESSAGES:
          return "doFlagMessages";
        case DO_RESET_INVENTORY_ON_SPAWN:
          return "doResetInventoryOnSpawn";
        case ALLOW_ITEM_PICKUP:
          return "allowItemPickup";
        case ALLOW_ITEM_DROP:
          return "allowItemDrop";
        case ALLOW_HELMET_REMOVAL:
          return "allowHelmetRemoval";
        case ALLOW_CHESTPLATE_REMOVAL:
          return "allowChestplateRemoval";
        case ALLOW_LEGGING_REMOVAL:
          return "allowLeggingRemoval";
        case ALLOW_BOOT_REMOVAL:
          return "allowBootRemoval";
        default:
          return "INVALID";
      }
    }

    public static BooleanGameRules fromString(String value) throws CommandSyntaxException {
      switch (value) {
        case "doSideSwitch":
          return BooleanGameRules.DO_SIDE_SWITCH;
        case "doFlagMessages":
          return BooleanGameRules.DO_FLAG_MESSAGES;
        case "doResetInventoryOnSpawn":
          return BooleanGameRules.DO_RESET_INVENTORY_ON_SPAWN;
        case "allowItemPickup":
          return BooleanGameRules.ALLOW_ITEM_PICKUP;
        case "allowItemDrop":
          return BooleanGameRules.ALLOW_ITEM_DROP;
        case "allowHelmetRemoval":
          return BooleanGameRules.ALLOW_HELMET_REMOVAL;
        case "allowChestplateRemoval":
          return BooleanGameRules.ALLOW_CHESTPLATE_REMOVAL;
        case "allowLeggingRemoval":
          return BooleanGameRules.ALLOW_LEGGING_REMOVAL;
        case "allowBootRemoval":
          return BooleanGameRules.ALLOW_BOOT_REMOVAL;
        default:
          throw BooleanGameruleKeyArgument.ERROR_INVALID_GAMERULE_KEY.create(value);
      }
    }

    public static void setDefaultValues(Map<BooleanGameRules, Boolean> map) {
      map.put(BooleanGameRules.DO_FLAG_MESSAGES, true);
      map.put(BooleanGameRules.DO_SIDE_SWITCH, false);
      map.put(BooleanGameRules.ALLOW_ITEM_PICKUP, false);
      map.put(BooleanGameRules.ALLOW_ITEM_DROP, false);
      map.put(BooleanGameRules.ALLOW_HELMET_REMOVAL, false);
      map.put(BooleanGameRules.ALLOW_CHESTPLATE_REMOVAL, false);
      map.put(BooleanGameRules.ALLOW_LEGGING_REMOVAL, false);
      map.put(BooleanGameRules.ALLOW_BOOT_REMOVAL, false);
      map.put(BooleanGameRules.DO_RESET_INVENTORY_ON_SPAWN, false);
    }
  };

  public static enum IntegerGameRules {
    FLAG_SPAWN_DELAY,
    POINTS_TO_WIN_CTF,
    POINTS_TO_WIN_TDM,
    POINTS_TO_WIN_DM;

    public String asString() {
      switch (this) {
        case FLAG_SPAWN_DELAY:
          return "flagSpawnDelay";
        case POINTS_TO_WIN_CTF:
          return "pointsToWinCTF";
        case POINTS_TO_WIN_TDM:
          return "pointsToWinTDM";
        case POINTS_TO_WIN_DM:
          return "pointsToWinDM";
        default:
          return "INVALID";
      }
    }

    public static IntegerGameRules fromString(String value) throws CommandSyntaxException {
      switch (value) {
        case "flagSpawnDelay":
          return IntegerGameRules.FLAG_SPAWN_DELAY;
        case "pointsToWinCTF":
          return IntegerGameRules.POINTS_TO_WIN_CTF;
        case "pointsToWinTDM":
          return IntegerGameRules.POINTS_TO_WIN_TDM;
        case "pointsToWinDM":
          return IntegerGameRules.POINTS_TO_WIN_DM;
        default:
          throw IntegerGameruleKeyArgument.ERROR_INVALID_GAMERULE_KEY.create(value);
      }
    }

    public static void setDefaultValues(Map<IntegerGameRules, Integer> map) {
      map.put(IntegerGameRules.FLAG_SPAWN_DELAY, 0);
      map.put(IntegerGameRules.POINTS_TO_WIN_CTF, 4);
      map.put(IntegerGameRules.POINTS_TO_WIN_TDM, 15);
      map.put(IntegerGameRules.POINTS_TO_WIN_DM, 15);
    }
  }

  private Map<BooleanGameRules, Boolean> booleanGameRules;
  private Map<IntegerGameRules, Integer> integerGameRules;

  public GameMap(Location cornerA, Location cornerB) {
    this.cornerA = cornerA;
    this.cornerB = cornerB;

    teamSide = new Hashtable<Team, Integer>();
    playerLoadout = Bukkit.createInventory(null, InventoryType.PLAYER, "Map Loadout");

    soloSpawnLocations = new ArrayList<Location>();
    sideSpawnLocations = new Hashtable<Integer, List<Location>>();
    sideSpawnLocations.put(0, new ArrayList<Location>());
    sideSpawnLocations.put(1, new ArrayList<Location>());
    sideFlagLocation = new Hashtable<Integer, Location>();
    capturePointLocations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    mapEffects = new ArrayList<PotionEffect>();

    booleanGameRules = new Hashtable<>();
    BooleanGameRules.setDefaultValues(booleanGameRules);
    integerGameRules = new Hashtable<>();
    IntegerGameRules.setDefaultValues(integerGameRules);
  }

  public String mapSupportsGameMode(GameMode mode) {
    if (mode == GameMode.DEATH_MATCH) {
      if (this.soloSpawnLocations.size() == 0) {
        return "Selected Map has no Solo Spawns";
      }
    } else if (mode == GameMode.CAPTURE_POINT) {
      if (this.capturePointLocations.size() == 0) {
        return "Selected Map has no Capture Points";
      }
    } else {
      if (this.sideSpawnLocations.get(0).size() == 0 || this.sideSpawnLocations.get(0).size() == 0) {
        return "Selected Map has no Team Spawns";
      }
    }

    if (mode == GameMode.CAPTURE_THE_FLAG) {
      if (this.sideFlagLocation.get(0) == null || this.sideSpawnLocations.get(1) == null) {
        return "Selected Map has no Flag Spawns";
      }
    }

    return null;
  }

  public Material getMapIcon() {
    return icon;
  }

  public void setMapIcon(Material icon) {
    this.icon = icon;
  }

  public Location getCornerA() {
    return cornerA;
  }

  public Location getCornerB() {
    return cornerB;
  }

  public List<PotionEffect> getMapEffects() {
    return mapEffects;
  }

  public void addMapEffect(PotionEffect effect) {
    mapEffects.add(effect);
  }

  public Inventory getLoadout() {
    return playerLoadout;
  }

  public void addCapturePoint(String pointName, Location location) {
    capturePointLocations.put(pointName, location);
  }

  public void removeCapturePoint(String pointName) {
    capturePointLocations.remove(pointName);
  }

  public List<String> getAllCapturePointName() {
    List<String> capturePointNames = new ArrayList<>();
    for (String capturePointName : capturePointLocations.keySet()) {
      capturePointNames.add(capturePointName);
    }

    return capturePointNames;
  }

  public Location getCapturePoint(String pointName) {
    if (capturePointLocations.get(pointName) != null) {
      return capturePointLocations.get(pointName).clone();
    }
    return null;
  }

  public void setLoadoutContents(ItemStack[] playerLoadoutContents) {
    this.playerLoadout.setContents(playerLoadoutContents);
  }

  public void addMapEffect(PotionEffectType potionType, int amplifier) {
    if (potionType != null) {
      PotionEffect effect = new PotionEffect(potionType, 20 * 9, amplifier - 1, true);
      mapEffects.add(effect);
    }
  }

  public void removeMapEffect(PotionEffect effect) {
    mapEffects.remove(effect);
  }

  // Game Rules
  public Boolean getBooleanGamerule(BooleanGameRules booleanGameRuleKey) {
    return this.booleanGameRules.get(booleanGameRuleKey);
  }

  public void setBooleanGamerule(BooleanGameRules booleanGameRuleKey, boolean value) {
    this.booleanGameRules.put(booleanGameRuleKey, value);
  }

  public Boolean getDoSideSwitch() {
    return this.booleanGameRules.get(BooleanGameRules.DO_SIDE_SWITCH);
  }

  public void setDoSideSwitch(Boolean doSideSwitch) {
    this.booleanGameRules.put(BooleanGameRules.DO_SIDE_SWITCH, doSideSwitch);
  }

  public Boolean getDoFlagMessages() {
    return this.booleanGameRules.get(BooleanGameRules.DO_FLAG_MESSAGES);
  }

  public void setDoFlagMessages(Boolean doFlagMessages) {
    this.booleanGameRules.put(BooleanGameRules.DO_FLAG_MESSAGES, doFlagMessages);
  }

  public Boolean getAllowItemPickup() {
    return this.booleanGameRules.get(BooleanGameRules.ALLOW_ITEM_PICKUP);
  }

  public void setAllowItemPickup(Boolean allowItemPickup) {
    this.booleanGameRules.put(BooleanGameRules.ALLOW_ITEM_PICKUP, allowItemPickup);
  }

  public Boolean getAllowItemDrop() {
    return this.booleanGameRules.get(BooleanGameRules.ALLOW_ITEM_DROP);
  }

  public void setAllowItemDrop(Boolean allowItemDrop) {
    this.booleanGameRules.put(BooleanGameRules.ALLOW_ITEM_DROP, allowItemDrop);
  }

  public Boolean getAllowHelmetRemoval() {
    return this.booleanGameRules.get(BooleanGameRules.ALLOW_HELMET_REMOVAL);
  }

  public void setAllowHelmetRemoval(Boolean allowHelmetRemoval) {
    this.booleanGameRules.put(BooleanGameRules.ALLOW_HELMET_REMOVAL, allowHelmetRemoval);
  }

  public Boolean getAllowChestplateRemoval() {
    return this.booleanGameRules.get(BooleanGameRules.ALLOW_CHESTPLATE_REMOVAL);
  }

  public void setAllowChestplateRemoval(Boolean allowChestplateRemoval) {
    this.booleanGameRules.put(BooleanGameRules.ALLOW_CHESTPLATE_REMOVAL, allowChestplateRemoval);
  }

  public Boolean getAllowLeggingRemoval() {
    return this.booleanGameRules.get(BooleanGameRules.ALLOW_LEGGING_REMOVAL);
  }

  public void setAllowLeggingRemoval(Boolean allowLeggingRemoval) {
    this.booleanGameRules.put(BooleanGameRules.ALLOW_LEGGING_REMOVAL, allowLeggingRemoval);
  }

  public Boolean getAllowBootRemoval() {
    return this.booleanGameRules.get(BooleanGameRules.ALLOW_BOOT_REMOVAL);
  }

  public void setAllowBootRemoval(Boolean allowBootRemoval) {
    this.booleanGameRules.put(BooleanGameRules.ALLOW_BOOT_REMOVAL, allowBootRemoval);
  }

  public Boolean getResetInventoryOnSpawn() {
    return this.booleanGameRules.get(BooleanGameRules.DO_RESET_INVENTORY_ON_SPAWN);
  }

  public void setResetInventoryOnSpawn(Boolean resetInventoryOnSpawn) {
    this.booleanGameRules.put(BooleanGameRules.DO_RESET_INVENTORY_ON_SPAWN, resetInventoryOnSpawn);
  }

  public Integer getIntegerGamerule(IntegerGameRules integerGameRuleKey) {
    return this.integerGameRules.get(integerGameRuleKey);
  }

  public void setIntegerGamerule(IntegerGameRules integerGameRuleKey, Integer value) {
    this.integerGameRules.put(integerGameRuleKey, value);
  }

  public Integer getFlagSpawnDelay() {
    return this.integerGameRules.get(IntegerGameRules.FLAG_SPAWN_DELAY);
  }

  public void setFlagSpawnDelay(Integer flagSpawnDelay) {
    this.integerGameRules.put(IntegerGameRules.FLAG_SPAWN_DELAY, flagSpawnDelay);
  }

  public Integer getPointsToWinCTF() {
    return this.integerGameRules.get(IntegerGameRules.POINTS_TO_WIN_CTF);
  }

  public void setPointsToWinCTF(Integer pointsToWinCTF) {
    this.integerGameRules.put(IntegerGameRules.POINTS_TO_WIN_CTF, pointsToWinCTF);
  }

  public Integer getPointsToWinTDM() {
    return this.integerGameRules.get(IntegerGameRules.POINTS_TO_WIN_TDM);
  }

  public void setPointsToWinTDM(Integer pointsToWinTDM) {
    this.integerGameRules.put(IntegerGameRules.POINTS_TO_WIN_TDM, pointsToWinTDM);
  }

  public Integer getPointsToWinDM() {
    return this.integerGameRules.get(IntegerGameRules.POINTS_TO_WIN_DM);
  }

  public void setPointsToWinDM(Integer pointsToWinDM) {
    this.integerGameRules.put(IntegerGameRules.POINTS_TO_WIN_DM, pointsToWinDM);
  }

  public ItemStack getHelmet() {
    if (helmet != null) {
      return helmet.clone();
    }
    return null;
  }

  public ItemStack getChestplate() {
    if (chestplate != null) {
      return chestplate.clone();
    }
    return null;
  }

  public ItemStack getLeggings() {
    if (leggings != null) {
      return leggings.clone();
    }
    return null;
  }

  public ItemStack getBoots() {
    if (boots != null) {
      return boots.clone();
    }
    return null;
  }

  public void setArmor(Integer slot, ItemStack item) {
    if (slot == 0) {
      setHelmet(item);
    } else if (slot == 1) {
      setChestplate(item);
    } else if (slot == 2) {
      setLeggings(item);
    } else if (slot == 3) {
      setBoots(item);
    }
  }

  public void setHelmet(ItemStack helmet) {
    this.helmet = helmet;
  }

  public void setChestplate(ItemStack chestplate) {
    this.chestplate = chestplate;
  }

  public void setLeggings(ItemStack leggings) {
    this.leggings = leggings;
  }

  public void setBoots(ItemStack boots) {
    this.boots = boots;
  }

  public List<Location> getSoloSpawnLocations() {
    return soloSpawnLocations;
  }

  public void removeSoloSpawnLocation(int spawnPointIndex) {
    this.soloSpawnLocations.remove(spawnPointIndex);
  }

  public List<Location> getSideSpawnLocations(Integer side) {
    return sideSpawnLocations.get(side);
  }

  public List<Location> getSideASpawnLocations() {
    return getSideSpawnLocations(0);
  }

  public List<Location> getSideBSpawnLocations() {
    return getSideSpawnLocations(1);
  }

  public Team getSideTeam(Integer side) {
    for (Entry<Team, Integer> entry : teamSide.entrySet()) {
      if (entry.getValue() == side) {
        return entry.getKey();
      }
    }
    Bukkit.getLogger().warning("Side " + side + " does not exist in " + teamSide);
    return null;
  }

  public void randomizeTeamSides(List<Team> teams) {
    teamSide = new Hashtable<Team, Integer>();
    if (getDoSideSwitch()) {
      Random random = new Random();
      List<Team> teamsToAdd = new ArrayList<>(teams);
      Integer side = 0;
      while (teamsToAdd.size() > 0) {
        Integer nextTeam = random.nextInt(teamsToAdd.size());
        Team team = teamsToAdd.get(nextTeam);
        teamSide.put(team, side);

        side++;
        teamsToAdd.remove(team);
      }
    } else {
      Integer side = 0;
      for (Team team : teams) {
        teamSide.put(team, side++);
      }
    }
  }

  public void switchTeamSides() {
    Map<Team, Integer> newTeamSide = new Hashtable<Team, Integer>();
    if (this.getDoSideSwitch()) {
      newTeamSide.put(getSideTeam(0), teamSide.size() - 1);
      for (Integer side = 1; side < teamSide.size(); side++) {
        newTeamSide.put(getSideTeam(side), side - 1);
      }
    }

    teamSide = newTeamSide;
  }

  public Integer getTeamSide(Team team) {
    return teamSide.get(team);
  }

  public Location getSideFlagLocation(Integer side) {
    if (sideFlagLocation.get(side) != null) {
      return sideFlagLocation.get(side).clone();
    }
    return null;
  }

  public void spawnFlag(Integer side) {
    Team team = getSideTeam(side);
    String bannerType = team.getColor().name() + "_BANNER";
    Location flagLocation = getSideFlagLocation(side).clone();
    Material flagType = Material.getMaterial(bannerType);

    flagLocation.add(0, 1, 0);
    Block teamFlag = flagLocation.getBlock();
    Location teamFlagBase = flagLocation.clone();
    teamFlagBase.add(0, -1, 0);
    Block teamFlagBaseBlock = teamFlagBase.getBlock();

    teamFlag.setType(Material.AIR);
    teamFlagBaseBlock.setType(Material.OBSIDIAN);
    teamFlag.setType(flagType);
  }

  public void spawnCapturePoints() {
    for (String capturePointName : getAllCapturePointName()) {
      Location beaconLocation = getCapturePoint(capturePointName);
      beaconLocation.getBlock().setType(Material.BEACON);
      for (Integer baseXOffset = 0; baseXOffset < 3; baseXOffset++) {
        for (Integer baseZOffset = 0; baseZOffset < 3; baseZOffset++) {
          Location baseBlockLocation = beaconLocation.clone().add(-1 + baseXOffset, -1, -1 + baseZOffset);
          baseBlockLocation.getBlock().setType(Material.IRON_BLOCK);
        }
      }
    }
  }

  public void clearFlag(Integer side) {
    Location flagLocation = getSideFlagLocation(side).clone();

    flagLocation.add(0, 1, 0);
    Block teamFlag = flagLocation.getBlock();

    teamFlag.setType(Material.AIR);
  }

  public void spawnFlags() {
    for (int side = 0; side < sideFlagLocation.size(); side++) {
      spawnFlag(side);
    }
  }

  public void clearFlags() {
    for (int side = 0; side < sideFlagLocation.size(); side++) {
      clearFlag(side);
    }
  }

  public void respawnFlag(Team team) {
    spawnFlag(getTeamSide(team));
  }

  public void setSideFlagLocation(Integer side, Location location) {
    sideFlagLocation.put(side, location);
  }

  public void setSideAFlagLocation(Location location) {
    setSideFlagLocation(0, location);
  }

  public void setSideBFlagLocation(Location location) {
    setSideFlagLocation(1, location);
  }

  public Location getSideAFlagLocation() {
    return getSideFlagLocation(0);
  }

  public Location getSideBFlagLocation() {
    return getSideFlagLocation(1);
  }

  public void loadMapFromFile(List<Location> soloSpawnLocations, List<Location> sideASpawnLocations,
      List<Location> sideBSpawnLocations, Location sideAFlagLocation, Location sideBFlagLocation) {
    this.soloSpawnLocations = soloSpawnLocations;
    this.sideSpawnLocations.put(0, sideASpawnLocations);
    this.sideSpawnLocations.put(1, sideBSpawnLocations);

    this.sideFlagLocation.put(0, sideAFlagLocation);
    this.sideFlagLocation.put(1, sideBFlagLocation);
  }

  public boolean locationInMap(Location location) {
    double[] dim = new double[2];

    dim[0] = cornerA.getX();
    dim[1] = cornerB.getX();
    Arrays.sort(dim);
    if (location.getX() > dim[1] || location.getX() < dim[0])
      return false;

    dim[0] = cornerA.getZ();
    dim[1] = cornerB.getZ();
    Arrays.sort(dim);
    if (location.getZ() > dim[1] || location.getZ() < dim[0])
      return false;

    return true;
  }

  public void setBoundry(Location cornerA, Location cornerB) {
    this.cornerA = cornerA;
    this.cornerB = cornerB;
  }

  public boolean playerInMap(Player player) {
    return locationInMap(player.getLocation());
  }

  public void addSoloSpawnpoint(Location location) {
    soloSpawnLocations.add(location);
  }

  public void addSideSpawnPoint(Integer side, Location location) {
    sideSpawnLocations.get(side).add(location);
  }

  public void addSideASpawnPoint(Location location) {
    addSideSpawnPoint(0, location);
  }

  public void addSideBSpawnPoint(Location location) {
    addSideSpawnPoint(1, location);
  }

  public Location getSpawnPoint(Team team) {
    Random random = new Random();
    Location spawnPoint = null;
    if (team == null) {
      if (soloSpawnLocations.size() > 0) {
        Integer spawnInt = random.nextInt(soloSpawnLocations.size());
        spawnPoint = soloSpawnLocations.get(spawnInt);
      }
    } else {
      Integer playerTeamSide = getTeamSide(team);
      List<Location> spawnLocations = getSideSpawnLocations(playerTeamSide);
      if (spawnLocations != null && (spawnLocations.size() > 0)) {
        Integer spawnInt = random.nextInt(spawnLocations.size());
        spawnPoint = spawnLocations.get(spawnInt);
      }
    }

    if (spawnPoint != null) {
      Location adjustedSpawnPoint = spawnPoint.clone();
      adjustedSpawnPoint.add(0, 1, 0);
      return adjustedSpawnPoint;
    }
    return null;
  }

  public Location getSpawnPoint() {
    return getSpawnPoint(null);
  }
}
