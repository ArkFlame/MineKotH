package com.arkflame.minekoth.utils;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.arkflame.minekoth.MineKoth;

@SuppressWarnings("unchecked")
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
    	try { Class.forName("io.papermc.paper.threadedregions.RegionizedServer"); return (true && globalRegionScheduler != null); } catch (Exception ig) { return false; }
    }
    
    public static void runTaskAsync(Runnable run) {
        if (!isFolia()) {
        	bS.runTaskAsynchronously(MineKoth.getInstance(), run);
            return;
        }
        Executors.defaultThreadFactory().newThread(run).start();
    }
    
    public static void runTaskTimerAsync(Consumer<Object> run, long delay, long period) {
        if (!isFolia()) {
        	bS.runTaskTimerAsynchronously(MineKoth.getInstance(), () -> run.accept(null), delay, period);
            return;
        }
        try {
        	Method m = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            m.setAccessible(true);
            m.invoke(globalRegionScheduler, MineKoth.getInstance(), run, delay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runTaskTimer(Consumer<Object> run, long delay, long period) {
        if (!isFolia()) {
        	bS.runTaskTimer(MineKoth.getInstance(), () -> run.accept(null), delay, period);
            return;
        }
        try {
        	Method m = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
            m.setAccessible(true);
            m.invoke(globalRegionScheduler, MineKoth.getInstance(), run, delay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runTask(Runnable run) {
        if (!isFolia()) {
            bS.runTask(MineKoth.getInstance(), run);
            return;
        }

        try {
            Method executeMethod = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
            executeMethod.setAccessible(true);
            executeMethod.invoke(globalRegionScheduler, MineKoth.getInstance(), (Consumer<Object>) ignored -> run.run());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runTask(Consumer<Object> run) {
        if (!isFolia()) {
            bS.runTask(MineKoth.getInstance(), () -> run.accept(null));
            return;
        }
        try {
        	Method executeMethod = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
            executeMethod.setAccessible(true);
            executeMethod.invoke(globalRegionScheduler, MineKoth.getInstance(), run);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runTaskForEntity(Entity entity, Runnable run, Runnable retired, long delay) {
    	if (!isFolia()) {
    		bS.runTaskLater(MineKoth.getInstance(), run, delay);
    		return;
    	} 
    	try {
            Method getSchedulerMethod = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getSchedulerMethod.invoke(entity);
            Method executeMethod = entityScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class, Runnable.class, long.class);
            executeMethod.invoke(entityScheduler, MineKoth.getInstance(), run, retired, delay);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} 
    }
    
    public static void runTaskForEntityRepeating(Entity entity, Consumer<Object> task, Runnable retired, long initialDelay, long period) {
        if (!isFolia()) {
            bS.runTaskTimer(MineKoth.getInstance(), () -> task.accept(null), initialDelay, period);
            return;
        }
        try {
            Method getSchedulerMethod = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getSchedulerMethod.invoke(entity);
            Method runAtFixedRateMethod = entityScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
            runAtFixedRateMethod.invoke(entityScheduler, MineKoth.getInstance(), task, retired, initialDelay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}