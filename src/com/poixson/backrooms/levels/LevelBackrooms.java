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



	public abstract int getLevelFromY(final int y);
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
			BackroomsPlugin.log.warning(String.format("%sCreating backrooms level: %d", BackroomsPlugin.LOG_PREFIX, Integer.toString(level)));
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



	public static MultiverseCore GetMVCore() {
		final PluginManager pm = Bukkit.getServer().getPluginManager();
		final MultiverseCore mvcore = (MultiverseCore) pm.getPlugin("Multiverse-Core");
		if (mvcore == null) throw new RuntimeException("Multiverse-Core plugin not found");
		return mvcore;
	}



}
