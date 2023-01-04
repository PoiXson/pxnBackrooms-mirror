package com.poixson.backrooms.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonbukkit.tools.commands.pxnCommandsHandler;


public class Commands extends pxnCommandsHandler {



	public Commands(final BackroomsPlugin plugin) {
		super(
			plugin,
			"backrooms"
		);
		this.addCommand(new CommandTP(plugin));
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
				final List<String> levels = new ArrayList<String>();
				levels.add("0");
				levels.add("1");
				levels.add("5");
				levels.add("9");
				levels.add("10");
				levels.add("11");
				levels.add("19");
				levels.add("37");
				levels.add("78");
				levels.add("151");
				levels.add("309");
				levels.add("771");
				levels.add("866");
				for (final String lvl : levels) {
					if (lvl.startsWith(args[1]))
						matches.add(lvl);
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
