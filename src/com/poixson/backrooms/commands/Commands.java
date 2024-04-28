package com.poixson.backrooms.commands;

import java.io.Closeable;

import com.poixson.backrooms.BackroomsPlugin;


public class Commands implements Closeable {

	protected final Command_NoClip cmd_noclip; // /noclip



	public Commands(final BackroomsPlugin plugin) {
		this.cmd_noclip = new Command_NoClip(plugin); // /noclip
	}



	@Override
	public void close() {
		this.cmd_noclip.close();
	}



}
