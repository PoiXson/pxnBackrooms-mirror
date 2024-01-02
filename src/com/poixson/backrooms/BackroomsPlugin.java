package com.poixson.backrooms;

import static com.poixson.utils.Utils.IsEmpty;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.commands.Commands;
import com.poixson.backrooms.dynmap.GeneratorPerspective;
import com.poixson.backrooms.gens.Gen_000;
import com.poixson.backrooms.gens.Gen_001;
import com.poixson.backrooms.gens.Gen_004;
import com.poixson.backrooms.gens.Gen_005;
import com.poixson.backrooms.gens.Gen_006;
import com.poixson.backrooms.gens.Gen_011;
import com.poixson.backrooms.gens.Gen_019;
import com.poixson.backrooms.gens.Gen_023;
import com.poixson.backrooms.gens.Gen_033;
import com.poixson.backrooms.gens.Gen_037;
import com.poixson.backrooms.gens.Gen_040;
import com.poixson.backrooms.gens.Gen_094;
import com.poixson.backrooms.gens.Gen_188;
import com.poixson.backrooms.gens.Gen_308;
import com.poixson.backrooms.gens.Gen_309;
import com.poixson.backrooms.gens.Gen_771;
import com.poixson.backrooms.listeners.Listener_OutOfWorld;
import com.poixson.backrooms.listeners.Listener_PlayerDamage;
import com.poixson.backrooms.tasks.QuoteAnnouncer;
import com.poixson.backrooms.tasks.TaskReconvergence;
import com.poixson.backrooms.tasks.TeleportManager;
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_011;
import com.poixson.backrooms.worlds.Level_033;
import com.poixson.backrooms.worlds.Level_094;
import com.poixson.backrooms.worlds.Level_771;
import com.poixson.tools.DelayedChestFiller;
import com.poixson.tools.xJavaPlugin;


public class BackroomsPlugin extends xJavaPlugin {
	@Override public int getSpigotPluginID() { return 108148; }
	@Override public int getBStatsID() {       return 17231;  }
	public static final String LOG_PREFIX  = "[pxnBackrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;

	public static final String GENERATOR_NAME = "pxnBackrooms";
	protected static final String DEFAULT_RESOURCE_PACK = "https://dl.poixson.com/mcplugins/pxnBackrooms/pxnBackrooms-resourcepack-{VERSION}.zip";
//	protected static final String DEFAULT_RESOURCE_PACK = "https://backrooms.poixson.com/pxnBackrooms-resourcepack.zip";
	protected static final int DEFAULT_SPAWN_DISTANCE = 10000;

	// backrooms levels
	protected final HashMap<Integer, BackroomsLevel> backlevels = new HashMap<Integer, BackroomsLevel>();
	protected final ConcurrentHashMap<UUID, CopyOnWriteArraySet<Integer>> visitLevels = new ConcurrentHashMap<UUID, CopyOnWriteArraySet<Integer>>();

	// reconvergence task
	protected final AtomicReference<TaskReconvergence> taskReconvergence = new AtomicReference<TaskReconvergence>(null);

	// quotes
	protected final AtomicReference<QuoteAnnouncer> quoteAnnouncer = new AtomicReference<QuoteAnnouncer>(null);

	// chance to teleport to levels
	protected final AtomicReference<TeleportManager> tpManager = new AtomicReference<TeleportManager>(null);

	// listeners
	protected final AtomicReference<Commands> commands = new AtomicReference<Commands>(null);
	protected final AtomicReference<Listener_PlayerDamage> listenerPlayerDamage = new AtomicReference<Listener_PlayerDamage>(null);
	protected final AtomicReference<Listener_OutOfWorld>   listenerOutOfWorld   = new AtomicReference<Listener_OutOfWorld>(null);

	// dynmap config generator
	protected final AtomicReference<GeneratorPerspective> dynmap_perspective = new AtomicReference<GeneratorPerspective>(null);



	public BackroomsPlugin() {
		super(BackroomsPlugin.class);
	}



