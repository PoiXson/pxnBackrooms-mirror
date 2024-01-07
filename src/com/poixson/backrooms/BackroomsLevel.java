package com.poixson.backrooms;

import static com.poixson.backrooms.BackroomsPlugin.LOG_PREFIX;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.PluginManager;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.poixson.tools.xRand;
import com.poixson.tools.plotter.BlockPlotter;


public abstract class BackroomsLevel extends ChunkGenerator {
	public static final int DEFAULT_SPAWN_NEAR_DISTANCE = 100;

	protected final BackroomsPlugin plugin;
	protected final int seed;

	protected final CopyOnWriteArraySet<BackroomsGen> gens = new CopyOnWriteArraySet<BackroomsGen>();
	protected final CopyOnWriteArraySet<BackroomsPop> pops = new CopyOnWriteArraySet<BackroomsPop>();
	protected final PopulatorManager popman = new PopulatorManager();

	protected final ConcurrentHashMap<Integer, Location> spawns = new ConcurrentHashMap<Integer, Location>();



	public BackroomsLevel(final BackroomsPlugin plugin) {
		this.plugin = plugin;
		this.seed = plugin.getSeed();
		plugin.register(this.getMainLevel(), this);
	}



	public void register() {
		for (final BackroomsGen gen : this.gens)
			gen.register();
	}
	public void unregister() {
		for (final BackroomsGen gen : this.gens)
			gen.unregister();
	}



	protected <T extends BackroomsGen> T register(final T gen) {
		this.gens.add(gen);
		gen.loadConfig();
		gen.initNoise();
		return gen;
	}
	protected <T extends BackroomsPop> T register(final T pop) {
		this.pops.add(pop);
		return pop;
	}



	public BackroomsPlugin getPlugin() {
		return this.plugin;
	}



	// -------------------------------------------------------------------------------
	// locations



	public abstract int getMainLevel();
	public abstract boolean containsLevel(final int level);

	public boolean isWorldMain(final int level) {
		return (this.getMainLevel() == level);
	}
	public boolean isSingleLevelWorld() {
		return (this.gens.size() <= 1);
	}

	public int getLevel(final Location loc) {
		return this.getMainLevel();
	}

	public abstract int getY(final int level);
	public abstract int getMaxY(final int level);



	// -------------------------------------------------------------------------------
	// spawn



	public void flushSpawns() {
		this.spawns.clear();
	}



	public Location validSpawn(final Location loc) {
		final Block blockA = loc.getBlock();
		final Block blockB = blockA.getRelative(0, 1, 0);
		if (blockA.isPassable()
		&&  blockB.isPassable())
			return blockA.getLocation();
		return null;
	}

	public Location getSpawnArea(final int level) {
		// cached area
		{
			final Location area = this.spawns.get(Integer.valueOf(level));
			if (area != null)
				return area;
		}
		// find spawn area
		{
			final Location area = this.getNewSpawnArea(level);
			if (area == null) return null;
			this.spawns.put(Integer.valueOf(level), area);
			return area;
		}
	}
	public Location getNewSpawnArea(final int level) {
		final int distance = this.plugin.getSpawnDistance();
		final xRand rnd = xRand.Get(0-distance, distance);
		final int x = rnd.nextInt();
		final int z = rnd.nextInt();
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return world.getBlockAt(x, 0, z).getLocation();
	}
	public Location getSpawnNear(final int level, final Location area) {
		final int max_y         = this.getMaxY(level);
		final int distance_near = this.getSpawnDistanceNear(level);
		final int distance_min  = Math.floorDiv(distance_near, 3);
		final xRand rnd = xRand.Get(distance_min, distance_near);
		final float yaw = (float) xRand.Get(0, 360).nextInt();
		final World world = area.getWorld();
		final int y = this.getY(level);
		final int h = max_y - y;
		int x, z;
		Location near, valid;
		for (int tries=0; tries<20; tries++) {
			for (int iy=0; iy<h; iy++) {
				x = area.getBlockX() + rnd.nextInt();
				z = area.getBlockZ() + rnd.nextInt();
				near = world.getBlockAt(x, y+iy, z).getLocation();
				valid = this.validSpawn(near);
				if (valid != null) {
					valid.setYaw(yaw);
					return valid;
				}
			}
		}
		this.log().warning(String.format("%sFailed to find a safe spawn location: %s", LOG_PREFIX, area.toString()));
		return area;
	}

	public Location getSpawnLocation(final int level) {
		final Location area = this.getSpawnArea(level);
		return this.getSpawnNear(level, area);
	}

