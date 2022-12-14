package com.poixson.backrooms.levels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;


// 1 | Basement
public class Gen_001 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = Level_000.BUILD_ROOF;
	public static final int     SUBFLOOR   = Level_000.SUBFLOOR;
	public static final int     SUBCEILING = Level_000.SUBCEILING;

	public static final int BASEMENT_Y = Level_000.Y_001;
	public static final int BASEMENT_H = Level_000.H_001;

	public static final int BASEMENT_LIGHT_RADIUS = 20;
	public static final double MOIST_THRESHOLD = 0.35;

	public static final Material BASEMENT_WALL      = Material.MUD_BRICKS;
	public static final Material BASEMENT_SUBFLOOR  = Material.DIRT;
	public static final Material BASEMENT_FLOOR_DRY = Material.BROWN_CONCRETE_POWDER;
	public static final Material BASEMENT_FLOOR_WET = Material.BROWN_CONCRETE;

	// noise
	protected final FastNoiseLiteD noiseBasementWalls;
	protected final FastNoiseLiteD noiseMoist;

	protected final HashMap<String, ArrayList<Location>> playerLights =
			new HashMap<String, ArrayList<Location>>();



	public Gen_001() {
		super();
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
		synchronized (this.playerLights) {
			Block blk;
			for (final ArrayList<Location> list : this.playerLights.values()) {
				for (final Location loc : list) {
					blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
			}
			this.playerLights.clear();
		}
	}



	@Override
	public void setSeed(final int seed) {
		this.noiseBasementWalls.setSeed(seed);
		this.noiseMoist.setSeed(seed);
	}



	public void generateBasement(
			final ChunkData chunk, final int chunkX, final int chunkZ,
			final int x, final int z, final int xx, final int zz) {
		int y  = BASEMENT_Y;
		int cy = BASEMENT_Y + SUBFLOOR + BASEMENT_H;
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
			final int h = BASEMENT_H - 2;
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
						for (int iy=0; iy<3; iy++) {
							chunk.setBlock(x, y+iy+6, z, Material.CHAIN);
						}
						break;
					}
				}
			}
		}
		// basement ceiling
		if (BUILD_ROOF) {
			chunk.setBlock(x, cy-1, z, Material.BEDROCK);
			if (isWet && !isWall) {
				chunk.setBlock(x, cy, z, Material.WATER);
			} else {
				chunk.setBlock(x, cy, z, Material.STONE);
			}
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
			String key;
			ArrayList<Location> list;
			final Iterator<String> it = this.playerLights.keySet().iterator();
			while (it.hasNext()) {
				key = it.next();
				list = this.playerLights.get(key);
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



	public void onPlayerMove(final PlayerMoveEvent event, final int level) {
		final Player player = event.getPlayer();
		final Location to   = event.getTo();
		final int toX = to.getBlockX();
		final int toY = to.getBlockY();
		final int toZ = to.getBlockZ();
		// player left world
		if (level != 0) {
			final String uuid = player.getUniqueId().toString();
			if (this.playerLights.containsKey(uuid)) {
				Block blk;
				for (final Location loc : this.playerLights.get(uuid)) {
					blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
				this.playerLights.remove(uuid);
			}
			return;
		}
		// basement level
		if (toY < BASEMENT_Y           ) return;
		if (toY > BASEMENT_Y+BASEMENT_H) return;
		final ArrayList<Location> lights = this.getPlayerLightsList(player);
		// turn off lights
		{
			Location loc;
			Block blk;
			final Iterator<Location> it = lights.iterator();
			while (it.hasNext()) {
				loc = it.next();
				if (to.distance(loc) > BASEMENT_LIGHT_RADIUS) {
					it.remove();
					blk = loc.getBlock();
					if (Material.REDSTONE_TORCH.equals(blk.getType()))
						blk.setType(Material.BEDROCK);
				}
			}
		}
		final int y = BASEMENT_Y + 10;
		final World world = player.getWorld();
		final int r = BASEMENT_LIGHT_RADIUS;
		int xx, zz;
		for (int iz=0-r-1; iz<r; iz+=10) {
			zz = Math.floorDiv(toZ+iz, 10) * 10;
			for (int ix=0-r-1; ix<r; ix+=10) {
				xx = Math.floorDiv(toX+ix, 10) * 10;
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
