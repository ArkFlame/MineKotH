package com.arkflame.minekoth;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.arkflame.minekoth.commands.KothCommand;
import com.arkflame.minekoth.koth.events.listeners.KothEventPlayerMoveListener;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
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
import com.arkflame.minekoth.utils.FoliaAPI;

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

    @Override
    public void onEnable() {
        setInstance(this);

        // Managers
        particleScheduler = new ParticleScheduler(this);

        // Lang
        langManager = new LangManager(getDataFolder());
        scheduleRunnerTask = new ScheduleRunnerTask();
        kothEventTickTask = new KothEventTickTask();

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
    }
}