	public Location getFixedSpawnLocation(final World world) {
		return this.getFixedSpawnLocation(world, null);
	}
	@Override
	public Location getFixedSpawnLocation(final World world, final Random random) {
		final int level_main = this.getMainLevel();
		final int y = this.getY(level_main);
		return world.getBlockAt(0, y, 0).getLocation();
	}



	public int getSpawnDistanceNear(final int level) {
		return DEFAULT_SPAWN_NEAR_DISTANCE;
	}



	// -------------------------------------------------------------------------------
	// generate



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		// generate
		final LinkedList<BlockPlotter> delayed_plotters = new LinkedList<BlockPlotter>();
		this.generate(chunkX, chunkZ, chunk, delayed_plotters);
		// place delayed blocks
		if (!delayed_plotters.isEmpty()) {
			for (final BlockPlotter plot : delayed_plotters)
				plot.run();
			delayed_plotters.clear();
		}
	}
	protected abstract void generate(final int chunkX, final int chunkZ,
			final ChunkData chunk, final LinkedList<BlockPlotter> plots);



	@Override
	public BiomeProvider getDefaultBiomeProvider(final WorldInfo worldInfo) {
		return new BiomeProvider() {
			private final List<Biome> biomes = new LinkedList<Biome>();
			{ this.biomes.add(Biome.THE_VOID); }
			@Override
			public List<Biome> getBiomes(final WorldInfo worldInfo) {
				return this.biomes;
			}
			@Override
			public Biome getBiome(final WorldInfo worldInfo, final int x, final int y, final int z) {
				return Biome.THE_VOID;
			}
		};
	}



	protected class PopulatorManager extends BlockPopulator {
		@Override
		public void populate(final WorldInfo worldInfo, final Random rnd,
				final int chunkX, final int chunkZ, final LimitedRegion region) {
			final LinkedList<BlockPlotter> delayed_plotters = new LinkedList<BlockPlotter>();
			// block plotters
			for (final BackroomsPop pop : BackroomsLevel.this.pops)
				pop.populate(chunkX, chunkZ, region, delayed_plotters);
			// place delayed blocks
			if (!delayed_plotters.isEmpty()) {
				for (final BlockPlotter plot : delayed_plotters)
					plot.run();
				delayed_plotters.clear();
			}
		}
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		final List<BlockPopulator> list = new ArrayList<BlockPopulator>();
		list.add(this.popman);
		return list;
	}



	// -------------------------------------------------------------------------------
	// create world



	public void setup() {
		final MVWorldManager manager = GetMVCore().getMVWorldManager();
		final int level = this.getMainLevel();
		final String name = "level" + Integer.toString(level);
		if (!manager.isMVWorld(name, false)) {
			this.log().warning(String.format("%sCreating backrooms level: %d", LOG_PREFIX, Integer.valueOf(level)));
			final Environment env;
			switch (level) {
			case 78: env = Environment.THE_END; break;
			default: env = Environment.NORMAL;  break;
			}
			final String seedStr = this.plugin.getSeedString();
			if (!manager.addWorld(name, env, seedStr, WorldType.NORMAL, Boolean.FALSE, BackroomsPlugin.GENERATOR_NAME, true))
				throw new RuntimeException("Failed to create world: " + name);
			final MultiverseWorld mvworld = manager.getMVWorld(name, false);
			final World world = mvworld.getCBWorld();
			mvworld.setAutoLoad(true);
			mvworld.setHidden(true);
			mvworld.setKeepSpawnInMemory(false);
			mvworld.setAllowAnimalSpawn(true);
			mvworld.setAllowMonsterSpawn(true);
			mvworld.setAutoHeal(false);
			mvworld.setHunger(true);
			mvworld.setBedRespawn(true);
			mvworld.setDifficulty(Difficulty.HARD);
			mvworld.setPVPMode(true);
			mvworld.setGenerator(BackroomsPlugin.GENERATOR_NAME);
			world.setGameRule(GameRule.KEEP_INVENTORY,             Boolean.TRUE );
			world.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS,       Boolean.TRUE );
			world.setGameRule(GameRule.DROWNING_DAMAGE,            Boolean.TRUE );
			world.setGameRule(GameRule.FREEZE_DAMAGE,              Boolean.FALSE);
			world.setGameRule(GameRule.MOB_GRIEFING,               Boolean.FALSE);
			world.setGameRule(GameRule.DO_ENTITY_DROPS,            Boolean.TRUE );
			world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS,      Boolean.FALSE);
			world.setGameRule(GameRule.SHOW_DEATH_MESSAGES,        Boolean.TRUE );
			world.setGameRule(GameRule.BLOCK_EXPLOSION_DROP_DECAY, Boolean.FALSE);
			world.setGameRule(GameRule.TNT_EXPLOSION_DROP_DECAY,   Boolean.FALSE);
			world.setGameRule(GameRule.MOB_EXPLOSION_DROP_DECAY,   Boolean.FALSE);
			world.setGameRule(GameRule.FIRE_DAMAGE,                Boolean.TRUE );
			world.setGameRule(GameRule.DO_FIRE_TICK,               Boolean.TRUE );
			world.setGameRule(GameRule.DO_TILE_DROPS,              Boolean.TRUE );
			world.setGameRule(GameRule.DO_TRADER_SPAWNING,         Boolean.TRUE );
			world.setGameRule(GameRule.DO_WARDEN_SPAWNING,         Boolean.TRUE );
			world.setGameRule(GameRule.SPAWN_RADIUS,                Integer.valueOf(50));
			world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, Integer.valueOf( 1));
			world.setGameRule(GameRule.SNOW_ACCUMULATION_HEIGHT,    Integer.valueOf( 8));
			// game mode
			switch (level) {
			case 771: mvworld.setGameMode(GameMode.ADVENTURE); break;
			default:  mvworld.setGameMode(GameMode.SURVIVAL ); break;
			}
			// time
			switch (level) {
			case 0:  // lobby
			case 7:  // thalassophobia
			case 33: // run for your life
			case 78: // space
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, Boolean.FALSE);
				mvworld.setTime("midnight"); break;
			case 151: // dollhouse
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, Boolean.FALSE);
				mvworld.setTime("noon"); break;
			default:
				world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, Boolean.TRUE);
				mvworld.setTime("noon"); break;
			}
			// weather
			switch (level) {
			case 0:   // lobby
			case 7:   // thalassophobia
			case 33:  // run for your life
			case 78:  // space
			case 151: // dollhouse
			case 771: // crossroads
			case 866: // dirtfield
				world.setGameRule(GameRule.DO_WEATHER_CYCLE, Boolean.FALSE); break;
			default:
				world.setGameRule(GameRule.DO_WEATHER_CYCLE, Boolean.TRUE);  break;
			}
			// insomnia
			switch (level) {
			case 0:   // lobby
			case 9:   // suburbs
			case 11:  // concrete jungle
			case 78:  // space
			case 151: // dollhouse
			case 771: // crossroads
			case 866: // dirtfield
				world.setGameRule(GameRule.DO_INSOMNIA, Boolean.TRUE);  break;
			default:
				world.setGameRule(GameRule.DO_INSOMNIA, Boolean.FALSE); break;
			}
			// immediate respawn
			switch (level) {
			case 33: // run for your life
				world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, Boolean.TRUE);  break;
			default:
				world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, Boolean.FALSE); break;
			}
			// fall damage
			switch (level) {
			case  0: // lobby
			case 94: // motion
				world.setGameRule(GameRule.FALL_DAMAGE, Boolean.FALSE); break;
			default:
				world.setGameRule(GameRule.FALL_DAMAGE, Boolean.TRUE);  break;
			}
			// natural regeneration
			switch (level) {
			case 0:   // lobby
			case 7:   // thalassophobia
			case 9:   // suburbs
			case 10:  // field of wheat
			case 11:  // concrete jungle
			case 78:  // space
			case 151: // dollhouse
			case 771: // crossroads
			case 866: // dirtfield
				world.setGameRule(GameRule.NATURAL_REGENERATION, Boolean.FALSE); break;
			default:
				world.setGameRule(GameRule.NATURAL_REGENERATION, Boolean.TRUE);  break;
			}
