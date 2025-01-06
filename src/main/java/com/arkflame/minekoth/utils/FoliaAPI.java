package com.arkflame.minekoth.utils;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class FoliaAPI {
    private static final BukkitScheduler bS = Bukkit.getScheduler();
    private static final Object globalRegionScheduler = getGlobalRegionScheduler();
    
    private static Object getGlobalRegionScheduler() {
        try {
            Method method = Server.class.getDeclaredMethod("getGlobalRegionScheduler");
            method.setAccessible(true);
            return method.invoke(Bukkit.getServer());
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean isFolia() {
        return globalRegionScheduler != null;
    }
    
    public static void runTaskAsync(Plugin plugin, Runnable run) {
        if (!isFolia()) {
        	bS.runTaskAsynchronously(plugin, run);
            return;
        }
        Executors.defaultThreadFactory().newThread(run).start();
    }
    
    public static void runTaskTimerAsync(Plugin plugin, Consumer<Object> run, long delay, long period) {
        if (!isFolia()) {
        	bS.runTaskTimerAsynchronously(plugin, () -> run.accept(null), delay, period);
            return;
        }
        try {
        	Method m = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            m.setAccessible(true);
            m.invoke(globalRegionScheduler, plugin, run, delay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runTaskTimer(Plugin plugin, Consumer<Object> run, long delay, long period) {
        if (!isFolia()) {
        	bS.runTaskTimer(plugin, () -> run.accept(null), delay, period);
            return;
        }
        runTaskTimerAsync(plugin, run, delay, period);
    }
    
    public static void runTask(Plugin plugin, Runnable run) {
        if (!isFolia()) {
            bS.runTask(plugin, run);
            return;
        }

        try {
            Method executeMethod = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
            executeMethod.setAccessible(true);
            executeMethod.invoke(globalRegionScheduler, plugin, (Consumer<Object>) ignored -> run.run());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runTask(Plugin plugin, Consumer<Object> run) {
        if (!isFolia()) {
            bS.runTask(plugin, () -> run.accept(null));
            return;
        }
        try {
        	Method executeMethod = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
            executeMethod.setAccessible(true);
            executeMethod.invoke(globalRegionScheduler, plugin, run);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runTaskForEntity(Entity entity, Plugin plugin, Runnable run, Runnable retired, long delay) {
    	if (!isFolia()) {
    		bS.runTaskLater(plugin, run, delay);
    		return;
    	} 
    	try {
            Method getSchedulerMethod = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getSchedulerMethod.invoke(entity);
            Method executeMethod = entityScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);
            executeMethod.invoke(entityScheduler, plugin, run, retired, delay);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} 
    }
    
    public static void runTaskForEntityRepeating(Entity entity, Plugin plugin, Consumer<Object> task, Runnable retired, long initialDelay, long period) {
        if (!isFolia()) {
            bS.runTaskTimer(plugin, () -> task.accept(null), initialDelay, period);
            return;
        }
        try {
            Method getSchedulerMethod = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getSchedulerMethod.invoke(entity);
            Method runAtFixedRateMethod = entityScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
            runAtFixedRateMethod.invoke(entityScheduler, plugin, task, retired, initialDelay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}