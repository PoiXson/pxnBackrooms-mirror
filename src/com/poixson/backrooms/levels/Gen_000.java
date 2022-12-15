package com.poixson.backrooms.levels;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


// 0 | lobby
public class Gen_000 extends BackroomsGenerator {

	public static final boolean BUILD_ROOF = Level_000.BUILD_ROOF;
	public static final int     SUBFLOOR   = Level_000.SUBFLOOR;
	public static final int     SUBCEILING = Level_000.SUBCEILING;

	public static final int LOBBY_Y = Level_000.Y_000;
	public static final int LOBBY_H = Level_000.H_000;

	public static final Material LOBBY_WALL = Material.YELLOW_TERRACOTTA;
	public static final Material LOBBY_SUBFLOOR  = Material.OAK_PLANKS;

	// noise
	protected final FastNoiseLiteD noiseLobbyWalls;



	public Gen_000() {
		super();
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
	@Override
	public void unload() {
	}



	@Override
	public void setSeed(final int seed) {
		this.noiseLobbyWalls.setSeed(seed);
	}



	protected void generateLobby(
			final int chunkX, final int chunkZ, final ChunkData chunk,
			final int x, final int z, final int xx, final int zz) {
		int y  = LOBBY_Y;
		int cy = LOBBY_Y + SUBFLOOR + LOBBY_H;
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
			final int h = LOBBY_H + 1;
			for (int yy=0; yy<h; yy++) {
				chunk.setBlock(x, y+yy, z, LOBBY_WALL);
			}
		} else {
			chunk.setBlock(x, y, z, Material.LIGHT_GRAY_WOOL);
			if (BUILD_ROOF) {
				final int modX6 = Math.abs(xx) % 7;
				final int modZ6 = Math.abs(zz) % 7;
				if (modZ6 == 0 && modX6 < 2) {
//TODO: not near walls
					// ceiling lights
					chunk.setBlock(x, cy, z, Material.REDSTONE_LAMP);
						final BlockData block = chunk.getBlockData(x, cy, z);
						((Lightable)block).setLit(true);
						chunk.setBlock(x, cy, z, block);
					chunk.setBlock(x, cy+1, z, Material.REDSTONE_BLOCK);
				} else {
					// ceiling
					chunk.setBlock(x, cy, z, Material.SMOOTH_STONE_SLAB);
						final Slab slab = (Slab) chunk.getBlockData(x, cy, z);
						slab.setType(Slab.Type.TOP);
						chunk.setBlock(x, cy, z, slab);
					chunk.setBlock(x, cy+1, z, Material.STONE);
				}
			}
		}
		if (BUILD_ROOF) {
			cy++;
			for (int i=1; i<SUBCEILING; i++) {
				chunk.setBlock(x, cy+i, z, Material.STONE);
			}
		}
	}



}
