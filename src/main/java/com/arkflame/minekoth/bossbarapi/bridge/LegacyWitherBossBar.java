package com.arkflame.minekoth.bossbarapi.bridge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.arkflame.minekoth.bossbarapi.enums.BarColor;
import com.arkflame.minekoth.bossbarapi.enums.BarStyle;
import com.arkflame.minekoth.colorapi.ColorAPI;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation for 1.8 servers using the "Wither trick".
 * Spawns and controls an invisible Wither entity far below the player.
 */
public class LegacyWitherBossBar implements BossBarBridge {
    private static final int WITHER_HEALTH = 300; // Max health of a Wither
    private static final int WITHER_DISTANCE_BELOW = 35;

    private String text = "";
    private double progress = 1.0;
    private boolean visible = true;
    private final Map<UUID, Object> withers = new HashMap<>(); // Player UUID -> NMS Wither Entity

    // --- Reflection Caches ---
    private static Class<?> craftWorldClass;
    private static Class<?> entityWitherClass;
    private static Class<?> entityClass;
    private static Class<?> dataWatcherClass;
    private static Class<?> packetPlayOutSpawnEntityLivingClass;
    private static Class<?> packetPlayOutEntityDestroyClass;
    private static Class<?> packetPlayOutEntityMetadataClass;
    private static Class<?> packetPlayOutEntityTeleportClass;

    private static Constructor<?> witherConstructor;
    private static Constructor<?> spawnPacketConstructor;
    private static Constructor<?> destroyPacketConstructor;
    private static Constructor<?> metadataPacketConstructor;
    private static Constructor<?> teleportPacketConstructor;
    
    private static Method getHandleWorldMethod;
    private static Method getHandlePlayerMethod;
    private static Method sendPacketMethod;
    private static Method setLocationMethod;
    private static Method setCustomNameMethod;
    private static Method setCustomNameVisibleMethod;
    private static Method setHealthMethod;
    private static Method setInvisibleMethod;
    private static Method getIdMethod;
    private static Method getDataWatcherMethod;
    
    private static Field playerConnectionField;

    static {
        try {
            String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            String craftbukkitPackage = "org.bukkit.craftbukkit." + nmsVersion;
            String nmsPackage = "net.minecraft.server." + nmsVersion;

            craftWorldClass = Class.forName(craftbukkitPackage + ".CraftWorld");
            Class<?> craftPlayerClass = Class.forName(craftbukkitPackage + ".entity.CraftPlayer");
            
            entityWitherClass = Class.forName(nmsPackage + ".EntityWither");
            entityClass = Class.forName(nmsPackage + ".Entity");
            dataWatcherClass = Class.forName(nmsPackage + ".DataWatcher");
            Class<?> worldClass = Class.forName(nmsPackage + ".World");
            
            packetPlayOutSpawnEntityLivingClass = Class.forName(nmsPackage + ".PacketPlayOutSpawnEntityLiving");
            packetPlayOutEntityDestroyClass = Class.forName(nmsPackage + ".PacketPlayOutEntityDestroy");
            packetPlayOutEntityMetadataClass = Class.forName(nmsPackage + ".PacketPlayOutEntityMetadata");
            packetPlayOutEntityTeleportClass = Class.forName(nmsPackage + ".PacketPlayOutEntityTeleport");
            
            Class<?> entityPlayerClass = Class.forName(nmsPackage + ".EntityPlayer");
            Class<?> playerConnectionClass = Class.forName(nmsPackage + ".PlayerConnection");
            Class<?> packetClass = Class.forName(nmsPackage + ".Packet");

            witherConstructor = entityWitherClass.getConstructor(worldClass);
            spawnPacketConstructor = packetPlayOutSpawnEntityLivingClass.getConstructor(Class.forName(nmsPackage + ".EntityLiving"));
            destroyPacketConstructor = packetPlayOutEntityDestroyClass.getConstructor(int[].class);
            metadataPacketConstructor = packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class);
            teleportPacketConstructor = packetPlayOutEntityTeleportClass.getConstructor(entityClass);
            
            getHandleWorldMethod = craftWorldClass.getMethod("getHandle");
            getHandlePlayerMethod = craftPlayerClass.getMethod("getHandle");
            sendPacketMethod = playerConnectionClass.getMethod("sendPacket", packetClass);
            
            setLocationMethod = entityWitherClass.getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
            setCustomNameMethod = entityWitherClass.getMethod("setCustomName", String.class);
            setCustomNameVisibleMethod = entityWitherClass.getMethod("setCustomNameVisible", boolean.class);
            setHealthMethod = entityWitherClass.getMethod("setHealth", float.class);
            setInvisibleMethod = entityWitherClass.getMethod("setInvisible", boolean.class);
            getIdMethod = entityWitherClass.getMethod("getId");
            getDataWatcherMethod = entityWitherClass.getMethod("getDataWatcher");

            playerConnectionField = entityPlayerClass.getField("playerConnection");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setText(String text) {
        this.text = ColorAPI.colorize(text).toLegacyText();
        updateMetadata();
    }

    @Override
    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
        updateMetadata();
    }
    
