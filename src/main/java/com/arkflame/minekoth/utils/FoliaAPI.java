package com.arkflame.minekoth.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
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

@SuppressWarnings("unchecked")
public class FoliaAPI {
    private static final Map<String, Method> cachedMethods = new HashMap<>();
    
    private static final BukkitScheduler bS = Bukkit.getScheduler();
    private static final Object globalRegionScheduler = getGlobalRegionScheduler();
    private static final Object regionScheduler = getRegionScheduler();

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            if (cachedMethods.containsKey(methodName)) {
                return cachedMethods.get(methodName);
            }

            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            cachedMethods.put(methodName, method);
            return method;
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private static Object invokeMethod(Method method, Object object, Object... args) {
        try {
            if (method != null) {
                method.setAccessible(true);
                return method.invoke(object, args);
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private static Object getGlobalRegionScheduler() {
        return invokeMethod(getMethod(Server.class, "getGlobalRegionScheduler"), Bukkit.getServer());
    }

    private static Object getRegionScheduler() {
        return invokeMethod(getMethod(Server.class, "getRegionScheduler"), Bukkit.getServer());
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return globalRegionScheduler != null && regionScheduler != null;
        } catch (Exception ig) {
            return false;
        }
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
        // New system using the method cache
        Method method = getMethod(globalRegionScheduler.getClass(), "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);
        invokeMethod(method, globalRegionScheduler, MineKoth.getInstance(), run, delay, period);
    }

    public static void runTaskTimer(Consumer<Object> run, long delay, long period) {
        if (!isFolia()) {
            bS.runTaskTimer(MineKoth.getInstance(), () -> run.accept(null), delay, period);
            return;
        }
        try {
            Method m = globalRegionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, Consumer.class,
                    long.class, long.class);
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
            executeMethod.invoke(globalRegionScheduler, MineKoth.getInstance(),
                    (Consumer<Object>) ignored -> run.run());
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
            Method executeMethod = entityScheduler.getClass().getMethod("execute", Plugin.class, Runnable.class,
                    Runnable.class, long.class);
            executeMethod.invoke(entityScheduler, MineKoth.getInstance(), run, retired, delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTaskForEntityRepeating(Entity entity, Consumer<Object> task, Runnable retired,
            long initialDelay, long period) {
        if (!isFolia()) {
            bS.runTaskTimer(MineKoth.getInstance(), () -> task.accept(null), initialDelay, period);
            return;
        }
        try {
            Method getSchedulerMethod = entity.getClass().getMethod("getScheduler");
            Object entityScheduler = getSchedulerMethod.invoke(entity);
            Method runAtFixedRateMethod = entityScheduler.getClass().getMethod("runAtFixedRate", Plugin.class,
                    Consumer.class, Runnable.class, long.class, long.class);
            runAtFixedRateMethod.invoke(entityScheduler, MineKoth.getInstance(), task, retired, initialDelay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTaskForRegion(World world, int chunkX, int chunkZ, Runnable run) {
        if (!isFolia()) {
            bS.runTask(MineKoth.getInstance(), run);
            return;
        }
        try {
            Method executeMethod = regionScheduler.getClass().getMethod("execute", Plugin.class, World.class, int.class,
                    int.class, Runnable.class);
            executeMethod.invoke(regionScheduler, MineKoth.getInstance(), world, chunkX, chunkZ, run);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTaskForRegion(Location location, Runnable run) {
        if (!isFolia()) {
            bS.runTask(MineKoth.getInstance(), run);
            return;
        }
        try {
            Method executeMethod = regionScheduler.getClass().getMethod("execute", Plugin.class, Location.class,
                    Runnable.class);
            executeMethod.invoke(regionScheduler, MineKoth.getInstance(), location, run);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTaskForRegionRepeating(Location location, Consumer<Object> task, long initialDelay,
            long period) {
        if (!isFolia()) {
            bS.runTaskTimer(MineKoth.getInstance(), () -> task.accept(null), initialDelay, period);
            return;
        }
        try {
            Method runAtFixedRateMethod = regionScheduler.getClass().getMethod("runAtFixedRate", Plugin.class,
                    Location.class, Consumer.class, long.class, long.class);
            runAtFixedRateMethod.invoke(regionScheduler, MineKoth.getInstance(), location, task, initialDelay, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTaskForRegionDelayed(Location location, Consumer<Object> task, long delay) {
        if (!isFolia()) {
            bS.runTaskLater(MineKoth.getInstance(), () -> task.accept(null), delay);
            return;
        }
        try {
            Method runDelayedMethod = regionScheduler.getClass().getMethod("runDelayed", Plugin.class, Location.class,
                    Consumer.class, long.class);
            runDelayedMethod.invoke(regionScheduler, MineKoth.getInstance(), location, task, delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}