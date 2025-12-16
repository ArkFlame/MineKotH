package com.arkflame.minekoth.utils;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.ulrich.clans.data.ClanData;
import me.ulrich.clans.interfaces.UClans;

public class UClansHook {

    private static UClans getUClans() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("UltimateClans");
        if (plugin instanceof UClans) {
            return (UClans) plugin;
        }
        return null;
    }

    public static boolean isSameTeam(Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        
        UClans uclans = getUClans();
        if (uclans == null) {
            return false;
        }

        // Use PlayerAPIManager -> isSameClan(uuid1, uuid2)
        return uclans.getPlayerAPI().isSameClan(p1.getUniqueId(), p2.getUniqueId());
    }

    public static String getClanName(Player player) {
        if (player == null) {
            return null;
        }

        UClans uclans = getUClans();
        if (uclans == null) {
            return null;
        }

        // Use PlayerAPIManager -> getPlayerClan(uuid) -> Optional<ClanData>
        Optional<ClanData> clanOpt = uclans.getPlayerAPI().getPlayerClan(player.getUniqueId());
        
        if (clanOpt.isPresent()) {
            // Return the Clan Tag
            return clanOpt.get().getTag();
        }
        
        return null;
    }
}