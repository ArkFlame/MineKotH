package com.arkflame.minekoth;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.arkflame.minekoth.commands.KothCommand;
import com.arkflame.minekoth.koth.events.listeners.KothEventPlayerMoveListener;
import com.arkflame.minekoth.koth.events.managers.KothEventManager;
import com.arkflame.minekoth.koth.events.tasks.KothEventTickTask;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.placeholders.MineKothPlaceholderExtension;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;
import com.arkflame.minekoth.schedule.tasks.ScheduleRunnerTask;
import com.arkflame.minekoth.setup.listeners.SetupChatListener;
import com.arkflame.minekoth.setup.listeners.SetupInteractListener;
import com.arkflame.minekoth.setup.listeners.SetupInventoryCloseListener;
import com.arkflame.minekoth.setup.session.SetupSessionManager;

public class MineKoth extends JavaPlugin {
    private static MineKoth instance;

    public static void setInstance(MineKoth instance) {
        MineKoth.instance = instance;
    }

    public static MineKoth getInstance() {
        return MineKoth.instance;
    }

    private SetupSessionManager sessionManager = new SetupSessionManager();

    public SetupSessionManager getSessionManager() {
        return sessionManager;
    }

    private KothManager kothManager = new KothManager();

    public KothManager getKothManager() {
        return kothManager;
    }

    private ScheduleManager scheduleManager = new ScheduleManager();

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    private KothEventManager kothEventManager = new KothEventManager();

    public KothEventManager getKothEventManager() {
        return kothEventManager;
    }

    @Override
    public void onEnable() {
        setInstance(this);

        // Bukkit Stuff
        PluginManager pluginManager = getServer().getPluginManager();

        // Listener - Koth Event
        pluginManager.registerEvents(new KothEventPlayerMoveListener(kothEventManager), this);

        // Listener - Setup
        pluginManager.registerEvents(new SetupChatListener(), this);
        pluginManager.registerEvents(new SetupInteractListener(), this);
        pluginManager.registerEvents(new SetupInventoryCloseListener(), this);

        // Tasks - Schedule
        new ScheduleRunnerTask().runTaskTimer(this, 0, 20);

        // Tasks - Koth Event
        new KothEventTickTask().runTaskTimer(this, 0, 20);

        // Commands
        getCommand("koth").setExecutor(new KothCommand());

        // PlaceholderAPI
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            new MineKothPlaceholderExtension().register();
        }
    }
}