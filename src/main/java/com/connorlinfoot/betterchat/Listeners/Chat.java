package com.connorlinfoot.betterchat.Listeners;

import com.connorlinfoot.betterchat.BetterChat;
import com.connorlinfoot.betterchat.ChannelHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.UUID;

public class Chat implements Listener {

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		String channel = ChannelHandler.getPlayerChannel(player);
		if (BetterChat.betterChat.getConfig().isSet("Channels." + channel + ".Prefix")) {
			String prefix = ChatColor.translateAlternateColorCodes('&', BetterChat.betterChat.getConfig().getString("Channels." + channel + ".Prefix"));
			event.setFormat(prefix + " " + event.getFormat());
		}

		if (BetterChat.betterChat.getConfig().isSet("Channels." + channel + ".Permission Required")
				&& !player.hasPermission("betterchat.channel." + channel)
				&& !player.hasPermission("betterchat.all")) {
			player.sendMessage(ChatColor.RED + "You do not have the required permissions to talk in this channel");
			event.setCancelled(false);
			return;
		}

		if (BetterChat.betterChat.getConfig().getBoolean("Spam Filter.Enable Same Message Blocking") && !player.hasPermission("betterchat.staff")) {
			/* Spam Filter - Check last message sent */
			if (ChannelHandler.lastMessages.containsKey(player.getUniqueId().toString())) {
				if (event.getMessage().equalsIgnoreCase(ChannelHandler.lastMessages.get(player.getUniqueId().toString()))) {
					player.sendMessage(ChatColor.RED + "You already sent that message");
					event.setCancelled(true);
					return;
				}
			}

            /* Spam Filter - Add to last messages sent */
			ChannelHandler.lastMessages.put(player.getUniqueId().toString(), event.getMessage());
		}

        /* Swear Filter */
		if (BetterChat.betterChat.getConfig().getBoolean("Swear Filter.Enable Swear Filter") && !player.hasPermission("betterchat.staff")) {
			boolean captured = false;
			if (BetterChat.betterChat.getConfig().getBoolean("Swear Filter.Enable Strict Swear Filter")) {
				for (String string : BetterChat.betterChat.getConfig().getStringList("Swear Filter.Words To Sensor")) {
					if (event.getMessage().toLowerCase().contains(string.toLowerCase())) {
						captured = true;
						break;
					}
				}
			} else {
				for (String string : BetterChat.betterChat.getConfig().getStringList("Swear Filter.Words To Sensor")) {
					String message1 = " " + event.getMessage() + " ";
					if (message1.toLowerCase().contains(" " + string.toLowerCase() + " ")) {
						captured = true;
						break;
					}
				}
			}

			if (captured) {
				player.sendMessage(ChatColor.RED + "Please do not use bad language on the server");
				event.setCancelled(true);
				return;
			}
		}

		if (BetterChat.betterChat.getConfig().getBoolean("Settings.Enable Chat Color")) {
			event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
		}

		boolean playerMentions = BetterChat.betterChat.getConfig().getBoolean("Settings.Enable Player Mentions");
		ArrayList<UUID> remove = new ArrayList<UUID>();
		for (Player player1 : event.getRecipients()) {
			if (!ChannelHandler.isInChannel(player1, channel))
				remove.add(player1.getUniqueId());

			if (playerMentions) {
				if (event.getMessage().toLowerCase().contains(" " + player1.getName().toLowerCase())) {
					player1.playSound(player1.getLocation(), Sound.CHEST_OPEN, 1, 1);
				}
			}
		}

		for (UUID uuid : remove) {
			Player player1 = Bukkit.getPlayer(uuid);
			if (player1 == null)
				continue;
			event.getRecipients().remove(player);
		}
	}

}
