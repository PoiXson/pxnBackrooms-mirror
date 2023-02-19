package com.poixson.backrooms;

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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.backrooms.commands.Commands;
import com.poixson.backrooms.dynmap.GeneratorPerspective;
import com.poixson.backrooms.levels.LevelBackrooms;
import com.poixson.backrooms.levels.Level_000;
import com.poixson.backrooms.levels.Level_009;
import com.poixson.backrooms.levels.Level_010;
import com.poixson.backrooms.levels.Level_011;
import com.poixson.backrooms.levels.Level_033;
import com.poixson.backrooms.levels.Level_078;
import com.poixson.backrooms.levels.Level_151;
import com.poixson.backrooms.levels.Level_771;
import com.poixson.backrooms.levels.Level_866;
import com.poixson.backrooms.listeners.ItemDespawnListener;
import com.poixson.backrooms.listeners.PlayerDamageListener;
import com.poixson.backrooms.listeners.RedstoneListener;
import com.poixson.commonmc.tools.DelayedChestFiller;
import com.poixson.commonmc.tools.plugin.xJavaPlugin;
import com.poixson.utils.Utils;


public class BackroomsPlugin extends xJavaPlugin {
	public static final String LOG_PREFIX  = "[pxnBackrooms] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + "[Backrooms] " + ChatColor.WHITE;
//TODO
	protected static final int SPIGOT_PLUGIN_ID = 0;
	protected static final int BSTATS_PLUGIN_ID = 17231;

	protected static final String GENERATOR_NAME = "Backrooms";
//TODO: set the version automatically
	protected static final String DEFAULT_RESOURCE_PACK = "http://dl.poixson.com/mcplugins/pxnBackrooms/pxnBackrooms-resourcepack-0.1.1.zip";

	protected static final AtomicReference<BackroomsPlugin> instance = new AtomicReference<BackroomsPlugin>(null);

	// backrooms levels
	protected final HashMap<Integer, LevelBackrooms> backlevels = new HashMap<Integer, LevelBackrooms>();
	protected final ConcurrentHashMap<UUID, CopyOnWriteArraySet<Integer>> visitLevels = new ConcurrentHashMap<UUID, CopyOnWriteArraySet<Integer>>();

	// chance to teleport to levels
	protected final AtomicReference<TeleportManager> tpManager = new AtomicReference<TeleportManager>(null);

	// listeners
	protected final AtomicReference<Commands>             commandListener      = new AtomicReference<Commands>(null);
	protected final AtomicReference<PlayerDamageListener> playerDamageListener = new AtomicReference<PlayerDamageListener>(null);
	protected final AtomicReference<RedstoneListener>     redstoneListener     = new AtomicReference<RedstoneListener>(null);
	protected final AtomicReference<ItemDespawnListener>  itemDespawnListener  = new AtomicReference<ItemDespawnListener>(null);

	// dynmap config generator
	protected final AtomicReference<GeneratorPerspective> dynmap_perspective = new AtomicReference<GeneratorPerspective>(null);



	public BackroomsPlugin() {
		super(BackroomsPlugin.class);
	}



