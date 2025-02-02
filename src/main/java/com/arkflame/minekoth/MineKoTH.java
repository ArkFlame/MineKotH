package com.arkflame.minekoth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.arkflame.minekoth.commands.KothCommand;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.listeners.KothEventPlayerListener;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.koth.events.random.RandomEventsManager;
import com.arkflame.minekoth.koth.events.tasks.KothEventTickTask;
import com.arkflame.minekoth.koth.loaders.KothLoader;
import com.arkflame.minekoth.koth.managers.KothManager;
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
import com.arkflame.minekoth.utils.HologramUtility;
import com.arkflame.minekoth.utils.MenuUtil;

import net.milkbowl.vault.economy.Economy;

public class MineKoth extends JavaPlugin {
    private static MineKoth instance;
    private SetupSessionManager sessionManager = new SetupSessionManager();
    private KothManager kothManager = new KothManager();
    private ScheduleManager scheduleManager = new ScheduleManager();
    private KothEventManager kothEventManager = new KothEventManager();
    private LangManager langManager;
    private ScheduleRunnerTask scheduleRunnerTask;
    private KothEventTickTask kothEventTickTask;
    private ParticleScheduler particleScheduler;
    private RandomEventsManager randomEventsManager;
    private Economy economy;
    private KothLoader kothLoader;
    private ScheduleLoader scheduleLoader;
    private PlayerDataManager playerDataManager;

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

    @Override
    public void onEnable() {
        setInstance(this);
        saveDefaultConfig();

        // Initialize database
        playerDataManager = PlayerDataInitializer.initializeDatabase(this);

        // Initialize hologram utility
        HologramUtility.initialize(this);
        getLogger().info("Hologram Utility initialized.");

        // Initialize Vault economy
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
                getLogger().info("Vault economy enabled.");
            }
        }

        // Managers
        particleScheduler = new ParticleScheduler(this);
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
        FoliaAPI.runTaskTimerAsync(task -> (scheduleRunnerTask = new ScheduleRunnerTask()).run(), 1, 20);

        // Tasks - Koth Event
        FoliaAPI.runTaskTimerAsync(task -> (kothEventTickTask = new KothEventTickTask()).run(), 1, 20);

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

        // Load all koths
        for (Koth koth : kothLoader.loadAll()) {
            kothManager.addKoth(koth);
        }

        // Load all schedules
        for (Schedule schedule : scheduleLoader.loadAll()) {
            scheduleManager.addSchedule(schedule);
        }

        // Load online player data
        for (Player player : getServer().getOnlinePlayers()) {
            playerDataManager.getAndLoad(player.getUniqueId().toString());
        }
    }

    public void onDisable() {
        DiscordHook.shutdown();
        MenuUtil.shutdown();
        HologramUtility.clearHolograms();
    }
}