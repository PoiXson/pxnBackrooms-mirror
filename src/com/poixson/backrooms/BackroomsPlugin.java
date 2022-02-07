package com.poixson.backrooms;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


public class BackroomsPlugin extends JavaPlugin {
	public static final String LOG_PREFIX  = "[Backrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");

	protected static final AtomicReference<BackroomsPlugin> instance = new AtomicReference<BackroomsPlugin>(null);

	// listeners
	protected final AtomicReference<BackroomsCommands> commandListener = new AtomicReference<BackroomsCommands>(null);



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		// commands listener
		{
			final BackroomsCommands listener = new BackroomsCommands(this);
			final BackroomsCommands previous = this.commandListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
	}

	@Override
	public void onDisable() {
		// commands listener
		{
			final BackroomsCommands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// stop listeners
		HandlerList.unregisterAll(this);
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		if (!instance.compareAndSet(this, null))
			throw new RuntimeException("Disable wrong instance of plugin?");
	}



}
