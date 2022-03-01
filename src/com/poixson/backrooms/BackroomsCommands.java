package com.poixson.backrooms;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.poixson.commonbukkit.tools.commands.pxnBukkitCommand;
import com.poixson.commonbukkit.tools.commands.pxnBukkitCommandsHelper;


public class BackroomsCommands extends pxnBukkitCommandsHelper {



	public BackroomsCommands(final BackroomsPlugin plugin) {
		super(plugin, "backrooms");
		// /backrooms reload
		this.addCommand(new cmdReload());
	}



	// /worldsq reload
	public static class cmdReload extends pxnBukkitCommand {
		public cmdReload() {
			super("reload");
		}
		@Override
		public boolean run(final CommandSender sender,
				final Command cmd, final String[] args) {
			
BackroomsPlugin.log.warning("RELOAD COMMAND");
			
			return true;
		}
	}



}
