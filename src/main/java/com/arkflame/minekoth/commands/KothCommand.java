package com.arkflame.minekoth.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.arkflame.minekoth.setup.commands.SetupCommand;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;
import com.arkflame.minekoth.utils.Titles;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.CapturingPlayers;
import com.arkflame.minekoth.koth.events.KothEvent;
import com.arkflame.minekoth.koth.events.KothEvent.KothEventState;
import com.arkflame.minekoth.koth.managers.KothManager;
import com.arkflame.minekoth.menus.PlayerStatsMenu;
import com.arkflame.minekoth.playerdata.PlayerData;
import com.arkflame.minekoth.schedule.Schedule;
import com.arkflame.minekoth.schedule.commands.ScheduleCommand;
import com.arkflame.minekoth.schedule.managers.ScheduleManager;

public class KothCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(
                    MineKoth.getInstance().getLangManager().getLang(null).getMessage("messages.players-only"));
            return true;
        }

        Player player = (Player) sender;
        KothManager kothManager = MineKoth.getInstance().getKothManager();
        ScheduleManager scheduleManager = MineKoth.getInstance().getScheduleManager();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setup":
                SetupCommand.run(player, args);
                break;

            case "list":
                if (!sender.hasPermission("minekoth.command.list")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.list"));
                    break;
                }

                if (kothManager.getAllkoths().isEmpty()) {
                    MineKoth.getInstance().getLangManager().sendMessage(player, "messages.no-koths");
                    break;
                }
                MineKoth.getInstance().getLangManager().sendMessage(player, "messages.koth-list");
                kothManager.getAllkoths().values()
                        .forEach(koth -> MineKoth.getInstance().getLangManager().sendMessage(
                                player, "messages.koth-info-list",
                                "<id>", koth.getId(),
                                "<name>", koth.getName()));
                break;

            case "info":
                if (!sender.hasPermission("minekoth.command.info")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.info"));
                    break;
                }

                if (args.length < 2) {
                    player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.usage-koth-info"));
                    break;
                }
                try {
                    int id = Integer.parseInt(args[1]);
                    Koth koth = kothManager.getKothById(id);
                    if (koth == null) {
                        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                                .getMessage("messages.no-koth-id").replace("<id>", String.valueOf(id)));
                        break;
                    }
                    sendKothInfo(player, koth);
                } catch (NumberFormatException e) {
                    player.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.invalid-id"));
                }
                break;

            case "delete":
                if (!sender.hasPermission("minekoth.command.delete")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.delete"));
                    return true;
                }

                if (args.length < 2) {
                    MineKoth.getInstance().getLangManager().sendMessage(player, "messages.usage-delete");
                    break;
                }
                try {
                    int id = Integer.parseInt(args[1]);
                    Koth removed = kothManager.deleteKoth(id);
                    scheduleManager.removeSchedulesByKoth(id);
                    MineKoth.getInstance().getKothLoader().delete(id);
                    if (removed == null) {
                        MineKoth.getInstance().getLangManager().sendMessage(player, "messages.no-koth-id", "<id>", id);
                        return true;
                    }
                    MineKoth.getInstance().getLangManager().sendMessage(player, "messages.koth-deleted", "<id>", id);
                } catch (NumberFormatException e) {
                    MineKoth.getInstance().getLangManager().sendMessage(player, "messages.invalid-id");
                }
                break;

            case "schedule":
                ScheduleCommand.run(player, args);
                break;

            case "start":
                if (!sender.hasPermission("minekoth.command.start")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.start"));
                    return true;
                }
                Schedule schedule = MineKoth.getInstance().getScheduleManager().getNextSchedule();
                if (schedule == null) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.no-schedules"));
                    break;
                }
                Koth koth = schedule.getKoth();
                if (koth == null) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.invalid-koth"));
                    break;
                }
                if (MineKoth.getInstance().getKothEventManager().getKothEvent(koth) != null) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.koth-running"));
                    break;
                }
                MineKoth.getInstance().getKothEventManager().start(koth);
                sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                        .getMessage("messages.koth-schedule").replace("<name>", koth.getName()));
                sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                        .getMessage("messages.koth-running-list"));
                for (KothEvent k : MineKoth.getInstance().getKothEventManager().getRunningKoths()) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.koth-running-item").replace("<name>", k.getKoth().getName()));
                }
                break;

            case "stop":
                if (!sender.hasPermission("minekoth.command.stop")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.stop"));
                    return true;
                }

                if (!MineKoth.getInstance().getKothEventManager().isEventActive()) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.no-event-active"));
                    break;
                }
                if (MineKoth.getInstance().getKothEventManager().getKothEvent().getState() == KothEventState.CAPTURED) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.koth-captured"));
                    break;
                }
                MineKoth.getInstance().getKothEventManager().end();
                sender.sendMessage(
                        MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.event-stopped"));
                Titles.sendTitle(MineKoth.getInstance().getLangManager().getLang(player)
                        .getMessage("messages.event-stopped-title").replace("<name>", player.getName()),
                        MineKoth.getInstance().getLangManager().getLang(player)
                                .getMessage("messages.event-stopped-subtitle").replace("<name>", player.getName()),
                        20, 40, 20);
                break;

            case "capture":
                if (!sender.hasPermission("minekoth.command.capture")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.capture"));
                    return true;
                }

                if (!MineKoth.getInstance().getKothEventManager().isEventActive()) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.no-event-active"));
                    break;
                }
                if (MineKoth.getInstance().getKothEventManager().getKothEvent().getState() == KothEventState.CAPTURED) {
                    sender.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.koth-captured"));
                    break;
                }
                MineKoth.getInstance().getKothEventManager().getKothEvent().setCaptured(new CapturingPlayers(player));
                break;

            case "teleport":
            case "tp":
                if (!sender.hasPermission("minekoth.command.teleport")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.teleport"));
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.usage-tp"));
                    break;
                }

                String kothIdOrName = String.join(" ", args).substring(args[0].length() + 1);
                Koth tpKoth;
                try {
                    tpKoth = MineKoth.getInstance().getKothManager().getKothById(Integer.parseInt(kothIdOrName));
                } catch (NumberFormatException e) {
                    tpKoth = MineKoth.getInstance().getKothManager().getKothByName(kothIdOrName);
                }

                if (tpKoth == null) {
                    player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.no-koth-id-or-name").replace("<id_or_name>", kothIdOrName));
                    break;
                }

                Location center = tpKoth.getCenter();

                FoliaAPI.runTask(() -> {
                    FoliaAPI.teleportPlayer(player, center, true);
                    Sounds.play(player, 1, 1, "ENTITY_ENDERMAN_TELEPORT");
                });
                break;

            case "bet":
                if (!sender.hasPermission("minekoth.command.bet")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.bet"));
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.usage-bet"));
                    break;
                }

                String kothIdOrNameBet = String.join(" ", args)
                        .substring(args[0].length() + args[1].length() + args[2].length() + 2);
                KothEvent betKothEvent = null;
                Koth betKoth = null;
                try {
                    betKoth = MineKoth.getInstance().getKothManager().getKothById(Integer.parseInt(kothIdOrNameBet));
                } catch (NumberFormatException e) {
                    betKoth = MineKoth.getInstance().getKothManager().getKothByName(kothIdOrNameBet);
                }

                if (betKoth != null) {
                    betKothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent(betKoth);
                }

                if (betKothEvent == null && kothIdOrNameBet.isEmpty()) {
                    betKothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent();
                }

                if (betKothEvent == null) {
                    if (MineKoth.getInstance().getKothEventManager().getRunningKoths().length == 0) {
                        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                                .getMessage("messages.no-running-koths"));
                    } else {
                        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                                .getMessage("messages.no-koth-id-or-name").replace("<id_or_name>", kothIdOrNameBet));
                        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                                .getMessage("messages.available-koths"));
                        for (KothEvent running : MineKoth.getInstance().getKothEventManager().getRunningKoths()) {
                            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                                    .getMessage("messages.running-koth")
                                    .replace("<name>", running.getKoth().getName()));
                        }
                    }
                    break;
                }

                betKothEvent.getKothEventBets().placeBet(player, args[2], Double.parseDouble(args[1]));
                break;

            case "stats":
                if (!sender.hasPermission("minekoth.command.stats")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.stats"));
                    return true;
                }
                if (args.length > 1 && !sender.hasPermission("minekoth.command.stats.other")) {
                    sender.sendMessage(
                            MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-permission")
                                    .replace("<node>", "minekoth.command.stats.other"));
                    return true;
                }
                Player statsTarget = null;
                if (args.length < 2) {
                    statsTarget = player;
                } else {
                    statsTarget = Bukkit.getPlayer(args[1]);
                }

                if (statsTarget == null) {
                    player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.no-player").replace("<name>", args[1]));
                    break;
                }

                PlayerData playerData = MineKoth.getInstance().getPlayerDataManager()
                        .getIfLoaded(player.getUniqueId().toString());

                if (playerData == null) {
                    player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                            .getMessage("messages.no-player-data"));
                    break;
                }

                new PlayerStatsMenu(player, playerData).open();
                break;

            default:
                player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                        .getMessage("messages.unknown-subcommand"));
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(
                MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.koth-commands"));
    }

    private void sendKothInfo(Player player, Koth koth) {
        MineKoth.getInstance().getLangManager().sendMessage(player, "messages.koth-info",
                "<id>", koth.getId(),
                "<name>", koth.getName(),
                "<world>", koth.getWorldName(),
                "<time_limit>", koth.getTimeLimit(),
                "<time_to_capture>", koth.getTimeToCapture(),
                "<reward_items>", koth.getRewards().getRewardsItems().size(),
                "<reward_commands>", koth.getRewards().getRewardsCommands().toString());
    }
}
