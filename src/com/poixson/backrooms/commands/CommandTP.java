package com.poixson.backrooms.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.commands.pxnCommand;
import com.poixson.utils.NumberUtils;


public class CommandTP extends pxnCommand {

	protected final BackroomsPlugin plugin;

	protected final String logPrefix;
	protected final String chatPrefix;



	public CommandTP(final BackroomsPlugin plugin) {
		super("tp", "teleport");
		this.plugin = plugin;
		this.logPrefix  = plugin.getLogPrefix();
		this.chatPrefix = plugin.getChatPrefix();
	}



	@Override
	public boolean run(final CommandSender sender,
			final Command cmd, final String[] args) {
		final Player player = (sender instanceof Player ? (Player)sender : null);
		if (player != null && !player.hasPermission("backrooms.tp")) {
			player.sendMessage(this.chatPrefix + "You don't have permission to use this.");
			return true;
		}
		final int numargs = args.length;
		// tp self random
		if (numargs == 1 && player != null) {
			this.plugin.noclip(player);
			return true;
		}
		// tp to level
		int level = Integer.MIN_VALUE;
		int i = 1;
		if (numargs > 1 && NumberUtils.IsNumeric(args[1])) {
			level = Integer.parseInt(args[1]);
			if (!this.plugin.isValidLevel(level)) {
				sender.sendMessage(this.chatPrefix + "Invalid backrooms level: " + Integer.toString(level));
				return true;
			}
			i = 2;
		}
		// tp players
		if (numargs > i) {
			if (player != null && !player.hasPermission("backrooms.tp.others")) {
				player.sendMessage(this.chatPrefix + "You don't have permission to use this.");
				return true;
			}
			for (; i<numargs; i++) {
				final Player p = Bukkit.getPlayer(args[i]);
				if (p == null) {
					sender.sendMessage(this.chatPrefix + "Unknown player: " + args[i]);
				} else {
					this.plugin.noclip(p, level);
				}
			}
		// tp self
		} else {
			if (player == null) {
				sender.sendMessage(this.logPrefix + "Cannot teleport");
			} else {
				this.plugin.noclip(player, level);
			}
		}
		return true;
	}



}
