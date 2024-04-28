package com.poixson.backrooms.commands;

import static com.poixson.backrooms.BackroomsPlugin.CHAT_PREFIX;
import static com.poixson.utils.NumberUtils.IsNumeric;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.commands.pxnCommandRoot;


public class Command_NoClip extends pxnCommandRoot {

	protected final BackroomsPlugin plugin;



	public Command_NoClip(final BackroomsPlugin plugin) {
		super(plugin,
			"No-Clip into the backrooms.", // desc
			null, // usage
			null, // perm
			new String[] {
				"noclip",
				"no-clip"
			}
		);
		this.plugin = plugin;
	}



	@Override
	public boolean onCommand(final CommandSender sender,
			final Command command, final String label, final String[] args) {
		final Player player = (sender instanceof Player ? (Player)sender : null);
		if (player != null && !player.hasPermission("backrooms.tp")) {
			player.sendMessage(CHAT_PREFIX+"You don't have permission to use this.");
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
				sender.sendMessage(String.format("%sInvalid backrooms level: %d", CHAT_PREFIX, Integer.valueOf(level)));
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
				if (p == null) sender.sendMessage(String.format("%sUnknown player: %s", CHAT_PREFIX, args[i]));
				else           this.plugin.noclip(p, level);
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



	@Override
	public List<String> onTabComplete(final CommandSender sender, final String[] args) {
//TODO
System.out.println("TAB:"); for (final String arg : args) System.out.println("  "+arg);
return null;
	}



}
