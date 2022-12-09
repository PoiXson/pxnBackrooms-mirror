package com.poixson.backrooms.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


public class Level_N001 extends BackroomsGenerator {

	public static final int SUBFLOOR = BackGen_000.SUBFLOOR;

	public static final int BASEMENT_Y      = 0;
	public static final int BASEMENT_HEIGHT = 30;

	public static final int BASEMENT_LIGHT_RADIUS = 20;

	public static final double MOIST_THRESHOLD = 0.35;

	public static final Material BASEMENT_WALL      = Material.MUD_BRICKS;
	public static final Material BASEMENT_SUBFLOOR  = Material.DIRT;
	public static final Material BASEMENT_FLOOR_DRY = Material.BROWN_CONCRETE_POWDER;
	public static final Material BASEMENT_FLOOR_WET = Material.BROWN_CONCRETE;

	protected final FastNoiseLiteD noiseBasementWalls;
	protected final FastNoiseLiteD noiseMoist;

	protected final HashMap<String, ArrayList<Location>> playerLights =
			new HashMap<String, ArrayList<Location>>();



	public Level_N001(final BackroomsPlugin plugin) {
		super(plugin);
		// basement wall noise
		this.noiseBasementWalls = new FastNoiseLiteD();
		this.noiseBasementWalls.setFrequency(0.035);
		this.noiseBasementWalls.setFractalOctaves(1);
		this.noiseBasementWalls.setNoiseType(NoiseType.Cellular);
		this.noiseBasementWalls.setFractalType(FractalType.PingPong);
		this.noiseBasementWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		this.noiseBasementWalls.setCellularReturnType(CellularReturnType.Distance);
		// moist noise
		this.noiseMoist = new FastNoiseLiteD();
		this.noiseMoist.setFrequency(0.03);
		this.noiseMoist.setFractalOctaves(2);
		this.noiseMoist.setFractalGain(2.0);
	}

	@Override
	public void unload() {
		super.unload();
		synchronized (this.playerLights) {
			for (final ArrayList<Location> list : this.playerLights.values()) {
				for (final Location loc : list) {
					final Block blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
			}
			this.playerLights.clear();
		}
	}



	public void setSeed(final int seed) {
		this.noiseBasementWalls.setSeed(seed);
		this.noiseMoist.setSeed(seed);
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
	}



	protected void generateBasement(
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y = BASEMENT_Y;
		// basement floor
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int yy=0; yy<SUBFLOOR; yy++) {
			chunk.setBlock(x, y+yy, z, BASEMENT_SUBFLOOR);
		}
		y += SUBFLOOR;
		final double moist = this.noiseMoist.getNoise(xx, zz);
		final boolean isWet = (moist > MOIST_THRESHOLD);
		final double value = this.noiseBasementWalls.getNoiseRot(xx, zz, 0.25);
		final boolean isWall = (value > 0.8 && value < 0.95);
		if (isWall) {
			// basement walls
			final int h = BASEMENT_HEIGHT - SUBFLOOR - 2;
			for (int yy=0; yy<h; yy++) {
				if (yy > 6) {
					chunk.setBlock(x, y+yy, z, Material.BEDROCK);
				} else {
					chunk.setBlock(x, y+yy, z, BASEMENT_WALL);
				}
			}
		} else {
			if (isWet) {
				chunk.setBlock(x, y, z, BASEMENT_FLOOR_WET);
			} else {
				chunk.setBlock(x, y, z, BASEMENT_FLOOR_DRY);
			}
			// basement lights
			final int modX10 = Math.abs(xx) % 10;
			final int modZ10 = Math.abs(zz) % 10;
			if (modZ10 == 0) {
				if (modX10 < 3 || modX10 > 7) {
					chunk.setBlock(x, y+5, z, Material.REDSTONE_LAMP);
					switch (modX10) {
					case 0: chunk.setBlock(x, y+6, z, Material.BEDROCK);       break;
					case 1:
					case 9: chunk.setBlock(x, y+6, z, Material.REDSTONE_WIRE); break;
					case 2:
					case 8:
						for (int yy=0; yy<3; yy++) {
							chunk.setBlock(x, y+yy+6, z, Material.CHAIN);
						}
						break;
					}
				}
			}
		}
		// basement ceiling
		y += BASEMENT_HEIGHT - SUBFLOOR - 1;
		chunk.setBlock(x, y-1, z, Material.BEDROCK);
		if (isWet && !isWall) {
			chunk.setBlock(x, y, z, Material.WATER);
		} else {
			chunk.setBlock(x, y, z, Material.STONE);
		}
	}



	public ArrayList<Location> getPlayerLightsList(final Player player) {
		return this.getPlayerLightsList(player.getUniqueId().toString());
	}
	public ArrayList<Location> getPlayerLightsList(final String uuid) {
		// existing list
		{
			final ArrayList<Location> list = this.playerLights.get(uuid);
			if (list != null)
				return list;
		}
		// cleanup
//TODO: improve this
		if (this.playerLights.size() % 5 == 0) {
			final Iterator<String> it = this.playerLights.keySet().iterator();
			while (it.hasNext()) {
				final String key = it.next();
				final ArrayList<Location> list = this.playerLights.get(key);
				if (list == null || list.isEmpty())
					this.playerLights.remove(key);
			}
		}
		// new list
		{
			final ArrayList<Location> list = new ArrayList<Location>();
			this.playerLights.put(uuid, list);
			return list;
		}
	}



	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		if (!"level0".equals(player.getWorld().getName())) {
			// player left world
			final String uuid = player.getUniqueId().toString();
			if (this.playerLights.containsKey(uuid)) {
				for (final Location loc : this.playerLights.get(uuid)) {
					final Block blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
				this.playerLights.remove(uuid);
			}
			return;
		}
		// location changed
		final Location from = event.getFrom();
		final Location to   = event.getTo();
		final int toX = to.getBlockX();
		final int toY = to.getBlockY();
		final int toZ = to.getBlockZ();
		if (from.getBlockX() == toX && from.getBlockZ() == toZ)
			return;
		// basement level
		if (toY < BASEMENT_Y                ) return;
		if (toY > BASEMENT_Y+BASEMENT_HEIGHT) return;
		final ArrayList<Location> lights = this.getPlayerLightsList(player);
		// turn off lights
		{
			final Iterator<Location> it = lights.iterator();
			while (it.hasNext()) {
				final Location loc = it.next();
				if (to.distance(loc) > BASEMENT_LIGHT_RADIUS) {
					it.remove();
					final Block blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
			}
		}
		final int y = 10;
		final World world = player.getWorld();
		final int r = BASEMENT_LIGHT_RADIUS;
		final int rr = r * 2;
		final int fx = Math.floorDiv(toX, 10) * 10;
		final int fz = Math.floorDiv(toZ, 10) * 10;
		int xx, zz;
		for (int z=0; z<rr; z+=10) {
			zz = (fz + z) - r;
			for (int x=0; x<rr; x+=10) {
				xx = (fx + x) - r;
				final Block blk = world.getBlockAt(xx, y, zz);
				if (to.distance(blk.getLocation()) < BASEMENT_LIGHT_RADIUS) {
					if (Material.BEDROCK.equals(blk.getType())
					||  Material.REDSTONE_TORCH.equals(blk.getType()) ) {
						lights.add(blk.getLocation());
						world.setType(xx, y, zz, Material.REDSTONE_TORCH);
					}
				}
			}
		}
	}



}