	@Override
	public void onEnable() {
		super.onEnable();
		// resource pack
		{
			final String pack = Bukkit.getResourcePack();
			if (pack == null || pack.isEmpty()) {
				this.log().warning(String.format(
					"%sResource pack not set; You can use this one: %s",
					LOG_PREFIX,
					DEFAULT_RESOURCE_PACK.replace("{VERSION}", this.getPluginVersion())
				));
			} else {
				this.log().info(String.format(
					"%sUsing resource pack: %s",
					LOG_PREFIX,
					Bukkit.getResourcePack()
				));
			}
		}
		// backrooms levels
		new Level_000(this); // lobby, windows, overgrowth, lights out, basement, hotel, attic, poolrooms, radio station
//		new Level_007(this); // thalassophobia
//		new Level_009(this); // suburbs
//		new Level_010(this); // field of wheat
		new Level_011(this); // concrete jungle, arcade, ikea, abandoned office
		new Level_033(this); // run for your life
//		new Level_078(this); // space
		new Level_094(this); // motion
//		new Level_151(this); // dollhouse
		new Level_771(this); // crossroads
//		new Level_866(this); // dirtfield
		if (this.enableDynmapConfigGen())
			this.getDynmapPerspective().commit( new File(this.getDataFolder(), "../dynmap/") );
//TODO: enable in config
		// create worlds (after server starts)
		(new BukkitRunnable() {
			@Override
			public void run() {
//TODO: this is converting long to string
				final String seed = Long.toString( Bukkit.getWorld("world").getSeed() );
				final Iterator<Entry<Integer, BackroomsLevel>> it = BackroomsPlugin.this.backlevels.entrySet().iterator();
				while (it.hasNext()) {
					final Entry<Integer, BackroomsLevel> entry = it.next();
					final int level = entry.getKey().intValue();
					if (entry.getValue().isWorldMain(level))
						BackroomsLevel.MakeWorld(level, seed);
				}
			}
		}).runTask(this);
		// register levels
		for (final BackroomsLevel level : this.backlevels.values())
			level.register();
		// commands listener
		{
			final Commands listener = new Commands(this);
			final Commands previous = this.commands.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// load teleport chance
		this.tpManager.set(TeleportManager.Load(this));
		// load quotes
		this.quoteAnnouncer.set(QuoteAnnouncer.Load(this));
		// reconvergence task
		{
			final ConfigurationSection cfg = this.config.get().getConfigurationSection("Reconvergence");
			final TaskReconvergence task = new TaskReconvergence(this, cfg);
			final TaskReconvergence previous = this.taskReconvergence.getAndSet(task);
			if (previous != null)
				previous.stop();
			task.start();
		}
		// player damage listeners
		{
			final Listener_PlayerDamage listener = new Listener_PlayerDamage(this);
			final Listener_PlayerDamage previous = this.listenerPlayerDamage.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// out of world listeners
		{
			final Listener_OutOfWorld listener = new Listener_OutOfWorld(this);
			final Listener_OutOfWorld previous = this.listenerOutOfWorld.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		// reconvergence task
		{
			final TaskReconvergence task = this.taskReconvergence.getAndSet(null);
			if (task != null)
				task.stop();
		}
		// finish filling chests
		DelayedChestFiller.stop();
		// unload levels
		for (final BackroomsLevel lvl : this.backlevels.values()) {
			lvl.unregister();
		}
		this.backlevels.clear();
		// commands listener
		{
			final Commands listener = this.commands.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// out of world listeners
		{
			final Listener_OutOfWorld listener = this.listenerOutOfWorld.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// player damage listeners
		{
			final Listener_PlayerDamage listener = this.listenerPlayerDamage.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// teleport chance
		this.tpManager.set(null);
		this.dynmap_perspective.set(null);
		// quotes
		this.quoteAnnouncer.set(null);
	}



	// -------------------------------------------------------------------------------
	// configs



	@Override
	protected void loadConfigs() {
		this.mkPluginDir();
		// config.yml
		{
			final FileConfiguration cfg = this.getConfig();
			this.config.set(cfg);
			this.configDefaults(cfg);
			cfg.options().copyDefaults(true);
			super.saveConfig();
		}
		// levels-visited.yml
		{
			final File file = new File(this.getDataFolder(), "levels-visited.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			final Set<String> keys = cfg.getKeys(false);
			for (final String key : keys) {
				final UUID uuid = UUID.fromString(key);
				final CopyOnWriteArraySet<Integer> visited = new CopyOnWriteArraySet<Integer>();
				visited.addAll(cfg.getIntegerList(key));
				this.visitLevels.put(uuid, visited);
			}
		}
	}
	@Override
	protected void saveConfigs() {
		// config.yml
		super.saveConfig();
		// levels-visited.yml
		{
			final File file = new File(this.getDataFolder(), "levels-visited.yml");
			final FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			final Iterator<Entry<UUID, CopyOnWriteArraySet<Integer>>> it = this.visitLevels.entrySet().iterator();
			while (it.hasNext()) {
				final Entry<UUID, CopyOnWriteArraySet<Integer>> entry = it.next();
				final String uuid = entry.getKey().toString();
				final HashSet<Integer> set = new HashSet<Integer>();
				set.addAll(entry.getValue());
				cfg.set(uuid, set);
			}
			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	protected void configDefaults(final FileConfiguration cfg) {
		TaskReconvergence.ConfigDefaults(cfg);
		cfg.addDefault("Enable Dynmap Config Gen", Boolean.FALSE);
		cfg.addDefault("Spawn Distance", Integer.valueOf(DEFAULT_SPAWN_DISTANCE));
		Gen_000.ConfigDefaults(cfg); // lobby
		Gen_001.ConfigDefaults(cfg); // basement
//		Gen_002.ConfigDefaults(cfg); // pipe dreams
		Gen_004.ConfigDefaults(cfg); // abandoned office
		Gen_005.ConfigDefaults(cfg); // hotel
		Gen_006.ConfigDefaults(cfg); // lights out
//		Gen_007.ConfigDefaults(cfg); // thalassophobia
//		Gen_009.ConfigDefaults(cfg); // suburbs
//		Gen_010.ConfigDefaults(cfg); // field of wheat
		Gen_011.ConfigDefaults(cfg); // concrete jungle
		Gen_019.ConfigDefaults(cfg); // attic
		Gen_023.ConfigDefaults(cfg); // overgrowth
		Gen_033.ConfigDefaults(cfg); // run for your life
//		Gen_036.ConfigDefaults(cfg); // airport
		Gen_037.ConfigDefaults(cfg); // poolrooms
		Gen_040.ConfigDefaults(cfg); // arcade
//		Gen_078.ConfigDefaults(cfg); // space
		Gen_094.ConfigDefaults(cfg); // motion
//		Gen_151.ConfigDefaults(cfg); // dollhouse
		Gen_188.ConfigDefaults(cfg); // the windows
		Gen_308.ConfigDefaults(cfg); // ikea
		Gen_309.ConfigDefaults(cfg); // radio station
		Gen_771.ConfigDefaults(cfg); // crossroads
//		Gen_866.ConfigDefaults(cfg); // dirtfield
	}



	public boolean enableDynmapConfigGen() {
		return this.config.get().getBoolean("Enable Dynmap Config Gen");
	}



	public int getSpawnDistance() {
		return this.config.get().getInt("Spawn Distance");
	}

	public ConfigurationSection getLevelParams(final int level) {
		return this.config.get()
			.getConfigurationSection(
				String.format("Level%d.Params", Integer.valueOf(level)));
	}
	public ConfigurationSection getLevelBlocks(final int level) {
		return this.config.get()
			.getConfigurationSection(
				String.format("Level%d.Blocks", Integer.valueOf(level)));
	}



	// -------------------------------------------------------------------------------



	public TaskReconvergence getReconvergenceTask() {
		return this.taskReconvergence.get();
	}
	public TeleportManager getTeleportManager() {
		return this.tpManager.get();
	}
	public QuoteAnnouncer getQuoteAnnouncer() {
		return this.quoteAnnouncer.get();
	}



	// -------------------------------------------------------------------------------
	// levels



	public BackroomsLevel register(final int level, final BackroomsLevel backlevel) {
		this.backlevels.put(Integer.valueOf(level), backlevel);
		return backlevel;
	}
	public int[] getLevels() {
		final int num = this.backlevels.size();
		final int[] levels = new int[num];
		int i = 0;
		for (final Integer lvl : this.backlevels.keySet()) {
			levels[i] = lvl.intValue();
			i++;
		}
		return levels;
	}



	public BackroomsLevel getBackroomsLevel(final int level) {
		// main level
		{
			final BackroomsLevel backlevel = this.backlevels.get(Integer.valueOf(level));
			if (backlevel != null)
				return backlevel;
		}
		// level in world
		for (final BackroomsLevel backlevel : this.backlevels.values()) {
			if (backlevel.containsLevel(level))
				return backlevel;
		}
		return null;
	}



	// main level
	public int getMainLevel(final int level) {
		final BackroomsLevel backlevel = this.getBackroomsLevel(level);
		return (backlevel == null ? Integer.MIN_VALUE : backlevel.getMainLevel());
	}
	public BackroomsLevel getMainBackroomsLevel(final String worldName) {
		final int level = this.getWorldLevel(worldName);
		if (level < 0) return null;
		return this.backlevels.get(Integer.valueOf(level));
	}
	public BackroomsLevel getMainBackroomsLevel(final World world) {
		if (world == null) return null;
		return this.getMainBackroomsLevel(world.getName());
	}



	// level world
	public int getWorldLevel(final World world) {
		return (world == null ? null : this.getWorldLevel(world.getName()));
	}
	public int getWorldLevel(final String worldName) {
		if (!IsEmpty(worldName)) {
			if (worldName.startsWith("level")) {
				final String str = worldName.substring(5);
				if (!IsEmpty(str)) {
					try {
						final int level = Integer.parseInt(str);
						return (this.isValidLevel(level) ? level : Integer.MIN_VALUE);
					} catch (NumberFormatException ignore) {}
				}
			}
		}
		return Integer.MIN_VALUE;
	}
	public World getWorldFromLevel(final int level) {
		final int lvl = this.getMainLevel(level);
		if (lvl < 0) return null;
		return Bukkit.getWorld("level" + Integer.toString(lvl));
	}



	// level from location
	public int getLevel(final Location loc) {
		final BackroomsLevel backlevel = this.getBackroomsLevel(loc);
		if (backlevel == null) return Integer.MIN_VALUE;
		else                   return backlevel.getLevel(loc);
	}
	public BackroomsLevel getBackroomsLevel(final Location loc) {
		if (loc == null) return null;
		return this.getBackroomsLevel( this.getWorldLevel(loc.getWorld()) );
	}



	// level from player
	public int getLevel(final Player player) {
		if (player == null) return Integer.MIN_VALUE;
		return this.getLevel(player.getLocation());
	}
	public BackroomsLevel getBackroomsLevel(final Player player) {
		if (player == null) return null;
		return this.getMainBackroomsLevel(player.getLocation().getWorld());
	}



	public void assertValidLevel(final int level) {
		if (!this.isValidLevel(level))
			throw new RuntimeException("Invalid backrooms level: " + Integer.toString(level));
	}
	public boolean isValidLevel(final int level) {
		if (this.isValidWorld(level)) return true;
		for (final BackroomsLevel backlevel : this.backlevels.values()) {
			if (backlevel.containsLevel(level))
				return true;
		}
		return false;
	}
	public boolean isValidWorld(final int level) {
		return this.backlevels.containsKey(Integer.valueOf(level));
	}
	public boolean isValidWorld(final World world) {
		return this.isValidWorld( this.getWorldLevel(world) );
	}



	public void noclip(final Player player, final int level_to) {
		if (level_to == Integer.MIN_VALUE) {
			this.noclip(player);
			return;
		}
		final TeleportManager manager = this.tpManager.get();
		Location loc = manager.getSpawnLocation(level_to);
		if (loc == null) {
			this.log().warning(LOG_PREFIX + "Failed to find spawn for level: " + Integer.toString(level_to));
			final World world = this.getWorldFromLevel(level_to);
			if (world == null) {
				this.log().warning(LOG_PREFIX + "Unknown backrooms world for level: " + Integer.toString(level_to));
				return;
			}
			loc = world.getSpawnLocation();
		}
		this.log().info(LOG_PREFIX+"No-clip player: "+player.getName()+" to level: "+Integer.toString(level_to));
		player.teleport(loc);
		player.setNoDamageTicks(100);
		player.setFallDistance(0.0f);
	}
	public int noclip(final Player player) {
		final int level_from = this.getLevel(player);
		final int level_to   = this.noclip(level_from);
		this.noclip(player, level_to);
		return level_to;
	}
	public int noclip(final int level_from) {
		final TeleportManager manager = this.tpManager.get();
		if (manager == null) {
			this.log().warning(LOG_PREFIX+"teleport chance weights not loaded");
			return 0;
		}
		return manager.getDestinationLevel(level_from);
	}



//TODO: use this
	public boolean addVisitedLevel(final Player player) {
		final UUID uuid = player.getUniqueId();
		final int level = this.getLevel(player);
		if (level < 0) return false;
		CopyOnWriteArraySet<Integer> visited = this.visitLevels.get(uuid);
		if (visited == null) {
			visited = new CopyOnWriteArraySet<Integer>();
			this.visitLevels.put(uuid, visited);
		}
		final int sizeLast = visited.size();
		visited.add(Integer.valueOf(level));
		if (visited.size() > sizeLast) {
			// check if visited all levels
			for (final Integer lvl : this.backlevels.keySet()) {
				if (!visited.contains(lvl))
					return false;
			}
			return true;
		}
		return false;
	}



	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String argsStr) {
//TODO: https://github.com/Multiverse/Multiverse-Core/blob/17129f68d204438f1d8e134388b72507dc8c1a63/src/main/java/com/onarandombox/MultiverseCore/commands/CreateCommand.java#L117
		if (!worldName.startsWith("level"))
			throw new RuntimeException("Invalid world name, must be level# found: "+worldName);
		this.log().info(String.format("%s%s world: %s", LOG_PREFIX, GENERATOR_NAME, worldName));
		return this.getMainBackroomsLevel(worldName);
	}



	public GeneratorPerspective getDynmapPerspective() {
		// existing
		{
			final GeneratorPerspective gen = this.dynmap_perspective.get();
			if (gen != null)
				return gen;
		}
		// new instance
		{
			final GeneratorPerspective gen = new GeneratorPerspective();
			this.dynmap_perspective.set(gen);
			return gen;
		}
	}



}
