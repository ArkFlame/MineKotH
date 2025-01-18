package com.arkflame.minekoth.utils;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.arkflame.minekoth.MineKoth;

public class FoliaAPI {

    private static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();
    private static final Plugin PLUGIN = MineKoth.getInstance();

    private static final Object GLOBAL_REGION_SCHEDULER = initializeScheduler("getGlobalRegionScheduler");
    private static final Object REGION_SCHEDULER = initializeScheduler("getRegionScheduler");

    private static Object initializeScheduler(String methodName) {
        try {
            Method method = Server.class.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(Bukkit.getServer());
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return GLOBAL_REGION_SCHEDULER != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void invokeSchedulerMethod(Object scheduler, String methodName, Object... args) {
        try {
            Method method = scheduler.getClass().getMethod(methodName, getParameterTypes(args));
            method.setAccessible(true);
            method.invoke(scheduler, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<?>[] getParameterTypes(Object... args) {
        return java.util.Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);
    }

    public static void runTaskAsync(Runnable task) {
        if (isFolia()) {
            Executors.defaultThreadFactory().newThread(task).start();
        } else {
            SCHEDULER.runTaskAsynchronously(PLUGIN, task);
        }
    }

    public static void runTaskTimerAsync(Consumer<Object> task, long delay, long period) {
        if (isFolia()) {
            invokeSchedulerMethod(GLOBAL_REGION_SCHEDULER, "runAtFixedRate", PLUGIN, task, delay, period);
        } else {
            SCHEDULER.runTaskTimerAsynchronously(PLUGIN, () -> task.accept(null), delay, period);
        }
    }

    public static void runTaskTimer(Consumer<Object> task, long delay, long period) {
        if (isFolia()) {
            invokeSchedulerMethod(GLOBAL_REGION_SCHEDULER, "runAtFixedRate", PLUGIN, task, delay, period);
        } else {
            SCHEDULER.runTaskTimer(PLUGIN, () -> task.accept(null), delay, period);
        }
    }

    public static void runTask(Runnable task) {
        if (isFolia()) {
            invokeSchedulerMethod(GLOBAL_REGION_SCHEDULER, "run", PLUGIN, (Consumer<Object>) ignored -> task.run());
        } else {
            SCHEDULER.runTask(PLUGIN, task);
        }
    }

    public static void runTask(Consumer<Object> task) {
        if (isFolia()) {
            invokeSchedulerMethod(GLOBAL_REGION_SCHEDULER, "run", PLUGIN, task);
        } else {
            SCHEDULER.runTask(PLUGIN, () -> task.accept(null));
        }
    }

    public static void runTaskForEntity(Entity entity, Runnable task, Runnable retired, long delay) {
        if (isFolia()) {
            try {
                Method getSchedulerMethod = entity.getClass().getMethod("getScheduler");
                Object entityScheduler = getSchedulerMethod.invoke(entity);
                invokeSchedulerMethod(entityScheduler, "execute", PLUGIN, task, retired, delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SCHEDULER.runTaskLater(PLUGIN, task, delay);
        }
    }

    public static void runTaskForEntityRepeating(Entity entity, Consumer<Object> task, Runnable retired, long initialDelay, long period) {
        if (isFolia()) {
            try {
                Method getSchedulerMethod = entity.getClass().getMethod("getScheduler");
                Object entityScheduler = getSchedulerMethod.invoke(entity);
                invokeSchedulerMethod(entityScheduler, "runAtFixedRate", PLUGIN, task, retired, initialDelay, period);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SCHEDULER.runTaskTimer(PLUGIN, () -> task.accept(null), initialDelay, period);
        }
    }

    public static void runTaskForRegion(World world, int chunkX, int chunkZ, Runnable task) {
        if (isFolia()) {
            invokeSchedulerMethod(REGION_SCHEDULER, "execute", PLUGIN, world, chunkX, chunkZ, task);
        } else {
            SCHEDULER.runTask(PLUGIN, task);
        }
    }

    public static void runTaskForRegion(Location location, Runnable task) {
        if (isFolia()) {
            invokeSchedulerMethod(REGION_SCHEDULER, "execute", PLUGIN, location, task);
        } else {
            SCHEDULER.runTask(PLUGIN, task);
        }
    }

    public static void runTaskForRegionRepeating(Location location, Consumer<Object> task, long initialDelay, long period) {
        if (isFolia()) {
            invokeSchedulerMethod(REGION_SCHEDULER, "runAtFixedRate", PLUGIN, location, task, initialDelay, period);
        } else {
            SCHEDULER.runTaskTimer(PLUGIN, () -> task.accept(null), initialDelay, period);
        }
    }

    public static void runTaskForRegionDelayed(Location location, Consumer<Object> task, long delay) {
        if (isFolia()) {
            invokeSchedulerMethod(REGION_SCHEDULER, "runDelayed", PLUGIN, location, task, delay);
        } else {
            SCHEDULER.runTaskLater(PLUGIN, () -> task.accept(null), delay);
        }
    }
}
