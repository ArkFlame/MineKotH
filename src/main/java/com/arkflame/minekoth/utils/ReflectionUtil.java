package com.arkflame.minekoth.utils;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;
import org.bukkit.entity.Player.Spigot;

public class ReflectionUtil {
	private static Method getLocalePlayerMethod = null;
	private static Method getLocaleSpigotMethod = null;

	public static Method getLocalePlayerMethod(Player player) throws NoSuchMethodException, SecurityException {
		getLocalePlayerMethod = getLocalePlayerMethod == null
				? getLocalePlayerMethod = player.getClass().getMethod("getLocale")
				: getLocalePlayerMethod;
		getLocalePlayerMethod.setAccessible(true);
		return getLocalePlayerMethod;
	}

	public static Method getLocaleSpigotMethod() throws NoSuchMethodException, SecurityException {
		getLocaleSpigotMethod = getLocaleSpigotMethod == null
				? getLocaleSpigotMethod = Spigot.class.getMethod("getLocale")
				: getLocaleSpigotMethod;
		getLocaleSpigotMethod.setAccessible(true);
		return getLocaleSpigotMethod;
	}

	public static String getLocale(Player player) {
		try {
			return (String) getLocalePlayerMethod(player).invoke(player);
		} catch (Exception ex) {
			/* Couldn't get locale */
		}
		try {
			return (String) getLocaleSpigotMethod().invoke(player.spigot());
		} catch (Exception ex) {
			/* Couldn't get locale */
		}

		return null;
	}
}
