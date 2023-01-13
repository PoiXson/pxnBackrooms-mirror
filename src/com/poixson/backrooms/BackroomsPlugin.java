package com.poixson.backrooms;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.poixson.backrooms.commands.Commands;
import com.poixson.backrooms.levels.BackroomsLevel;
import com.poixson.backrooms.levels.Level_000;
import com.poixson.backrooms.levels.Level_009;
import com.poixson.backrooms.levels.Level_010;
import com.poixson.backrooms.levels.Level_011;
import com.poixson.backrooms.levels.Level_078;
import com.poixson.backrooms.levels.Level_151;
import com.poixson.backrooms.levels.Level_771;
import com.poixson.backrooms.levels.Level_866;
import com.poixson.backrooms.listeners.ItemDespawnListener;
import com.poixson.backrooms.listeners.PlayerDamageListener;
import com.poixson.backrooms.listeners.PlayerMoveListener;
import com.poixson.commonbukkit.pxnCommonPlugin;
import com.poixson.tools.AppProps;
import com.poixson.utils.Utils;


public class BackroomsPlugin extends JavaPlugin {
	public static final String LOG_PREFIX  = "[Backrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + LOG_PREFIX + ChatColor.WHITE;
	public static final Logger log = Logger.getLogger("Minecraft");
//TODO
	public static final int SPIGOT_PLUGIN_ID = 0;
	public static final int BSTATS_PLUGIN_ID = 17231;

	public static final String GENERATOR_NAME = "Backrooms";

	protected static final AtomicReference<BackroomsPlugin> instance = new AtomicReference<BackroomsPlugin>(null);
	protected static final AtomicReference<Metrics>         metrics  = new AtomicReference<Metrics>(null);
	protected final AppProps props;

	// world generators
	protected final HashMap<Integer, BackroomsLevel> backlevels = new HashMap<Integer, BackroomsLevel>();

	// chance to teleport to levels
	protected final AtomicReference<TeleportManager> tpManager = new AtomicReference<TeleportManager>(null);

	// listeners
	protected final AtomicReference<Commands>             commandListener      = new AtomicReference<Commands>(null);
	protected final AtomicReference<PlayerMoveListener>   playerMoveListener   = new AtomicReference<PlayerMoveListener>(null);
	protected final AtomicReference<PlayerDamageListener> playerDamageListener = new AtomicReference<PlayerDamageListener>(null);
	protected final AtomicReference<ItemDespawnListener>  itemDespawnListener  = new AtomicReference<ItemDespawnListener>(null);



