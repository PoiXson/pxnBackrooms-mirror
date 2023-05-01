package com.poixson.backrooms.levels;

import static com.poixson.backrooms.BackroomsPlugin.LOG_PREFIX;
import static com.poixson.commonmc.tools.plugin.xJavaPlugin.LOG;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.PluginManager;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.commonmc.tools.plotter.BlockPlotter;
import com.poixson.utils.RandomUtils;


public abstract class LevelBackrooms extends ChunkGenerator {
	public static final int DEFAULT_SPAWN_SEARCH_HEIGHT = 10;
	public static final int DEFAULT_SPAWN_NEAR_DISTANCE = 50;

	protected final BackroomsPlugin plugin;

	protected final CopyOnWriteArraySet<GenBackrooms> gens = new CopyOnWriteArraySet<GenBackrooms>();
	protected final CopyOnWriteArraySet<PopBackrooms> pops = new CopyOnWriteArraySet<PopBackrooms>();
	protected final PopulatorManager popman = new PopulatorManager();

	protected final int mainlevel;



	public LevelBackrooms(final BackroomsPlugin plugin, final int mainlevel) {
		this.plugin    = plugin;
		this.mainlevel = mainlevel;
		plugin.register(this.getMainLevel(), this);
	}

	public void register() {
		for (final GenBackrooms gen : this.gens) {
			gen.register();
		}
	}
	public void unregister() {
		for (final GenBackrooms gen : this.gens) {
			gen.unregister();
		}
	}



	protected class PopulatorManager extends BlockPopulator {
		@Override
		public void populate(final WorldInfo worldInfo, final Random rnd,
				final int chunkX, final int chunkZ, final LimitedRegion region) {
			final LinkedList<BlockPlotter> delayed_plotters = new LinkedList<BlockPlotter>();
			// block plotters
			for (final PopBackrooms pop : LevelBackrooms.this.pops)
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
		return Arrays.asList(this.popman);
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



	protected <T extends GenBackrooms> T register(final T gen) {
		this.gens.add(gen);
		return gen;
	}
	protected <T extends PopBackrooms> T register(final T pop) {
		this.pops.add(pop);
		return pop;
	}



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		// seed
		final int seed = Long.valueOf( worldInfo.getSeed() ).intValue();
		for (final GenBackrooms gen : this.gens) {
			gen.setSeed(seed);
		}
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



	// -------------------------------------------------------------------------------
	// locations



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

	public Location getNewSpawn(final int level) {
		final int distance = this.plugin.getSpawnDistance();
		final int y = this.getY(level);
		final int x = RandomUtils.GetRandom(0-distance, distance);
		final int z = RandomUtils.GetRandom(0-distance, distance);
		final World world = this.plugin.getWorldFromLevel(level);
		if (world == null) throw new RuntimeException("Invalid backrooms level: "+Integer.toString(level));
		return this.getSpawnNear(world.getBlockAt(x, y, z).getLocation());
	}

	public Location getSpawnNear(final Location loc) {
		return getSpawnNear(loc, DEFAULT_SPAWN_NEAR_DISTANCE);
	}
	public Location getSpawnNear(final Location loc, final int distance) {
		final int distanceMin = Math.floorDiv(distance, 3);
		final World world = loc.getWorld();
		final int y = loc.getBlockY();
		int x, z;
		Location near, valid;
		for (int i=0; i<10; i++) {
			x = loc.getBlockX() + RandomUtils.GetRandom(distanceMin, distance);
			z = loc.getBlockZ() + RandomUtils.GetRandom(distanceMin, distance);
			near = world.getBlockAt(x, y+i, z).getLocation();
			valid = this.validateSpawn(near);
			if (valid != null)
				return valid;
		}
		LOG.warning(LOG_PREFIX + "Failed to find a safe spawn location: " + loc.toString());
		return loc;
	}

	@Override
	public Location getFixedSpawnLocation(final World world, final Random random) {
		final int level = this.plugin.getLevelFromWorld(world);
		final int y = this.getY(level);
		final Location loc = world.getBlockAt(0, y, 0).getLocation();
		return this.getSpawnNear(loc);
	}



	public static void MakeWorld(final int level, final String seed) {
		final MVWorldManager manager = GetMVCore().getMVWorldManager();
		final String name = "level" + Integer.toString(level);
		if (!manager.isMVWorld(name, false)) {
			LOG.warning(String.format(
				"%sCreating backrooms level: %d",
				BackroomsPlugin.LOG_PREFIX,
				Integer.valueOf(level)
			));
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
			// time
			switch (level) {
			case 0:  // lobby
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
			case 11:  // city
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
			case 9:   // suburbs
			case 10:  // field of wheat
			case 11:  // city
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
			case 9:   // suburbs
			case 10:  // field of wheat
			case 11:  // city
			case 151: // dollhouse
			case 866: // dirtfield
				world.setGameRule(GameRule.REDUCED_DEBUG_INFO, Boolean.TRUE);  break;
			default:
				world.setGameRule(GameRule.REDUCED_DEBUG_INFO, Boolean.FALSE); break;
			}
		}
	}



	public static MultiverseCore GetMVCore() {
		final PluginManager pm = Bukkit.getServer().getPluginManager();
		final MultiverseCore mvcore = (MultiverseCore) pm.getPlugin("Multiverse-Core");
		if (mvcore == null) throw new RuntimeException("Multiverse-Core plugin not found");
		return mvcore;
	}



}
