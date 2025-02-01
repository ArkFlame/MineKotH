package com.arkflame.minekoth;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.arkflame.minekoth.commands.KothCommand;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.listeners.KothEventPlayerMoveListener;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.koth.events.random.RandomEventsManager;
import com.arkflame.minekoth.koth.events.tasks.KothEventTickTask;
import com.arkflame.minekoth.koth.loaders.KothLoader;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.lang.LangManager;
import com.arkflame.minekoth.particles.ParticleScheduler;
import com.arkflame.minekoth.placeholders.MineKothPlaceholderExtension;
import com.arkflame.minekoth.player.PlayerDataManager;
import com.arkflame.minekoth.player.mysql.MySQLPlayerDataManager;
import com.arkflame.minekoth.player.yaml.YamlPlayerDataManager;
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

    // Optionally, add a getter for playerDataManager for use in other parts of the plugin.
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    /**
     * Creates and returns a JDBC Connection to the MySQL database.
     * Returns null if connection creation fails.
     */
    private Connection createMySQLConnection(String host, int port, String database, String username, String password) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        try {
            // Load the JDBC driver if necessary.
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            getLogger().log(Level.SEVERE, "Error establishing MySQL connection.", e);
            return null;
        }
    }

    public void initializeDatabase(MineKoth plugin) {
        // Get the storage type from config (mysql, yaml, memory).
        String storageType = getConfig().getString("playerdata.storage", "memory").toLowerCase();

        switch (storageType) {
            case "mysql":
                // MySQL configuration from config.yml.
                String host = getConfig().getString("mysql.host", "localhost");
                int port = getConfig().getInt("mysql.port", 3306);
                String database = getConfig().getString("mysql.database", "mydatabase");
                String username = getConfig().getString("mysql.username", "myusername");
                String password = getConfig().getString("mysql.password", "mypassword");

                // Attempt to create a MySQL connection.
                Connection connection = createMySQLConnection(host, port, database, username, password);
                if (connection == null) {
                    getLogger().severe("Failed to create MySQL connection. Disabling plugin.");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                playerDataManager = new MySQLPlayerDataManager(connection, getLogger());
                getLogger().info("Using MySQLPlayerDataManager.");
                break;

            case "yaml":
                // Use YAML-based persistence.
                File dataFolder = new File(getDataFolder(), getConfig().getString("yaml.directory", "playerdata"));
                // Ensure the data folder exists.
                if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                    getLogger().severe("Failed to create YAML data folder: " + dataFolder.getAbsolutePath());
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
                playerDataManager = new YamlPlayerDataManager(dataFolder, new ConfigUtil(this), getLogger());
                getLogger().info("Using YamlPlayerDataManager.");
                break;

            case "memory":
            default:
                // Use in-memory persistence.
                playerDataManager = new PlayerDataManager();
                getLogger().info("Using in-memory PlayerDataManager.");
                break;
        }
    }

    @Override
    public void onEnable() {
        setInstance(this);
        saveDefaultConfig();
        initializeDatabase(this);

        HologramUtility.initialize(this);

        // Initialize Vault economy
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                this.economy = rsp.getProvider();
            }
        }

        // Managers
        particleScheduler = new ParticleScheduler(this);

        // Lang
        langManager = new LangManager(getDataFolder());
        scheduleRunnerTask = new ScheduleRunnerTask();
        kothEventTickTask = new KothEventTickTask();

        // Random Events
        randomEventsManager = new RandomEventsManager();

        // Koth Loader
        kothLoader = new KothLoader(this);

        // Schedule Loader
        scheduleLoader = new ScheduleLoader(this);

        // Bukkit Stuff
        PluginManager pluginManager = getServer().getPluginManager();

        // Listener - Koth Event
        pluginManager.registerEvents(new KothEventPlayerMoveListener(kothEventManager), this);

        // Listener - Setup
        pluginManager.registerEvents(new SetupChatListener(), this);
        pluginManager.registerEvents(new SetupInteractListener(), this);
        pluginManager.registerEvents(new SetupInventoryCloseListener(), this);

        // Tasks - Schedule
        FoliaAPI.runTaskTimerAsync(task -> scheduleRunnerTask.run(), 1, 20);

        // Tasks - Koth Event
        FoliaAPI.runTaskTimerAsync(task -> kothEventTickTask.run(), 1, 20);

        // Commands
        getCommand("koth").setExecutor(new KothCommand());

        // PlaceholderAPI
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            new MineKothPlaceholderExtension().register();
        }

        String webhookUrl = getConfig().getString("webhook-url");

        // Initialize the DiscordHook
        DiscordHook.init(webhookUrl);

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
    }

    public void onDisable() {
        DiscordHook.shutdown();
        MenuUtil.shutdown();
        HologramUtility.clearHolograms();
    }
}