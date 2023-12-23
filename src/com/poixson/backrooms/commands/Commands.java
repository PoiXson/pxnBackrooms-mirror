package com.poixson.backrooms.commands;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.commands.pxnCommandsHandler;


public class Commands extends pxnCommandsHandler<BackroomsPlugin> {



	public Commands(final BackroomsPlugin plugin) {
		super(plugin, "backrooms");
		this.addCommand(new Command_TP(plugin));
	}



}