    @Override
    public void addPlayer(Player player) {
        try {
            Object nmsWither = createWither(player);
            withers.put(player.getUniqueId(), nmsWither);
            sendPacket(player, spawnPacketConstructor.newInstance(nmsWither));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removePlayer(Player player) {
        Object nmsWither = withers.remove(player.getUniqueId());
        if (nmsWither != null) {
            try {
                sendPacket(player, destroyPacketConstructor.newInstance(new int[]{(int) getIdMethod.invoke(nmsWither)}));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updatePosition(Player player) {
        Object nmsWither = withers.get(player.getUniqueId());
        if (nmsWither == null) return;
        
        try {
            Location loc = getWitherLocation(player);
            setLocationMethod.invoke(nmsWither, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
            sendPacket(player, teleportPacketConstructor.newInstance(nmsWither));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMetadata() {
        for (Map.Entry<UUID, Object> entry : withers.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) continue;
            
            try {
                Object nmsWither = entry.getValue();
                setCustomNameMethod.invoke(nmsWither, this.text);
                setHealthMethod.invoke(nmsWither, (float) (this.progress * WITHER_HEALTH));
                
                Object dataWatcher = getDataWatcherMethod.invoke(nmsWither);
                int witherId = (int) getIdMethod.invoke(nmsWither);
                
                sendPacket(p, metadataPacketConstructor.newInstance(witherId, dataWatcher, true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Object createWither(Player player) throws Exception {
        Object nmsWorld = getHandleWorldMethod.invoke(player.getWorld());
        Object nmsWither = witherConstructor.newInstance(nmsWorld);

        setCustomNameMethod.invoke(nmsWither, this.text);
        setCustomNameVisibleMethod.invoke(nmsWither, true);
        setHealthMethod.invoke(nmsWither, (float) (this.progress * WITHER_HEALTH));
        setInvisibleMethod.invoke(nmsWither, true);

        Location loc = getWitherLocation(player);
        setLocationMethod.invoke(nmsWither, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        
        return nmsWither;
    }
    
    private Location getWitherLocation(Player player) {
        if (!visible) {
            // Send it to the void to hide it
            return player.getLocation().clone().add(0, -500, 0);
        }
        // Spawn it relative to the player's view direction
        Vector direction = player.getLocation().getDirection();
        return player.getLocation().add(direction.multiply(WITHER_DISTANCE_BELOW));
    }

    private void sendPacket(Player player, Object packet) throws Exception {
        Object handle = getHandlePlayerMethod.invoke(player);
        Object playerConnection = playerConnectionField.get(handle);
        sendPacketMethod.invoke(playerConnection, packet);
    }

    // --- Unused methods for API compatibility ---
    @Override public void setColor(BarColor color) {}
    @Override public void setStyle(BarStyle style) {}
    @Override public void destroy() { withers.clear(); }
    @Override public void setVisible(boolean visible) {
        if(this.visible != visible) {
            this.visible = visible;
            withers.forEach((uuid, wither) -> {
                Player p = Bukkit.getPlayer(uuid);
                if(p != null) updatePosition(p);
            });
        }
    }
}