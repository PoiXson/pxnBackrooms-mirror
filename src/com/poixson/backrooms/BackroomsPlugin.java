package com.poixson.backrooms;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.poixson.backrooms.listeners.PlayerDamageListener;
import com.poixson.scriptkit.api.ChunkGeneratorFactory;
import com.poixson.scriptkit.api.ScriptChunkGenerator;
import com.poixson.scriptkit.api.ScriptKitAPI;
import com.poixson.utils.Utils;


public class BackroomsPlugin extends JavaPlugin implements ChunkGeneratorFactory {
	public static final String LOG_PREFIX  = "[Backrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");

	public static final String GENERATOR_NAME = "Backrooms";

	protected static final AtomicReference<BackroomsPlugin> instance = new AtomicReference<BackroomsPlugin>(null);
	protected final AtomicReference<String> worldName = new AtomicReference<String>(null);

	// listeners
	protected final AtomicReference<BackroomsCommands>   commandListener = new AtomicReference<BackroomsCommands>(null);
	protected final AtomicReference<PlayerDamageListener> damageListener = new AtomicReference<PlayerDamageListener>(null);



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		{
			final File path = new File(this.getDataFolder(), "scripts");
			if (!path.isDirectory()) {
				log.info(CHAT_PREFIX + "Creating directory: " + path.toString());
				path.mkdirs();
			}
		}
		// world generator
		ScriptKitAPI.GetAPI()
			.addGenFactory(GENERATOR_NAME, this);
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
	}

	@Override
	public void onDisable() {
		// world generator
		ScriptKitAPI.GetAPI()
			.removeGenFactory(GENERATOR_NAME);
		// commands listener
		{
			final BackroomsCommands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// stop listeners
		HandlerList.unregisterAll(this);
		this.damageListener.set(null);
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		if (!instance.compareAndSet(this, null))
			throw new RuntimeException("Disable wrong instance of plugin?");
	}



	// -------------------------------------------------------------------------------
	// chunk generator



	// factory
	@Override
	public ScriptChunkGenerator newInstance(final String worldName, final String argsStr) {
		log.info(
			String.format(
				"%sWorld <%s> using generator <%s> %s",
				LOG_PREFIX,
				worldName,
				GENERATOR_NAME,
				argsStr
			)
		);
//TODO
		final String pathLoc = (new File(this.getDataFolder(), "scripts")).toString();
		final String pathRes = "scripts";
		final String filename = "backrooms.js";
		final ScriptChunkGenerator gen =
			new ScriptChunkGenerator(
				this,
				worldName, argsStr,
				pathLoc, pathRes,
				filename
			);
		return gen;
	}



	// generator
	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		log.info(String.format("%sWorld <%s> using generator <%s> %s",
			LOG_PREFIX, worldName, GENERATOR_NAME, argsStr));
		this.worldName.set(worldName);
		return ScriptKitAPI.GetAPI()
				.getChunkGenerator(GENERATOR_NAME, worldName, argsStr);
	}



	// -------------------------------------------------------------------------------



	public String getBackroomsWorldName() {
		return this.worldName.get();
	}

	public boolean isBackroomsWorld(final Player player) {
		if (player == null) throw new NullPointerException();
		return this.isBackroomsWorld(player.getWorld());
	}
	public boolean isBackroomsWorld(final World world) {
		if (world == null) throw new NullPointerException();
		return this.isBackroomsWorld(world.getName());
	}
	public boolean isBackroomsWorld(final String worldName) {
		if (Utils.isEmpty(worldName)) throw new NullPointerException();
		return ScriptKitAPI.GetAPI()
				.hasChunkGenerator(worldName);
	}



}
