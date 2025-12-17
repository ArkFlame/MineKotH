package com.arkflame.minekoth.commands.subcommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.arkflame.minekoth.MineKoth;
import com.arkflame.minekoth.koth.Koth;
import com.arkflame.minekoth.utils.FoliaAPI;
import com.arkflame.minekoth.utils.Sounds;

public class TeleportSubCommand implements SubCommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage(
                    MineKoth.getInstance().getLangManager().getLang(player).getMessage("messages.usage-tp"));
            return true;
        }

        String kothIdOrName = String.join(" ", args);
        Koth tpKoth;
        
        try {
            tpKoth = MineKoth.getInstance().getKothManager().getKothById(Integer.parseInt(kothIdOrName));
        } catch (NumberFormatException e) {
            tpKoth = MineKoth.getInstance().getKothManager().getKothByName(kothIdOrName);
        }

        if (tpKoth == null) {
            player.sendMessage(MineKoth.getInstance().getLangManager().getLang(player)
                    .getMessage("messages.no-koth-id-or-name").replace("<id_or_name>", kothIdOrName));
            return true;
        }

        Location center = tpKoth.getCenter().clone().add(0, 2, 0);

        FoliaAPI.runTask(() -> {
            FoliaAPI.teleportPlayer(player, center, true);
            Sounds.play(player, 1, 1, "ENTITY_ENDERMAN_TELEPORT");
        });
        
        return true;
    }

    @Override
    public String getName() {
        return "teleport";
    }

    @Override
    public String getPermission() {
        return "minekoth.command.teleport";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}