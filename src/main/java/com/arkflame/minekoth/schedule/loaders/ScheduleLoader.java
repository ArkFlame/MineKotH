package com.arkflame.minekoth.schedule.loaders;

import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.utils.ConfigUtil;
import com.arkflame.minekoth.utils.Times;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class ScheduleLoader {
    private final JavaPlugin plugin;
    private final File schedulesDir;
    private final ConfigUtil configUtil;

    public ScheduleLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.schedulesDir = new File(plugin.getDataFolder(), "schedules");
        this.configUtil = new ConfigUtil(plugin);
        configUtil.createDirectory(schedulesDir);
    }

    public void save(Schedule schedule) {
        File scheduleFile = new File(schedulesDir, schedule.getId() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        // Set Schedule properties
        config.set("id", schedule.getId());
        config.set("kothId", schedule.getKothId());
        config.set("days", schedule.getDayNames());
        config.set("hour", schedule.getHour());
        config.set("minute", schedule.getMinute());

        // Save using ConfigUtil
        configUtil.saveConfig(config, scheduleFile);
    }

    public Schedule load(int scheduleId) {
        File scheduleFile = new File(schedulesDir, scheduleId + ".yml");
        YamlConfiguration config = configUtil.loadConfig(scheduleFile);

        if (config == null) return null; // Early exit if loading failed

        try {
            int id = config.getInt("id");
            int kothId = config.getInt("kothId");
            List<String> dayNames = config.getStringList("days");
            List<DayOfWeek> days = Times.parseDayNames(dayNames.toArray(new String[0]));
            int hour = config.getInt("hour");
            int minute = config.getInt("minute");
            return new Schedule(id, kothId, days, hour, minute);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load Schedule " + scheduleId, e);
            return null;
        }
    }

    public boolean delete(int scheduleId) {
        File scheduleFile = new File(schedulesDir, scheduleId + ".yml");
        return configUtil.deleteConfig(scheduleFile);
    }

    public List<Schedule> loadAll() {
        List<Schedule> schedules = new ArrayList<>();
        File[] files = schedulesDir.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null) return schedules;

        for (File file : files) {
            String fileName = file.getName().replace(".yml", "");
            try {
                int scheduleId = Integer.parseInt(fileName);
                Schedule schedule = load(scheduleId);
                if (schedule != null) {
                    if (schedule.getKoth() == null) {
                        plugin.getLogger().warning("Schedule " + scheduleId + " has no associated KOTH");
                        continue;
                    }
                    schedules.add(schedule);
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid Schedule file name: " + file.getName());
            }
        }

        return schedules;
    }
}
