package com.arkflame.minekoth.utils;

import org.bukkit.entity.Player;

import com.arkflame.mineclans.MineClans;
import com.arkflame.mineclans.api.MineClansAPI;
import com.arkflame.mineclans.models.Faction;
import com.arkflame.mineclans.models.FactionPlayer;

public class MineClansHook {
    public static boolean isSameTeam(Player p1, Player p2) {
        if (p1 == null || p2 == null) {
            return false;
        }
        MineClansAPI mineClansAPI = MineClans.getInstance().getAPI();

        FactionPlayer fp1 = mineClansAPI.getFactionPlayer(p1);
        FactionPlayer fp2 = mineClansAPI.getFactionPlayer(p2);
        if (fp1 != null && fp2 != null) {
            Faction f1 = fp1.getFaction();
            Faction f2 = fp2.getFaction();
            if (f1 != null && f2 != null) {
                return f1.equals(f2);
            }
        }
        return false;
    }

    public static String getClanName(Player player) {
        if (player == null) {
            return null;
        }
        MineClansAPI mineClansAPI = MineClans.getInstance().getAPI();
        FactionPlayer fp = mineClansAPI.getFactionPlayer(player);
        if (fp != null) {
            Faction faction = fp.getFaction();
            if (faction == null) {
                return null;
            }
            return faction.getName();
        }
        return null;
    }
}
