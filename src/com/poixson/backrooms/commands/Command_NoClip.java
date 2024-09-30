package com.poixson.backrooms.commands;

import static com.poixson.backrooms.BackroomsPlugin.CHAT_PREFIX;
import static com.poixson.utils.NumberUtils.IsNumeric;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.commands.pxnCommandRoot;


// /noclip
public class Command_NoClip extends pxnCommandRoot {

	protected final BackroomsPlugin plugin;



	public Command_NoClip(final BackroomsPlugin plugin) {
		super(plugin,
			"backrooms", // namespace
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
		final int num_args = args.length;
		// no-clip self
		if (num_args == 0) {
			if (player == null)
				return false;
			final int level_from = this.plugin.getLevel(player);
			// from frontrooms
			if (level_from < 0) {
				if (!player.hasPermission("backrooms.cmd.noclip.front")) {
					if (player.hasPermission("backrooms.cmd.noclip.back")) {
						sender.sendMessage("You don't have permission to use this command here.");
						return true;
					}
					return false;
				}
			// from backrooms
			} else {
				if (!player.hasPermission("backrooms.cmd.noclip.back")) {
					if (player.hasPermission("backrooms.cmd.noclip.front")) {
						sender.sendMessage("You don't have permission to use this command here.");
						return true;
					}
					return false;
				}
			}
			this.plugin.noclip(player);
			return true;
		} // end 0 args
		// no-clip to level
		int level = Integer.MIN_VALUE;
		if (IsNumeric(args[0])) {
			if (player != null
			&& !player.hasPermission("backrooms.cmd.noclip.specific"))
				return false;
			final int lvl = Integer.parseInt(args[0]);
			if (!this.plugin.isValidLevel(lvl)) {
				sender.sendMessage(String.format("%sInvalid backrooms level: %s", CHAT_PREFIX, args[0]));
				return true;
			}
			level = lvl;
		}
		// no-clip self
		if (num_args == 1 && level >= 0) {
			if (player == null)
				return false;
			final int level_from = this.plugin.getLevel(player);
			// from frontrooms
			if (level_from < 0) {
				if (!player.hasPermission("backrooms.cmd.noclip.front")) {
					if (player.hasPermission("backrooms.cmd.noclip.back")) {
						sender.sendMessage("You don't have permission to use this command here.");
						return true;
					}
					return false;
				}
			// from backrooms
			} else {
				if (!player.hasPermission("backrooms.cmd.noclip.back")) {
					if (player.hasPermission("backrooms.cmd.noclip.front")) {
						sender.sendMessage("You don't have permission to use this command here.");
						return true;
					}
					return false;
				}
			}
			this.plugin.noclip(player, level);
		// no-clip others
		} else {
			if (player != null
			&& !player.hasPermission("backrooms.cmd.noclip.others"))
				return false;
			int index = 0;
			if (level >= 0)
				index++;
			for (; index<num_args; index++) {
				final Player p = Bukkit.getPlayer(args[index]);
				if (p == null) sender.sendMessage(String.format("%sUnknown player: %s", CHAT_PREFIX, args[index]));
				else           this.plugin.noclip(p, level);
			}
		}
		return true;
	}



	@Override
	public List<String> onTabComplete(final CommandSender sender, final String[] args) {
		final LinkedList<String> list = new LinkedList<String>();
		final int[] levels = this.plugin.getAllLevels();
		for (final int level : levels) {
			final String str = Integer.toString(level);
			if (str.startsWith(args[0]))
				list.add(str);
		}
		return list;
	}



}
