package com.poixson.backrooms.levels;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.PluginManager;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.poixson.backrooms.BackroomsPlugin;


public abstract class LevelBackrooms extends ChunkGenerator {

	protected final BackroomsPlugin plugin;

	protected final CopyOnWriteArraySet<GenBackrooms> gens = new CopyOnWriteArraySet<GenBackrooms>();

	protected final int mainlevel;



	public LevelBackrooms(final BackroomsPlugin plugin, final int mainlevel) {
		this.plugin    = plugin;
		this.mainlevel = mainlevel;
		plugin.register(this.getMainLevel(), this);
	}

	public void unload() {
		for (final GenBackrooms gen : this.gens) {
			gen.unload();
		}
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



	@Override
	public void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		// seed
		final int seed = Long.valueOf( worldInfo.getSeed() ).intValue();
		for (final GenBackrooms gen : this.gens) {
			gen.setSeed(seed);
		}
		// generate
		this.generate(chunk, chunkX, chunkZ);
	}
	protected abstract void generate(final ChunkData chunk, final int chunkX, final int chunkZ);



	public int getLevelFromY(final int y) {
		return this.getMainLevel();
	}
	public abstract int getY(final int level);
	public abstract int getMaxY(final int level);

	public abstract Location getSpawn(final int level);
	public abstract Location getSpawn(final int level, final int x, final int z);

	public Location getSpawn(final int level, final int h,
			final int x, final int y, final int z) {
		final World world = this.plugin.getWorldFromLevel(level);
		// search location
		for (int i=0; i<h; i++) {
			if (Material.AIR.equals(world.getType(x, y+i,   z))
			&&  Material.AIR.equals(world.getType(x, y+i+1, z)) )
				return world.getBlockAt(x, y+i, z).getLocation();
		}
		// search near
		for (int i=0; i<10; i++) {
			// north
			if (Material.AIR.equals(world.getType(x, y,   z-i))
			&&  Material.AIR.equals(world.getType(x, y+1, z-i)) )
				return world.getBlockAt(x, y, z-i).getLocation();
			// south
			if (Material.AIR.equals(world.getType(x, y,   z+i))
			&&  Material.AIR.equals(world.getType(x, y+1, z+i)) )
				return world.getBlockAt(x, y, z+i).getLocation();
			// east
			if (Material.AIR.equals(world.getType(x+i, y,   z))
			&&  Material.AIR.equals(world.getType(x+i, y+1, z)) )
				return world.getBlockAt(x+i, y, z).getLocation();
			// west
			if (Material.AIR.equals(world.getType(x-i, y,   z))
			&&  Material.AIR.equals(world.getType(x-i, y+1, z)) )
				return world.getBlockAt(x-i, y, z).getLocation();
		}
		return null;
	}
	@Override
	public Location getFixedSpawnLocation(final World world, final Random random) {
		final int level = this.plugin.getLevelFromWorld(world);
		Location loc;
		for (int i=0; i<10; i++) {
			loc = this.getSpawn(level);
			if (loc.getWorld().equals(world))
				return loc;
		}
		return this.getSpawn(level);
	}



	public static void MakeWorld(final int level, final String seed) {
		final MVWorldManager manager = GetMVCore().getMVWorldManager();
		final String name = "level" + Integer.toString(level);
		if (!manager.isMVWorld(name, false)) {
			BackroomsPlugin.log.warning(String.format(
				"%sCreating backrooms level: %d",
				BackroomsPlugin.LOG_PREFIX,
				Integer.valueOf(level)
			));
			final Environment env;
			switch (level) {
			case 78: env = Environment.THE_END; break;
			default: env = Environment.NORMAL;  break;
			}
			if (!manager.addWorld(name, env, seed, WorldType.NORMAL, Boolean.FALSE, "pxnBackrooms", true))
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
			mvworld.setGenerator("");
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
