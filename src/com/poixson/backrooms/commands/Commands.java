package com.poixson.backrooms.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.commands.pxnCommandsHandler;


public class Commands extends pxnCommandsHandler {



	public Commands(final BackroomsPlugin plugin) {
		super(
			plugin,
			"backrooms"
		);
		this.addCommand(new Command_TP(plugin));
	}



	@Override
	public List<String> onTabComplete(
			final CommandSender sender, final Command cmd,
			final String label, final String[] args) {
		final List<String> matches = new ArrayList<String>();
		final int size = args.length;
		switch (size) {
		case 1:
			if ("tp".startsWith(args[0])) matches.add("tp");
			break;
		case 2:
			if ("tp".equals(args[0])) {
				final int[] levels = ((BackroomsPlugin)this.plugin).getLevels();
				for (final int lvl : levels) {
					final String str = Integer.toString(lvl);
					if (str.startsWith(args[1]))
						matches.add(str);
				}
				String name;
				for (final Player player : Bukkit.getOnlinePlayers()) {
					name = player.getName();
					if (name.startsWith(args[1]))
						matches.add(name);
				}
			}
			break;
		default:
		}
		return matches;
	}



}
