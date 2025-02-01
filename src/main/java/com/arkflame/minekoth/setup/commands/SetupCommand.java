package com.arkflame.minekoth.setup.commands;

import org.bukkit.entity.Player;

import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.Rewards;
import com.arkflame.minekoth.lang.Lang;
import com.arkflame.minekoth.menus.KothEditMenu;
import com.arkflame.minekoth.setup.session.SetupSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SetupCommand {
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
                (p, a) -> player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.unknown-command"))));
        handler.handle(player, args);
    }

    private static void handleHelp(Player player, String[] args) {
        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.setup-help"));
    }

    private static void handleStart(Player player, String[] args) {
        if (hasActiveSession(player)) {
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.already-in-session"));
            return;
        }

        MineKoth.getInstance().getSessionManager().addSession(player, new SetupSession());
        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.start-setup"));
    }

    private static void handleCancel(Player player, String[] args) {
        if (!hasActiveSession(player)) {
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-session"));
            return;
        }

        MineKoth.getInstance().getSessionManager().removeSession(player);
        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.cancel-setup"));
    }

    public static void handleFinish(Player player, String[] args) {
        SetupSession session = MineKoth.getInstance().getSessionManager().getSession(player);
        if (session == null) {
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-session"));
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
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.specify-id-name"));
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
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.no-koth-id-or-name").replace("<id_or_name>", kothIdOrName));
            return;
        }

        new KothEditMenu(koth).open(player);
        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.edit-started"));
    }

    private static boolean validateSession(Player player, SetupSession session) {
        if (session.isComplete()) {
            return true;
        }

        player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.setup-not-complete"));

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
                player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.set-field").replace("<field>", field));
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
            sessionId = MineKoth.getInstance().getKothManager().generateUniqueId();
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
        Lang
         lang = MineKoth.getInstance().getLangManager().getLang(player);
        player.sendMessage(lang.getMessage("messages.koth-setup-complete")
                .replace("<name>", session.getName())
                .replace("<times>", session.getTimes())
                .replace("<time_limit>", String.valueOf(session.getTimeLimit()))
                .replace("<capture_time>", String.valueOf(session.getCaptureTime()))
                .replace("<reward_items>", String.valueOf(session.getRewards().length))
                .replace("<reward_commands>", session.getRewardsCommands().toString()));
    }

    private static boolean hasActiveSession(Player player) {
        return MineKoth.getInstance().getSessionManager().hasSession(player);
    }
}