//			// F3 debug info
//			switch (level) {
//			case 0:   // lobby
//			case 7:   // thalassophobia
//			case 9:   // suburbs
//			case 10:  // field of wheat
//			case 11:  // concrete jungle
//			case 151: // dollhouse
//			case 866: // dirtfield
//				world.setGameRule(GameRule.REDUCED_DEBUG_INFO, Boolean.TRUE);  break;
//			default:
//				world.setGameRule(GameRule.REDUCED_DEBUG_INFO, Boolean.FALSE); break;
//			}
		}
		// not retained after restart
		{
			final World world = Bukkit.getWorld(name);
			if (world == null) throw new NullPointerException("Failed to find world: " + name);
			switch (level) {
			case 33: world.setTicksPerSpawns(SpawnCategory.MONSTER,   1); break;
			default: world.setTicksPerSpawns(SpawnCategory.MONSTER, 100); break;
			}
		}
	}



	public static MultiverseCore GetMVCore() {
		final PluginManager pm = Bukkit.getServer().getPluginManager();
		final MultiverseCore mvcore = (MultiverseCore) pm.getPlugin("Multiverse-Core");
		if (mvcore == null) throw new RuntimeException("Plugin not found: Multiverse-Core");
		return mvcore;
	}



	public Logger log() {
		return this.plugin.getLogger();
	}



}
