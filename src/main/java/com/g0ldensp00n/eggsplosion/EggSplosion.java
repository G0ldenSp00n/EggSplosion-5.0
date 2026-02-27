package com.g0ldensp00n.eggsplosion;

import com.g0ldensp00n.eggsplosion.handlers.Core.ArmorRemoveHandler;
import com.g0ldensp00n.eggsplosion.handlers.Core.DeathMessages;
import com.g0ldensp00n.eggsplosion.handlers.Core.EggExplode;
import com.g0ldensp00n.eggsplosion.handlers.Core.ExplosionRegen;
import com.g0ldensp00n.eggsplosion.handlers.Core.Food;
import com.g0ldensp00n.eggsplosion.handlers.Core.PickupDropHandler;
import com.g0ldensp00n.eggsplosion.handlers.Core.PlayerLeaveHandler;
import com.g0ldensp00n.eggsplosion.handlers.Core.RespawnHandler;
import com.g0ldensp00n.eggsplosion.handlers.Core.Weapon;
import com.g0ldensp00n.eggsplosion.handlers.GameModeManager.GameModeListeners;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyManager;
import com.g0ldensp00n.eggsplosion.handlers.LobbyManager.LobbyMenuSystem;
import com.g0ldensp00n.eggsplosion.handlers.MapManager.MapManager;
import com.g0ldensp00n.eggsplosion.handlers.Weapon.WeaponRegistry;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.plugin.java.JavaPlugin;

public class EggSplosion extends JavaPlugin {
    private String versionNumber;
    private ExplosionRegen explosionRegen;
    private MapManager mapManager;
    private LobbyManager lobbyManager;
    public String pluginFolder = getDataFolder().getAbsolutePath();
    private static EggSplosion plugin;

    @Override
    public void onEnable() {
        Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRules.KEEP_INVENTORY, true);
            world.setGameRule(GameRules.SHOW_DEATH_MESSAGES, false);
        });
        plugin = this;
        versionNumber = Bukkit.getServer().getPluginManager().getPlugin("EggSplosion").getDescription().getVersion();
        getLogger().info("Enabled EggSplosion v" + versionNumber);
        explosionRegen = new ExplosionRegen(this);
        mapManager = new MapManager(this, pluginFolder);
        lobbyManager = LobbyManager.getInstance(this, mapManager);
        new DeathMessages(this, lobbyManager);
        new EggExplode(this);
        new Weapon(this);
        new GameModeListeners(this, lobbyManager);
        new PickupDropHandler(this, lobbyManager);
        new ArmorRemoveHandler(this, lobbyManager);
        new RespawnHandler(this, lobbyManager);
        new LobbyMenuSystem(this, lobbyManager, mapManager);
        new Food(this);
        new PlayerLeaveHandler(this, lobbyManager);
        new WeaponRegistry();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(WeaponRegistry.createWeaponCommand(), "Give Player Weapon");
            commands.registrar().register(LobbyManager.createLobbyCommands(), "Lobby Manegment");
            commands.registrar().register(MapManager.createMapCommands(), "Map Manegment");
        });
        // this.getCommand("lobby").setExecutor(lobbyManager);
        // this.getCommand("lobby").setTabCompleter(lobbyManager);
        // this.getCommand("map").setExecutor(mapManager);
        // this.getCommand("map").setTabCompleter(mapManager);
        // this.getCommand("weapon").setExecutor(registry);
    }

    public static EggSplosion getInstance() {
        return plugin;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    @Override
    public void onDisable() {
        explosionRegen.repairAll();
        mapManager.saveMapsToFiles();
        lobbyManager.cleanupLobbies();
        getLogger().info("Disabled EggSplosion v" + versionNumber);
    }
}
