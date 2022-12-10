package com.poixson.backrooms.generators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.WorldInfo;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


public class Level_000 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = true;

	public static final int SUBFLOOR = BackGen_000.SUBFLOOR;

	public static final int LOBBY_Y      = 31;
	public static final int LOBBY_HEIGHT = 11;

	public static final Material LOBBY_WALL = Material.YELLOW_TERRACOTTA;
	public static final Material LOBBY_SUBFLOOR  = Material.DIRT;

	protected final FastNoiseLiteD noiseLobbyWalls;



	public Level_000(final BackroomsPlugin plugin) {
		super(plugin);
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
	}



	public void setSeed(final int seed) {
		this.noiseLobbyWalls.setSeed(seed);
	}



	@Override
	public void generateSurface(
			final WorldInfo worldInfo, final Random random,
			final int chunkX, final int chunkZ, final ChunkData chunk) {
	}



	protected void generateLobby(
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y = LOBBY_Y;
		// lobby floor
		chunk.setBlock(x, y, z, Material.BEDROCK);
		y++;
		for (int yy=0; yy<SUBFLOOR; yy++) {
			chunk.setBlock(x, y+yy, z, LOBBY_SUBFLOOR);
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
			if (BUILD_ROOF) {
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
						final Slab slab = (Slab) chunk.getBlockData(x, y, z);
						slab.setType(Slab.Type.TOP);
						chunk.setBlock(x, y,   z, slab);
					chunk.setBlock(x, y+1, z, Material.STONE);
				}
			}
		}
	}



}
