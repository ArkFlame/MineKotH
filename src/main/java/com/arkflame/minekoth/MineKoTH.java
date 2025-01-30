package com.arkflame.minekoth;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.arkflame.minekoth.commands.KothCommand;
import com.arkflame.minekoth.koth.events.listeners.KothEventPlayerMoveListener;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.koth.events.random.RandomEventsManager;
import com.arkflame.minekoth.koth.events.tasks.KothEventTickTask;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.lang.LangManager;
import com.arkflame.minekoth.particles.ParticleScheduler;
import com.arkflame.minekoth.placeholders.MineKothPlaceholderExtension;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;
import com.arkflame.minekoth.schedule.tasks.ScheduleRunnerTask;
import com.arkflame.minekoth.setup.listeners.SetupChatListener;
import com.arkflame.minekoth.setup.listeners.SetupInteractListener;
import com.arkflame.minekoth.setup.listeners.SetupInventoryCloseListener;
import com.arkflame.minekoth.setup.session.SetupSessionManager;
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

    @Override
    public void onEnable() {
        setInstance(this);
        saveDefaultConfig();

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
    }

    public void onDisable() {
        DiscordHook.shutdown();
        MenuUtil.shutdown();
        HologramUtility.clearHolograms();

        // TODO: Save all koths

        // TODO: Save all schedules
    }
}