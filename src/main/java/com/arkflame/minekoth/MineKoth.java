package com.arkflame.minekoth;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.arkflame.minekoth.commands.KothCommand;
import com.arkflame.minekoth.holograms.HologramsAPIUniversal;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.listeners.KothEventPlayerListener;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.koth.events.random.RandomEventsManager;
import com.arkflame.minekoth.koth.events.tasks.KothEventTickTask;
import com.arkflame.minekoth.koth.loaders.KothLoader;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.koth.tasks.KothTickTask;
import com.arkflame.minekoth.lang.LangManager;
import com.arkflame.minekoth.particles.ParticleScheduler;
import com.arkflame.minekoth.placeholders.MineKothPlaceholderExtension;
import com.arkflame.minekoth.playerdata.PlayerDataInitializer;
import com.arkflame.minekoth.playerdata.PlayerDataManager;
import com.arkflame.minekoth.playerdata.listeners.PlayerDataListener;
import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.schedule.loaders.ScheduleLoader;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;
import com.arkflame.minekoth.schedule.tasks.ScheduleRunnerTask;
import com.arkflame.minekoth.setup.listeners.SetupChatListener;
import com.arkflame.minekoth.setup.listeners.SetupInteractListener;
import com.arkflame.minekoth.setup.listeners.SetupInventoryCloseListener;
import com.arkflame.minekoth.setup.session.SetupSessionManager;
import com.arkflame.minekoth.utils.ConfigUtil;
import com.arkflame.minekoth.utils.DiscordHook;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.GlowingUtility;
import com.arkflame.minekoth.utils.MenuUtil;

import net.milkbowl.vault.economy.Economy;

public class MineKoth extends JavaPlugin {
    private static MineKoth instance;
    private SetupSessionManager sessionManager = new SetupSessionManager();
    private KothManager kothManager = new KothManager();
    private ScheduleManager scheduleManager = new ScheduleManager();
    private KothEventManager kothEventManager;
    private LangManager langManager;
    private ScheduleRunnerTask scheduleRunnerTask;
    private KothEventTickTask kothEventTickTask;
    private KothTickTask kothTickTask;
    private ParticleScheduler particleScheduler;
    private RandomEventsManager randomEventsManager;
    private Economy economy;
    private KothLoader kothLoader;
    private ScheduleLoader scheduleLoader;
    private PlayerDataManager playerDataManager = PlayerDataInitializer.initializeDatabase(this, "memory");

    public ParticleScheduler getParticleScheduler() {
        return particleScheduler;
    }

    public static void setInstance(MineKoth instance) {
        MineKoth.instance = instance;
    }

    public static MineKoth getInstance() {
        return MineKoth.instance;
    }

    public SetupSessionManager getSessionManager() {
        return sessionManager;
    }

