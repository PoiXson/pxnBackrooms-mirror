package com.poixson.backrooms.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


// 309 | Path
//   0 | Lobby
//  -1 | Basement
public class BackGen_000 extends BackroomsGenerator {

	public static final int BASEMENT_LIGHT_RADIUS = 20;

	public static final int SUBFLOOR = 3;

	public static final int BASEMENT_Y      = 0;
	public static final int BASEMENT_HEIGHT = 30;

	public static final int LOBBY_Y      = 31;
	public static final int LOBBY_HEIGHT = 11;

	public static final int PATH_Y        = 54;
	public static final int PATH_WIDTH    = 3;
	public static final int PATH_CLEARING = 10;

	public static final double MOIST_THRESHOLD = 0.35;

	public static final Material BASEMENT_WALL      = Material.MUD_BRICKS;
	public static final Material BASEMENT_SUBFLOOR  = Material.DIRT;
	public static final Material BASEMENT_FLOOR_DRY = Material.BROWN_CONCRETE_POWDER;
	public static final Material BASEMENT_FLOOR_WET = Material.BROWN_CONCRETE;

	public static final Material LOBBY_WALL = Material.YELLOW_TERRACOTTA;

	public static final Material PATH_TREE_TRUNK  = Material.BIRCH_LOG;
	public static final Material PATH_TREE_LEAVES = Material.BIRCH_LEAVES;

	protected final FastNoiseLiteD noiseMoist;
	protected final FastNoiseLiteD noiseBasementWalls;
	protected final FastNoiseLiteD noiseLobbyWalls;
	protected final FastNoiseLiteD noisePath;
	protected final FastNoiseLiteD noisePathGround;
	protected final FastNoiseLiteD noiseTrees;

	protected final HashMap<String, ArrayList<Location>> playerLights = new HashMap<String, ArrayList<Location>>();

	protected final TreePopulator treePop;
	protected final PathTracer pathTrace;
	protected final AtomicReference<ConcurrentHashMap<Integer, Double>> pathCache =
			new AtomicReference<ConcurrentHashMap<Integer, Double>>(null);



