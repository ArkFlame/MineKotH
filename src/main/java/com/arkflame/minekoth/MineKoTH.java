package com.arkflame.minekoth;

import org.bukkit.plugin.java.JavaPlugin;

import com.arkflame.minekoth.commands.KoTHCommand;
import com.arkflame.minekoth.koth.events.KoTHEventManager;
import com.arkflame.minekoth.koth.managers.KoTHManager;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;
import com.arkflame.minekoth.schedule.tasks.ScheduleRunnerTask;
import com.arkflame.minekoth.setup.listeners.SetupChatListener;
import com.arkflame.minekoth.setup.listeners.SetupInteractListener;
import com.arkflame.minekoth.setup.listeners.SetupInventoryCloseListener;
import com.arkflame.minekoth.setup.session.SetupSessionManager;

public class MineKoTH extends JavaPlugin {
    private static MineKoTH instance;

    public static void setInstance(MineKoTH instance) {
        MineKoTH.instance = instance;
    }

    public static MineKoTH getInstance() {
        return MineKoTH.instance;
    }

    private SetupSessionManager sessionManager = new SetupSessionManager();

    public SetupSessionManager getSessionManager() {
        return sessionManager;
    }

    private KoTHManager koTHManager = new KoTHManager();

    public KoTHManager getKoTHManager() {
        return koTHManager;
    }

    private ScheduleManager scheduleManager = new ScheduleManager();

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    private KoTHEventManager koTHEventManager = new KoTHEventManager();

    public KoTHEventManager getKoTHEventManager() {
        return koTHEventManager;
    }

    @Override
    public void onEnable() {
        setInstance(this);

        // Listeners
        this.getServer().getPluginManager().registerEvents(new SetupChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new SetupInteractListener(), this);
        this.getServer().getPluginManager().registerEvents(new SetupInventoryCloseListener(), this);

        // Commands
        getCommand("koth").setExecutor(new KoTHCommand());

        // Tasks
        new ScheduleRunnerTask().runTaskTimer(this, 0, 1200);
    }
}