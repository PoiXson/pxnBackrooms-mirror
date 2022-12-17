package com.poixson.backrooms.levels;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;


public abstract class BackroomsLevel extends ChunkGenerator {

	protected final BackroomsPlugin plugin;



	public BackroomsLevel(final BackroomsPlugin plugin) {
		this.plugin = plugin;
	}

	public abstract void unload();



	public abstract int getLevelFromY(final int y);
	public abstract int getYFromLevel(final int level);
	public abstract int getMaxYFromLevel(final int level);

	public abstract Location getSpawn(final int level);
	public abstract Location getSpawn(final int level, final int x, final int z);

	public Location getSpawn(final int level, final int h,
			final int x, final int y, final int z) {
		final World world = this.plugin.getLevelWorld(level);
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
		final int level = this.plugin.getLevel(world);
		Location loc;
		for (int i=0; i<10; i++) {
			loc = this.getSpawn(level);
			if (loc.getWorld().equals(world))
				return loc;
		}
		return this.getSpawn(level);
	}



	@Override
	public abstract void generateSurface(final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk);



}
