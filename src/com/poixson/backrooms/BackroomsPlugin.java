package com.poixson.backrooms;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.backrooms.generators.BackGen_000;
import com.poixson.backrooms.generators.BackGen_009;
import com.poixson.backrooms.generators.BackGen_011;
import com.poixson.backrooms.generators.BackGen_309;
import com.poixson.backrooms.generators.BackroomsGenerator;


public class BackroomsPlugin extends JavaPlugin {
	public static final String LOG_PREFIX  = "[Backrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");

	public static final String GENERATOR_NAME = "Backrooms";

	protected static final AtomicReference<BackroomsPlugin> instance = new AtomicReference<BackroomsPlugin>(null);
	protected final AtomicBoolean enableScripts = new AtomicBoolean(false);
//	protected final AtomicReference<String> worldName = new AtomicReference<String>(null);

	// world generators
	protected final HashMap<Integer, BackroomsGenerator> generators = new HashMap<Integer, BackroomsGenerator>();

//	// listeners
//	protected final AtomicReference<BackroomsCommands>   commandListener = new AtomicReference<BackroomsCommands>(null);
//	protected final AtomicReference<PlayerDamageListener> damageListener = new AtomicReference<PlayerDamageListener>(null);



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		{
			final File path = new File(this.getDataFolder(), "scripts");
			this.enableScripts.set(
				path.isDirectory()
			);
		}
/*
		// commands listener
		{
			final BackroomsCommands listener = new BackroomsCommands(this);
			final BackroomsCommands previous = this.commandListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// player damage listener
		{
			final PlayerDamageListener listener = new PlayerDamageListener(this);
			final PlayerDamageListener previous = this.damageListener.getAndSet(listener);
			final PluginManager pm = Bukkit.getPluginManager();
			if (previous != null)
				HandlerList.unregisterAll(previous);
			pm.registerEvents(listener, this);
		}
*/
//		final PluginManager pm = Bukkit.getPluginManager();
//		pm.registerEvents(new DecayListener(), this);
	}

	@Override
	public void onDisable() {
		// unload generators
		for (final BackroomsGenerator gen : this.generators.values()) {
			gen.unload();
		}
		this.generators.clear();
/*
		// commands listener
		{
			final BackroomsCommands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
*/
		// stop listeners
		HandlerList.unregisterAll(this);
//		this.damageListener.set(null);
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		if (!instance.compareAndSet(this, null))
			throw new RuntimeException("Disable wrong instance of plugin?");
	}



	// -------------------------------------------------------------------------------



	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		if (!worldName.startsWith("level"))
			throw new RuntimeException("Invalid world name, must be level#");
		log.info(String.format("%sWorld <%s> using generator <%s> %s",
			LOG_PREFIX, worldName, GENERATOR_NAME, argsStr));
		final int level = Integer.parseInt(worldName.substring(5));
		// existing generator
		{
			final BackroomsGenerator gen = this.generators.get(Integer.valueOf(level));
			if (gen != null)
				return gen;
		}
		// new generator instance
		{
			final BackroomsGenerator gen;
			switch (level) {
			case 0:   gen = new BackGen_000(this); break;
			case 9:   gen = new BackGen_009(this); break;
			case 11:  gen = new BackGen_011(this); break;
			case 309: gen = new BackGen_309(this); break;
			default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			}
			final BackroomsGenerator existing = this.generators.putIfAbsent(Integer.valueOf(level), gen);
			if (existing != null)
				return existing;
			return gen;
		}
	}



//	public String getBackroomsWorldName() {
//		return this.worldName.get();
//	}

/*
	public boolean isBackroomsWorld(final Player player) {
		if (player == null) throw new NullPointerException();
		return this.isBackroomsWorld(player.getWorld());
	}
	public boolean isBackroomsWorld(final World world) {
		if (world == null) throw new NullPointerException();
		return this.isBackroomsWorld(world.getName());
	}
	public boolean isBackroomsWorld(final String worldName) {
//TODO
return false;
//		if (Utils.isEmpty(worldName)) throw new NullPointerException();
//		return ScriptKitAPI.GetAPI()
//				.hasChunkGenerator(worldName);
	}
*/



}
