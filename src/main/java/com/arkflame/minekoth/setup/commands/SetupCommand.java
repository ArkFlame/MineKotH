package com.arkflame.minekoth.setup.commands;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.Rewards;
import com.arkflame.minekoth.menus.KothEditMenu;
import com.arkflame.minekoth.setup.session.SetupSession;
import com.arkflame.minekoth.utils.ChatColors;

import net.md_5.bungee.api.ChatColor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SetupCommand {
    private static final String PREFIX = ChatColor.GOLD + "[KOTH] ";
    private static final Map<String, CommandHandler> COMMANDS;

    // Command handler functional interface
    @FunctionalInterface
    private interface CommandHandler {
        void handle(Player player, String[] args);
    }

    // Initialize command handlers in a static block
    static {
        Map<String, CommandHandler> commands = new HashMap<>();
        commands.put("help", SetupCommand::handleHelp);
        commands.put("start", SetupCommand::handleStart);
        commands.put("cancel", SetupCommand::handleCancel);
        commands.put("finish", SetupCommand::handleFinish);
        commands.put("setup", SetupCommand::handleSetup);
        COMMANDS = Collections.unmodifiableMap(commands);
    }

    public static void run(Player player, String[] args) {
        String subCommand = args.length <= 1 ? "help" : args[1].toLowerCase();
        CommandHandler handler = COMMANDS.getOrDefault(subCommand, COMMANDS.getOrDefault("setup",
                (p, a) -> sendMessage(p, ChatColor.RED + "Unknown command. Use /koth setup help for help.")));
        handler.handle(player, args);
    }

    private static void handleHelp(Player player, String[] args) {
        sendMessage(player,
                ChatColors.color("&7&m-------------------------------------"),
                ChatColors.color("&cMineKOTH setup commands:"),
                ChatColors.color("&e➤&7 /koth setup start - Start a new koth setup session."),
                ChatColors.color("&e➤&7 /koth setup cancel - Cancel the current koth setup session."),
                ChatColors.color("&e➤&7 /koth setup finish - Finish the current koth setup session."),
                ChatColors.color("&e➤&7 /koth setup <id/name> - Edit an existing koth."),
                ChatColors.color("&7&m-------------------------------------"));
    }

    private static void handleStart(Player player, String[] args) {
        if (hasActiveSession(player)) {
            sendMessage(player, ChatColors.color(
                    "&c✧ [MineKOTH] &e➤ &7You are already in a setup session. &cUse /koth setup cancel to cancel it."));
            return;
        }

        MineKoth.getInstance().getSessionManager().addSession(player, new SetupSession());
        sendMessage(player, ChatColors.color("&c✧ [MineKOTH] &e➤ &7Koth creation started. &aPlease type the name."));
    }

    private static void handleCancel(Player player, String[] args) {
        if (!hasActiveSession(player)) {
            sendMessage(player, ChatColors.color("&c✧ [MineKOTH] &e➤ &7You are not in a setup session."));
            return;
        }

        MineKoth.getInstance().getSessionManager().removeSession(player);
        sendMessage(player, ChatColors.color("&c✧ [MineKOTH] &e➤ &7Koth setup session cancelled."));
    }

    public static void handleFinish(Player player, String[] args) {
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            sendMessage(player, ChatColor.RED + "You are not in a setup session.");
            return;
        }

        if (!validateSession(player, session)) {
            return;
        }

        saveKoth(player, session);

        if (session.isEditing() && session.isComplete()) {
            new KothEditMenu(MineKoth.getInstance().getKothManager().getKothById(session.getId())).open(player);
        }
    }

    private static void handleSetup(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, ChatColor.RED + "Please specify a koth ID/name. /koth setup <id/name>");
            return;
        }

        String kothIdOrName = String.join(" ", args).substring(args[0].length() + 1);
        Koth koth;
        try {
            koth = MineKoth.getInstance().getKothManager().getKothById(Integer.parseInt(kothIdOrName));
        } catch (NumberFormatException e) {
            koth = MineKoth.getInstance().getKothManager().getKothByName(kothIdOrName);
        }

        if (koth == null) {
            sendMessage(player, ChatColor.RED + "Could not find koth with ID or name: " + kothIdOrName);
            return;
        }

        new KothEditMenu(koth).open(player);
        sendMessage(player, ChatColor.GREEN + "KOTH edit started! Enter the name of the koth.");
    }

    private static boolean validateSession(Player player, SetupSession session) {
        if (session.isComplete()) {
            return true;
        }

        sendMessage(player, ChatColor.RED + "Setup is not complete. Finish all steps before using this command.");

        Map<String, Boolean> validations = new HashMap<>();
        validations.put("name", session.isNameSet());
        validations.put("first position", session.isFirstPositionSet());
        validations.put("second position", session.isSecondPositionSet());
        validations.put("times", session.isTimesSet());
        validations.put("time limit", session.isTimeLimitSet());
        validations.put("capture time", session.isCaptureTimeSet());
        validations.put("rewards", session.isRewardsSet());

        validations.forEach((field, isSet) -> {
            if (!isSet) {
                sendMessage(player, ChatColor.RED + "You must set the " + field + " of the koth first.");
            }
        });

        return false;
    }

    private static void saveKoth(Player player, SetupSession session) {
        int sessionId = session.getId();
        if (sessionId != -1) {
            MineKoth.getInstance().getKothManager().deleteKoth(sessionId);
            MineKoth.getInstance().getScheduleManager().removeSchedulesByKoth(sessionId);
            MineKoth.getInstance().getKothLoader().delete(sessionId);
        } else {
            sessionId = MineKoth.getInstance().getKothManager().getNextId();
        }

        Koth koth = createKothFromSession(sessionId, session);
        MineKoth.getInstance().getSessionManager().removeSession(player);
        MineKoth.getInstance().getKothManager().addKoth(koth);
        MineKoth.getInstance().getScheduleManager().scheduleKoth(koth.getId(), session.getTimes(), session.getDays());
        MineKoth.getInstance().getKothLoader().save(koth);

        sendSuccessMessage(player, session);
    }

    private static Koth createKothFromSession(int id, SetupSession session) {
        return new Koth(
                id,
                session.getName(),
                session.getWorldName(),
                session.getFirstPosition(),
                session.getSecondPosition(),
                session.getTimeLimit(),
                session.getCaptureTime(),
                new Rewards(
                        session.getRewardsCommands(),
                        session.getRewards(),
                        session.getLootType(),
                        session.getLootAmount()),
                session.getTimes(),
                session.getDays());
    }

    private static void sendSuccessMessage(Player player, SetupSession session) {
        sendMessage(player,
                ChatColor.GREEN + "KOTH setup complete! KOTH saved.",
                ChatColor.GREEN + "Name: " + ChatColor.AQUA + session.getName(),
                ChatColor.GREEN + "Times: " + ChatColor.AQUA + session.getTimes(),
                ChatColor.GREEN + "Time Limit: " + ChatColor.AQUA + session.getTimeLimit() + " seconds",
                ChatColor.GREEN + "Capture Time: " + ChatColor.AQUA + session.getCaptureTime() + " seconds",
                ChatColor.GREEN + "Reward Items: " + ChatColor.AQUA + session.getRewards().length,
                ChatColor.GREEN + "Reward Commands: " + ChatColor.AQUA + session.getRewardsCommands());
    }

    private static boolean hasActiveSession(Player player) {
        return MineKoth.getInstance().getSessionManager().hasSession(player);
    }

    private static void sendMessage(Player player, String... messages) {
        for (String message : messages) {
            player.sendMessage(PREFIX + message);
        }
    }
}