	@Override
	public void onEnable() {
		if (!instance.compareAndSet(null, this))
			throw new RuntimeException("Plugin instance already enabled?");
		super.onEnable();
		// resource pack
		{
			final String pack = Bukkit.getResourcePack();
			if (pack == null || pack.isEmpty()) {
				log.warning(LOG_PREFIX + "Resource pack not set");
				log.warning(LOG_PREFIX + "You can use this one: " + DEFAULT_RESOURCE_PACK);
			} else {
				log.info(String.format(
					"%sUsing resource pack: %s",
					LOG_PREFIX,
					Bukkit.getResourcePack()
				));
			}
		}
		// backrooms levels
		new Level_000(this); // lobby, lights out, basement, hotel, attic, poolrooms, radio station
		new Level_009(this); // suburbs
		new Level_010(this); // field of wheat
		new Level_011(this); // city
		new Level_033(this); // run for your life
		new Level_078(this); // space
		new Level_151(this); // dollhouse
		new Level_771(this); // crossroads
		new Level_866(this); // dirtfield
		this.getDynmapPerspective().commit( new File(this.getDataFolder(), "../dynmap/") );
		// create worlds (after server starts)
		(new BukkitRunnable() {
			@Override
			public void run() {
//TODO: this is converting long to string
				final String seed = Long.toString( Bukkit.getWorld("world").getSeed() );
				final Iterator<Entry<Integer, LevelBackrooms>> it = BackroomsPlugin.this.backlevels.entrySet().iterator();
				while (it.hasNext()) {
					final Entry<Integer, LevelBackrooms> entry = it.next();
					final int level = entry.getKey().intValue();
					if (entry.getValue().isWorldMain(level))
						LevelBackrooms.MakeWorld(level, seed);
				}
			}
		}).runTask(this);
		// register levels
		for (final LevelBackrooms level : this.backlevels.values()) {
			level.register();
		}
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
		// player damage listener
		{
			final PlayerDamageListener listener = new PlayerDamageListener(this);
			final PlayerDamageListener previous = this.playerDamageListener.getAndSet(listener);
			if (previous != null)
				previous.unregister();
			listener.register();
		}
		// redstone listener
		{
			final RedstoneListener listener = new RedstoneListener(this);
			final RedstoneListener previous = this.redstoneListener.getAndSet(listener);
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
	}

	@Override
	public void onDisable() {
		super.onDisable();
		// finish filling chests
		DelayedChestFiller.stop();
		// unload levels
		for (final LevelBackrooms lvl : this.backlevels.values()) {
			lvl.unregister();
		}
		this.backlevels.clear();
		// commands listener
		{
			final Commands listener = this.commandListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// player damage listener
		{
			final PlayerDamageListener listener = this.playerDamageListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// redstone listener
		{
			final RedstoneListener listener = this.redstoneListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		// teleport chance
		this.tpManager.set(null);
		// item despawn listener
		{
			final ItemDespawnListener listener = this.itemDespawnListener.getAndSet(null);
			if (listener != null)
				listener.unregister();
		}
		this.dynmap_perspective.set(null);
		if (!instance.compareAndSet(this, null))
			(new RuntimeException("Disable wrong instance of plugin?")).printStackTrace();
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
		cfg.addDefault("Enable Dynmap Config Gen", Boolean.FALSE);
	}



	public boolean enableDynmapConfigGen() {
		return this.config.get().getBoolean("Enable Dynmap Config Gen");
	}



	// -------------------------------------------------------------------------------
	// levels



	public LevelBackrooms register(final int level, final LevelBackrooms backlevel) {
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
	public int getPlayerLevel(final UUID uuid) {
		final Player player = Bukkit.getPlayer(uuid);
		if (player == null)
			return Integer.MIN_VALUE;
		return this.getPlayerLevel(player);
	}
	public int getPlayerLevel(final Player player) {
		if (player == null)
			return Integer.MIN_VALUE;
		final int lvl = this.getLevelFromWorld(player.getWorld());
		final LevelBackrooms backlevel = this.getBackroomsLevel(lvl);
		return backlevel.getLevelFromY(player.getLocation().getBlockY());
	}
	public int getLevelFromWorld(final World world) {
		if (world == null)
			return Integer.MIN_VALUE;
		return this.getLevelFromWorld(world.getName());
	}
	public int getLevelFromWorld(final String worldName) {
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
			throw new RuntimeException("Invalid backrooms level: " + Integer.toString(level));
	}
	public boolean isValidLevel(final int level) {
		return this.backlevels.containsKey(Integer.valueOf(level));
	}
	public boolean isValidLevel(final World world) {
		final int level = this.getLevelFromWorld(world);
		return this.isValidLevel(level);
	}



	public World getWorldFromLevel(final int level) {
		final String worldName = "level" + Integer.toString(level);
		if (Utils.isEmpty(worldName))
			return null;
		return Bukkit.getWorld(worldName);
	}



	public void noclip(final Player player, final int level) {
		if (level == Integer.MIN_VALUE) {
			this.noclip(player);
			return;
		}
		final TeleportManager manager = this.tpManager.get();
		Location loc = manager.getSpawnLocation(level);
		if (loc == null) {
			final World world = this.getWorldFromLevel(level);
			if (world == null) {
				log.warning(String.format("%sUnknown backrooms world for level: %d", LOG_PREFIX, Integer.valueOf(level)));
				return;
			}
			loc = world.getSpawnLocation();
		}
		log.info(LOG_PREFIX+"No-clip player: "+player.getName()+" to level: "+Integer.toString(level));
		player.teleport(loc);
	}
	public int noclip(final Player player) {
		final int levelFrom = this.getPlayerLevel(player);
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



	public LevelBackrooms getBackroomsLevel(final int level) {
		return this.backlevels.get(Integer.valueOf(level));
	}



	public boolean addVisitedLevel(final Player player) {
		return this.addVisitedLevel(player.getUniqueId());
	}
	public boolean addVisitedLevel(final UUID uuid) {
		final int level = this.getPlayerLevel(uuid);
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
		if (!worldName.startsWith("level"))
			throw new RuntimeException("Invalid world name, must be level# found: "+worldName);
		log.info(String.format("%s%s world: %s", LOG_PREFIX, GENERATOR_NAME, worldName));
		final int level = this.getLevelFromWorld(worldName);
		return this.getBackroomsLevel(level);
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



	// -------------------------------------------------------------------------------



	@Override
	protected int getSpigotPluginID() {
		return SPIGOT_PLUGIN_ID;
	}
	@Override
	protected int getBStatsID() {
		return BSTATS_PLUGIN_ID;
	}



}