    public KothManager getKothManager() {
        return kothManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public KothEventManager getKothEventManager() {
        return kothEventManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public ScheduleRunnerTask getScheduleRunnerTask() {
        return scheduleRunnerTask;
    }

    public KothEventTickTask getKothEventTickTask() {
        return kothEventTickTask;
    }

    public KothTickTask getKothTickTask() {
        return kothTickTask;
    }

    public RandomEventsManager getRandomEventsManager() {
        return randomEventsManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isEconomyPresent() {
        return economy != null;
    }

    public KothLoader getKothLoader() {
        return kothLoader;
    }

    public ScheduleLoader getScheduleLoader() {
        return scheduleLoader;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public void saveDefaults() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
    }

    @Override
    public void onEnable() {
        setInstance(this);
        saveDefaults();

        // Initialize database
        FoliaAPI.runTaskAsync(() -> {
            playerDataManager = PlayerDataInitializer.initializeDatabase(this);

            // Load online player data
            FoliaAPI.runTaskAsync(() -> {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (playerDataManager != null) {
                        playerDataManager.getAndLoad(player.getUniqueId().toString());
                    }
                }
            }, 20L);
        });

        // Initialize Koth Event Manager
        kothEventManager = new KothEventManager(this);

        // Initialize Vault economy
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
                getLogger().info("Vault economy enabled.");
            }
        }

        // Managers
        particleScheduler = new ParticleScheduler();
        getLogger().info("ParticleScheduler initialized.");

        // Lang
        langManager = new LangManager(getDataFolder(), new ConfigUtil(this));
        getLogger().info("Language Manager initialized.");

        // Random Events
        randomEventsManager = new RandomEventsManager();

        // Koth Loader
        kothLoader = new KothLoader(this);

        // Schedule Loader
        scheduleLoader = new ScheduleLoader(this);

        // Bukkit Stuff
        PluginManager pluginManager = getServer().getPluginManager();

        // Listener - Koth Event
        pluginManager.registerEvents(new KothEventPlayerListener(kothEventManager), this);

        // Listener - Setup
        pluginManager.registerEvents(new SetupChatListener(), this);
        pluginManager.registerEvents(new SetupInteractListener(), this);
        pluginManager.registerEvents(new SetupInventoryCloseListener(), this);

        // Listener - Player Data
        pluginManager.registerEvents(new PlayerDataListener(), this);

        // Tasks - Schedule
        FoliaAPI.runTaskTimerAsync(task -> (scheduleRunnerTask = new ScheduleRunnerTask()).run(), 20, 20);

        // Tasks - Koth Event
        FoliaAPI.runTaskTimerAsync(task -> (kothEventTickTask = new KothEventTickTask()).run(), 20, 20);

        // Tasks - Koths
        FoliaAPI.runTaskTimerAsync(task -> (kothTickTask = new KothTickTask()).run(), 20, 20);

        // Commands
        getCommand("koth").setExecutor(new KothCommand());

        // PlaceholderAPI
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            new MineKothPlaceholderExtension().register();
        }

        if (getConfig().getBoolean("webhook.enabled")) {
            // Initialize the DiscordHook
            DiscordHook.init(getConfig().getString("webhook.url"));
        }

        // Initialize the MenuUtil
        MenuUtil.registerEvents(this);
        HologramsAPIUniversal hologramsAPI = HologramsAPIUniversal.getHologramsAPI();
        if (hologramsAPI != HologramsAPIUniversal.NONE) {
            getLogger().info("Using " + hologramsAPI.getName() + " for holograms.");
        } else {
            getLogger().info(
                    "No hologram API found. Install any of the following: DecentHolograms, HolographicDisplays, or FancyHolograms.");
        }

        // Delay load
        FoliaAPI.runTaskAsync(() -> {
            // Load all koths
            for (Koth koth : kothLoader.loadAll()) {
                kothManager.addKoth(koth);
            }

            // Load all schedules
            for (Schedule schedule : scheduleLoader.loadAll()) {
                scheduleManager.addSchedule(schedule);
            }
        }, 20L);
    }

    public void onDisable() {
        DiscordHook.shutdown();
        MenuUtil.shutdown();
        HologramsAPIUniversal.getHologramsAPI().clearHolograms();
        if (playerDataManager != null) {
            playerDataManager.close();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            GlowingUtility.unsetGlowing(player);
        }
    }

    public boolean isMineClansEnabled() {
        return getServer().getPluginManager().isPluginEnabled("MineClans");
    }

    public int getLootMultiplier(Player player) {
        // Get the configured multiplier values from config
        List<Integer> multipliers = getConfig().getIntegerList("reward-multipliers");

        // Sort in descending order to check highest permissions first
        multipliers.sort(Collections.reverseOrder());

        // Check each multiplier permission from highest to lowest
        for (int multiplier : multipliers) {
            if (player.hasPermission("minekoth.reward.multiplier." + multiplier)) {
                return multiplier;
            }
        }

        // Return 1 as the default multiplier if no permissions found
        return 1;
    }
}