package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.koth.events.KothEvent;

public class BetSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        if (args.length < 2) {
            player.sendMessage(
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.usage-bet"));
            return true;
        }

        // args[0] = amount, args[1] = player name, args[2+] = koth name (optional)
        String kothIdOrNameBet = "";
        if (args.length > 2) {
            kothIdOrNameBet = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        }
        
        KothEvent betKothEvent = null;
        Koth betKoth = null;
        
        if (!kothIdOrNameBet.isEmpty()) {
            try {
                betKoth = MineKoth.getInstance().getKothManager().getKothById(Integer.parseInt(kothIdOrNameBet));
            } catch (NumberFormatException e) {
                betKoth = MineKoth.getInstance().getKothManager().getKothByName(kothIdOrNameBet);
            }

            if (betKoth != null) {
                betKothEvent = MineKoth.getInstance().getKothEventManager().getKothEvent(betKoth);
            }
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
                            .replace("<n>", running.getKoth().getName()));
                }
            }
            return true;
        }

        try {
            double amount = Double.parseDouble(args[0]);
            betKothEvent.getKothEventBets().placeBet(player, args[1], amount);
        } catch (NumberFormatException e) {
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.invalid-amount"));
        }
        
        return true;
    }

    @Override
    public String getName() {
        return "bet";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.bet";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}