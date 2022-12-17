package com.poixson.backrooms.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.NumberUtils;


public class BackroomsCommands implements CommandExecutor {
	public static final String CHAT_PREFIX = BackroomsPlugin.CHAT_PREFIX;

	protected final BackroomsPlugin plugin;

	protected final ArrayList<PluginCommand> cmds = new ArrayList<PluginCommand>();



	public BackroomsCommands(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}



	public void register() {
		final PluginCommand cmd = this.plugin.getCommand("backrooms");
		cmd.setExecutor(this);
		this.cmds.add(cmd);
		final BackroomsTabCompleter completer = new BackroomsTabCompleter();
		cmd.setTabCompleter(completer);
	}
	public void unregister() {
		for (final PluginCommand cmd : this.cmds) {
			cmd.setExecutor(null);
		}
		this.cmds.clear();
	}



	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
			final String label, final String[] args) {
		final Player player = (sender instanceof Player ? (Player)sender : null);
		final int numargs = args.length;
		if (numargs >= 1) {
			switch (args[0]) {
			case "tp":
				if (player != null && !player.hasPermission("backrooms.tp")) {
					player.sendMessage(CHAT_PREFIX+"You don't have permission to use this.");
					return true;
				}
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
						sender.sendMessage(CHAT_PREFIX+"Invalid backrooms level: "+Integer.toString(level));
						return true;
					}
					i = 2;
				}
				// tp players
				if (numargs > i) {
					if (player != null && !player.hasPermission("backrooms.tp.others")) {
						player.sendMessage(CHAT_PREFIX+"You don't have permission to use this.");
						return true;
					}
					for (; i<numargs; i++) {
						final Player p = Bukkit.getPlayer(args[i]);
						if (p == null) {
							sender.sendMessage(CHAT_PREFIX+"Unknown player: "+args[i]);
						} else {
							this.plugin.noclip(p, level);
						}
					}
				// tp self
				} else {
					if (player == null) {
						sender.sendMessage(CHAT_PREFIX+"Cannot teleport");
					} else {
						this.plugin.noclip(player, level);
					}
				}
				return true;
			}
		}
		return false;
	}



}