	public BackGen_000(final BackroomsPlugin plugin) {
		super(plugin);
		// moist noise
		this.noiseMoist = new FastNoiseLiteD();
		this.noiseMoist.setFrequency(0.03);
		this.noiseMoist.setFractalOctaves(2);
		this.noiseMoist.setFractalGain(2.0);
		// basement wall noise
		this.noiseBasementWalls = new FastNoiseLiteD();
		this.noiseBasementWalls.setFrequency(0.035);
		this.noiseBasementWalls.setFractalOctaves(1);
		this.noiseBasementWalls.setNoiseType(NoiseType.Cellular);
		this.noiseBasementWalls.setFractalType(FractalType.PingPong);
		this.noiseBasementWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		this.noiseBasementWalls.setCellularReturnType(CellularReturnType.Distance);
		// lobby walls
		this.noiseLobbyWalls = new FastNoiseLiteD();
		this.noiseLobbyWalls.setFrequency(0.023);
		this.noiseLobbyWalls.setFractalOctaves(2);
		this.noiseLobbyWalls.setFractalGain(0.05);
		this.noiseLobbyWalls.setNoiseType(NoiseType.Cellular);
		this.noiseLobbyWalls.setFractalType(FractalType.PingPong);
		this.noiseLobbyWalls.setCellularDistanceFunction(CellularDistanceFunction.Manhattan);
		this.noiseLobbyWalls.setCellularReturnType(CellularReturnType.Distance);
		this.noiseLobbyWalls.setRotationType3D(RotationType3D.ImproveXYPlanes);
		// path
		this.noisePath = new FastNoiseLiteD();
		this.noisePath.setFrequency(0.01f);
		// path ground
		this.noisePathGround = new FastNoiseLiteD();
		this.noisePathGround.setFrequency(0.002f);
		this.noisePathGround.setFractalType(FractalType.Ridged);
		this.noisePathGround.setFractalOctaves(3);
		this.noisePathGround.setFractalGain(0.5f);
		this.noisePathGround.setFractalLacunarity(2.0f);
		// tree noise
		this.noiseTrees = new FastNoiseLiteD();
		this.noiseTrees.setFrequency(0.2f);
		// populators
		this.treePop = new TreePopulator309(this.noiseTrees, PATH_Y);
		this.pathTrace = new PathTracer(this.noisePath, this.getPathCacheMap());
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
		this.pathCache.set(null);
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
		int xx, zz;
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				xx = x + (chunkX * 16);
				zz = z + (chunkZ * 16);
				// basement
				this.generateBasement(chunkX, chunkZ, chunk, x, z, xx, zz);
				// 0 main lobby
				this.generateLobby(chunkX, chunkZ, chunk, x, z, xx, zz);
				// 309 woods path
				this.generateWoodsPath(chunkX, chunkZ, chunk, x, z, xx, zz);
			}
		}
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
				if (modX10 < 5) {
					chunk.setBlock(x, y+5, z, Material.REDSTONE_LAMP);
					switch (modX10) {
					case 0:
					case 4:
						for (int yy=0; yy<3; yy++) {
							chunk.setBlock(x, y+yy+6, z, Material.CHAIN);
						}
						break;
					case 1:
					case 3: chunk.setBlock(x, y+6, z, Material.REDSTONE_WIRE); break;
					case 2: chunk.setBlock(x, y+6, z, Material.BEDROCK);       break;
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

	protected void generateLobby(
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y = LOBBY_Y;
		// lobby floor
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int yy=0; yy<SUBFLOOR; yy++) {
			chunk.setBlock(x, y+yy, z, BASEMENT_SUBFLOOR);
		}
		y += SUBFLOOR;
		final double value = this.noiseLobbyWalls.getNoiseRot(xx, zz, 0.25);
		final boolean isWall = (value > 0.38 && value < 0.5);
		if (isWall) {
			// lobby walls
			final int h = LOBBY_HEIGHT - SUBFLOOR;
			for (int yy=0; yy<h; yy++) {
				chunk.setBlock(x, y+yy, z, LOBBY_WALL);
			}
		} else {
			chunk.setBlock(x, y, z, Material.LIGHT_GRAY_WOOL);
			y += 6;
			final int modX6 = Math.abs(xx) % 7;
			final int modZ6 = Math.abs(zz) % 7;
			if (modZ6 == 0 && modX6 < 2) {
				// ceiling lights
				chunk.setBlock(x, y, z, Material.REDSTONE_LAMP);
				final BlockData block = chunk.getBlockData(x, y, z);
				((Lightable)block).setLit(true);
				chunk.setBlock(x, y,   z, block);
				chunk.setBlock(x, y+1, z, Material.REDSTONE_BLOCK);
			} else {
				// ceiling
				chunk.setBlock(x, y, z, Material.SMOOTH_STONE_SLAB);
				final BlockData block = chunk.getBlockData(x, y, z);
				((Slab)block).setType(Slab.Type.TOP);
				chunk.setBlock(x, y,   z, block);
				chunk.setBlock(x, y+1, z, Material.STONE);
			}
		}
	}

	protected void generateWoodsPath(
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y = PATH_Y;
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int i=0; i<SUBFLOOR; i++) {
			chunk.setBlock(x, y+i, z, Material.STONE);
		}
		y += SUBFLOOR;
		final double ground;
		{
			final double g = this.noisePathGround.getNoise(xx, zz);
			ground = 1.0f + (g < 0.0f ? g * 0.6f : g);
		}
		// dirt
		final int elevation = (int) (ground * 2.5f); // 0 to 5
		for (int i=0; i<elevation; i++) {
			if (i >= elevation-1) {
				if (this.pathTrace.isPath(xx, zz, PATH_WIDTH)) {
					chunk.setBlock(x, y+i, z, Material.DIRT_PATH);
				} else {
					chunk.setBlock(x, y+i, z, Material.GRASS_BLOCK);
				}
			} else {
				chunk.setBlock(x, y+i, z, Material.DIRT);
			}
		}
	}



	@Override
	public List<BlockPopulator> getDefaultPopulators(final World world) {
		return Arrays.asList(
			this.treePop
		);
	}

	public class TreePopulator309 extends TreePopulator {

		public TreePopulator309(final FastNoiseLiteD noise, final int chunkY) {
			super(
				noise, chunkY,
				PATH_TREE_TRUNK,
				PATH_TREE_LEAVES
			);
		}

		public boolean isTree(final int x, final int z) {
			if (!super.isTree(x, z))
				return false;
			if (BackGen_000.this.pathTrace.isPath(x, z, PATH_CLEARING))
				return false;
			return true;
		}

	}



	public ConcurrentHashMap<Integer, Double> getPathCacheMap() {
		// existing
		{
			final ConcurrentHashMap<Integer, Double> cache = this.pathCache.get();
			if (cache != null)
				return cache;
		}
		// new instance
		{
			final ConcurrentHashMap<Integer, Double> cache = new ConcurrentHashMap<Integer, Double>();
			if (this.pathCache.compareAndSet(null, cache))
				return cache;
		}
		return this.getPathCacheMap();
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
		final int fx = (Math.floorDiv(toX, 10) * 10) + 2;
		final int fz =  Math.floorDiv(toZ, 10) * 10;
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



}