	public BackroomsPlugin() {
		try {
			this.props = AppProps.LoadFromClassRef(BackroomsPlugin.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		// create worlds (after server starts)
		(new BukkitRunnable() {
			@Override
			public void run() {
				// note: this is converting long to string
				final String seed = Long.toString( Bukkit.getWorld("world").getSeed() );
				MakeWorld(  0, seed); // lobby
				MakeWorld(  9, seed); // suburbs
				MakeWorld( 10, seed); // field of wheat
				MakeWorld( 11, seed); // city
				MakeWorld( 78, seed); // space
				MakeWorld(151, seed); // dollhouse
				MakeWorld(771, seed); // crossroads
				MakeWorld(866, seed); // dirtfield
			}
		}).runTask(this);
		// commands listener
		{
			final Commands listener = new Commands(this);
			final Commands previous = this.commandListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// load teleport chance
		this.tpManager.set(TeleportManager.Load(this));
		// player move listener
		{
			final PlayerMoveListener listener = new PlayerMoveListener(this);
			final PlayerMoveListener previous = this.playerMoveListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// player damage listener
		{
			final PlayerDamageListener listener = new PlayerDamageListener(this);
			final PlayerDamageListener previous = this.playerDamageListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// item despawn listener
		{
			final ItemDespawnListener listener = new ItemDespawnListener(this);
			final ItemDespawnListener previous = this.itemDespawnListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// bStats
		System.setProperty("bstats.relocatecheck","false");
		metrics.set(new Metrics(this, BSTATS_PLUGIN_ID));
		// update checker
		pxnCommonPlugin.GetPlugin()
			.getUpdateCheckManager()
				.addPlugin(this, SPIGOT_PLUGIN_ID, this.getPluginVersion());
	}

	@Override
	public void onDisable() {
		// update checker
		pxnCommonPlugin.GetPlugin()
			.getUpdateCheckManager()
				.removePlugin(SPIGOT_PLUGIN_ID);
		// unload generators
		for (final BackroomsLevel lvl : this.backlevels.values()) {
			lvl.unload();
		}
		this.backlevels.clear();
		// commands listener
		{
			final Commands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// stop schedulers
		try {
			Bukkit.getScheduler()
				.cancelTasks(this);
		} catch (Exception ignore) {}
		// stop listeners
		HandlerList.unregisterAll(this);
		if (!instance.compareAndSet(this, null))
			throw new RuntimeException("Disable wrong instance of plugin?");
	}



	public static MultiverseCore GetMVCore() {
		final PluginManager pm = Bukkit.getServer().getPluginManager();
		final MultiverseCore mvcore = (MultiverseCore) pm.getPlugin("Multiverse-Core");
		if (mvcore == null) throw new RuntimeException("Multiverse-Core plugin not found");
		return mvcore;
	}

	protected static void MakeWorld(final int level, final String seed) {
		final MVWorldManager manager = GetMVCore().getMVWorldManager();
		final String name = "level" + Integer.toString(level);
		if (!manager.isMVWorld(name, false)) {
			log.warning(LOG_PREFIX+"Creating backrooms level: "+Integer.toString(level));
			final Environment env;
			switch (level) {
			case 78: env = Environment.THE_END; break;
			default: env = Environment.NORMAL;  break;
			}
			if (!manager.addWorld(name, env, seed, WorldType.NORMAL, false, "pxnBackrooms", true))
				throw new RuntimeException("Failed to create world: "+name);
			final MultiverseWorld mvworld = manager.getMVWorld(name, false);
			final World world = mvworld.getCBWorld();
			mvworld.setAlias("backrooms");
			mvworld.setHidden(true);
			mvworld.setKeepSpawnInMemory(false);
			mvworld.setAllowAnimalSpawn(true);
			mvworld.setAllowMonsterSpawn(true);
			mvworld.setAutoHeal(true);
			mvworld.setBedRespawn(true);
			mvworld.setDifficulty(Difficulty.HARD);
			mvworld.setHunger(true);
			mvworld.setPVPMode(true);
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, Boolean.TRUE );
			world.setGameRule(GameRule.DO_WEATHER_CYCLE,  Boolean.FALSE);
			world.setGameRule(GameRule.KEEP_INVENTORY,    Boolean.TRUE );
			world.setGameRule(GameRule.MOB_GRIEFING,      Boolean.FALSE);
		}
	}



	public int getLevel(final Player player) {
		if (player == null)
			return Integer.MIN_VALUE;
		final World world = player.getWorld();
		return this.getLevel(world);
	}
	public int getLevel(final World world) {
		if (world == null)
			return Integer.MIN_VALUE;
		return this.getLevel(world.getName());
	}
	public int getLevel(final String worldName) {
		if (worldName == null || worldName.isEmpty())
			return Integer.MIN_VALUE;
		if (!worldName.startsWith("level"))
			return Integer.MIN_VALUE;
		final int level = Integer.parseInt(worldName.substring(5));
		if (!isValidLevel(level))
			return Integer.MIN_VALUE;
		return level;
	}

	public void assertValidLevel(final int level) {
		if (!this.isValidLevel(level))
			throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
	}
	public boolean isValidLevel(final int level) {
		switch (level) {
		case 0:
		case 1:
		case 5:
		case 9:
		case 10:
		case 11:
		case 19:
		case 37:
		case 78:
		case 151:
		case 309:
		case 771:
		case 866:
			return true;
		default: break;
		}
		return false;
	}



	public World getLevelWorld(final int level) {
		final String worldName = this.getLevelWorldName(level);
		if (Utils.isEmpty(worldName))
			return null;
		return Bukkit.getWorld(worldName);
	}
	public String getLevelWorldName(final int level) {
		switch (level) {
		case 1:
		case 0:
		case 37:
		case 5:
		case 19:
		case 309: return "level0";
		case 9:   return "level9";
		case 10:  return "level10";
		case 11:  return "level11";
		case 78:  return "level78";
		case 151: return "level151";
		case 771: return "level771";
		case 866: return "level866";
		default: break;
		}
		return null;
	}



	public void noclip(final Player player, final int level) {
		if (level == Integer.MIN_VALUE) {
			this.noclip(player);
			return;
		}
		final TeleportManager manager = this.tpManager.get();
		Location loc = manager.getSpawnLocation(level);
		if (loc == null) {
			final World world = this.getLevelWorld(level);
			loc = world.getSpawnLocation();
		}
		log.info(LOG_PREFIX+"No-clip player: "+player.getName()+" to level: "+Integer.toString(level));
		player.teleport(loc);
	}
	public int noclip(final Player player) {
		final int levelFrom = this.getLevel(player);
		final int levelTo = this.noclip(levelFrom);
		this.noclip(player, levelTo);
		return levelTo;
	}
	public int noclip(final int level_from) {
		final TeleportManager manager = this.tpManager.get();
		if (manager == null) {
			log.warning(LOG_PREFIX+"teleport chance weights not loaded");
			return 0;
		}
		return manager.getDestinationLevel(level_from);
	}



	// -------------------------------------------------------------------------------



	public BackroomsLevel getBackroomsLevel(final int level) {
		// existing generator
		{
			final BackroomsLevel lvl = this.backlevels.get(Integer.valueOf(level));
			if (lvl != null)
				return lvl;
		}
		// new generator instance
		{
			final BackroomsLevel lvl;
			switch (level) {
			case 1:
			case 37:
			case 5:
			case 19:
			case 309: return this.getBackroomsLevel(0);
			case   0: lvl = new Level_000(this); break;
			case   9: lvl = new Level_009(this); break;
			case  10: lvl = new Level_010(this); break;
			case  11: lvl = new Level_011(this); break;
			case  78: lvl = new Level_078(this); break;
			case 151: lvl = new Level_151(this); break;
			case 771: lvl = new Level_771(this); break;
			case 866: lvl = new Level_866(this); break;
			default: throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
			}
			final BackroomsLevel existing = this.backlevels.putIfAbsent(Integer.valueOf(level), lvl);
			if (existing != null)
				return existing;
			return lvl;
		}
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
		if (!worldName.startsWith("level"))
			throw new RuntimeException("Invalid world name, must be level# found: "+worldName);
		log.info(String.format("%s%s world: %s", LOG_PREFIX, GENERATOR_NAME, worldName));
		final int level = this.getLevel(worldName);
		return this.getBackroomsLevel(level);
	}



	public String getPluginVersion() {
		return this.props.version;
	}



}
