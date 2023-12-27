package com.poixson.backrooms;

import static com.poixson.backrooms.BackroomsPlugin.LOG_PREFIX;
import static com.poixson.tools.xJavaPlugin.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
import com.poixson.backrooms.worlds.Level_000;
import com.poixson.backrooms.worlds.Level_033;
import com.poixson.backrooms.worlds.Level_094;
import com.poixson.backrooms.worlds.Level_771;
import com.poixson.tools.plotter.BlockPlotter;
import com.poixson.utils.RandomUtils;


public abstract class BackroomsLevel extends ChunkGenerator {
	public static final int DEFAULT_SPAWN_SEARCH_HEIGHT = 10;
	public static final int DEFAULT_SPAWN_NEAR_DISTANCE = 100;

	protected final BackroomsPlugin plugin;

	protected final CopyOnWriteArraySet<BackroomsGen> gens = new CopyOnWriteArraySet<BackroomsGen>();
	protected final CopyOnWriteArraySet<BackroomsPop> pops = new CopyOnWriteArraySet<BackroomsPop>();
	protected final PopulatorManager popman = new PopulatorManager();

	protected final int mainlevel;



	public BackroomsLevel(final BackroomsPlugin plugin, final int mainlevel) {
		this.plugin    = plugin;
		this.mainlevel = mainlevel;
		plugin.register(this.getMainLevel(), this);
	}

	public void register() {
		for (final BackroomsGen gen : this.gens) {
			gen.register();
		}
	}
	public void unregister() {
		for (final BackroomsGen gen : this.gens) {
			gen.unregister();
		}
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



	public int getMainLevel() {
		return this.mainlevel;
	}
	public boolean isWorldMain(final int level) {
		return (this.getMainLevel() == level);
	}
	public boolean isWorldStacked() {
		return (this.gens.size() > 1);
	}



	public BackroomsPlugin getPlugin() {
		return this.plugin;
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



	// -------------------------------------------------------------------------------
	// generate world



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		// seed
		final int seed = Long.valueOf( worldInfo.getSeed() ).intValue();
		for (final BackroomsGen gen : this.gens)
			gen.setSeed(seed);
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



	// -------------------------------------------------------------------------------
	// locations



	public boolean canCacheSpawn() {
		return true;
	}

	public int getLevelFromY(final int y) {
		return this.getMainLevel();
	}
	public abstract int getY(final int level);
	public abstract int getMaxY(final int level);

	public abstract boolean containsLevel(final int level);

	public Location validateSpawn(final Location loc) {
		return validateSpawn(loc, DEFAULT_SPAWN_SEARCH_HEIGHT);
	}
	public Location validateSpawn(final Location loc, final int height) {
		final World world = loc.getWorld();
		Block blockA = loc.getBlock();
		Block blockB;
		final int x = loc.getBlockX();
		final int y = loc.getBlockY();
		final int z = loc.getBlockZ();
		for (int i=0; i<height; i++) {
			blockB = world.getBlockAt(x, y+i+1, z);
			if (blockA.isPassable()
			&&  blockB.isPassable())
				return blockA.getLocation();
			blockA = blockB;
		}
		return null;
	}

	public Location getNewSpawnArea(final int level) {
		final int distance = this.plugin.getSpawnDistance();
		final int y = this.getY(level);
		final int x = RandomUtils.GetRandom(0-distance, distance);
		final int z = RandomUtils.GetRandom(0-distance, distance);
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return this.getSpawnNear(world.getBlockAt(x, y, z).getLocation());
	}

	public Location getSpawnNear(final Location spawn) {
		return getSpawnNear(spawn, DEFAULT_SPAWN_NEAR_DISTANCE);
	}
	public Location getSpawnNear(final Location spawn, final int distance) {
		final int distanceMin = Math.floorDiv(distance, 3);
		final float yaw = (float) RandomUtils.GetRandom(0, 360);
		final World world = spawn.getWorld();
		final int y = spawn.getBlockY();
		int x, z;
		Location near, valid;
		for (int t=0; t<10; t++) {
			for (int iy=0; iy<10; iy++) {
				x = spawn.getBlockX() + RandomUtils.GetRandom(distanceMin, distance);
				z = spawn.getBlockZ() + RandomUtils.GetRandom(distanceMin, distance);
				near = world.getBlockAt(x, y+iy, z).getLocation();
				valid = this.validateSpawn(near);
				if (valid != null) {
					valid.setYaw(yaw);
					return valid;
				}
			}
		}
		this.log().warning(LOG_PREFIX + "Failed to find a safe spawn location: " + spawn.toString());
		return spawn;
	}

	@Override
	public Location getFixedSpawnLocation(final World world, final Random random) {
		final int y = this.getY( this.getMainLevel() );
		final Location loc = world.getBlockAt(0, y, 0).getLocation();
		return this.getSpawnNear(loc);
	}



	// -------------------------------------------------------------------------------
	// create world



	public static void MakeWorld(final int level, final String seed) {
		final MVWorldManager manager = GetMVCore().getMVWorldManager();
		final String name = "level" + Integer.toString(level);
		if (!manager.isMVWorld(name, false)) {
			Log().warning(LOG_PREFIX + "Creating backrooms level: " + Integer.toString(level));
			final Environment env;
			switch (level) {
			case 78: env = Environment.THE_END; break;
			default: env = Environment.NORMAL;  break;
			}
			if (!manager.addWorld(name, env, seed, WorldType.NORMAL, Boolean.FALSE, BackroomsPlugin.GENERATOR_NAME, true))
				throw new RuntimeException("Failed to create world: " + name);
			final MultiverseWorld mvworld = manager.getMVWorld(name, false);
			final World world = mvworld.getCBWorld();
			mvworld.setAlias("backrooms");
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
			mvworld.setRespawnToWorld("level0");
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
			// fixed spawn
//TODO fixed spawn should probably be 0x 0z
			final int y;
			switch (level) {
			case 0:   y = Level_000.Y_000;   break; // lobby
//			case 7:   y = Level_007.LEVEL_Y; break; // thalassophobia
//			case 9:   y = Level_009.LEVEL_Y; break; // suburbs
//			case 10:  y = Level_010.LEVEL_Y; break; // field of wheat
//			case 11:  y = Level_011.LEVEL_Y; break; // concrete jungle
			case 33:  y = Level_033.LEVEL_Y; break; // run for your life
//			case 36:  y = Level_036.LEVEL_Y; break; // airport
			case 78:  y = 200;               break; // space
			case 94:  y = Level_094.LEVEL_Y; break; // motion
//			case 151: y = Level_151.LEVEL_Y; break; // dollhouse
			case 771: y = Level_771.LEVEL_Y; break; // crossroads
//			case 866: y = Level_866.LEVEL_Y; break; // dirtfield
			default:  y = 0;                 break;
			}
			mvworld.setSpawnLocation(world.getBlockAt(0, y, 0).getLocation());
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
			case 0: // lobby
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
			// F3 debug info
			switch (level) {
			case 0:   // lobby
			case 7:   // thalassophobia
			case 9:   // suburbs
			case 10:  // field of wheat
			case 11:  // concrete jungle
			case 151: // dollhouse
			case 866: // dirtfield
				world.setGameRule(GameRule.REDUCED_DEBUG_INFO, Boolean.TRUE);  break;
			default:
				world.setGameRule(GameRule.REDUCED_DEBUG_INFO, Boolean.FALSE); break;
			}
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
