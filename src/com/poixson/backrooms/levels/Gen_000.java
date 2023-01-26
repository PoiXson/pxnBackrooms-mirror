package com.poixson.backrooms.levels;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;

import com.poixson.backrooms.BackroomsPlugin;
import com.poixson.tools.dao.Dxy;
import com.poixson.utils.FastNoiseLiteD;
import com.poixson.utils.FastNoiseLiteD.CellularDistanceFunction;
import com.poixson.utils.FastNoiseLiteD.CellularReturnType;
import com.poixson.utils.FastNoiseLiteD.FractalType;
import com.poixson.utils.FastNoiseLiteD.NoiseType;
import com.poixson.utils.FastNoiseLiteD.RotationType3D;


// 0 | Lobby
public class Gen_000 extends GenBackrooms {

	public static final boolean BUILD_ROOF = Level_000.BUILD_ROOF;

	public static final Material LOBBY_WALL = Material.YELLOW_TERRACOTTA;
	public static final Material LOBBY_SUBFLOOR  = Material.OAK_PLANKS;

	public final boolean buildroof;
	public final int subfloor;
	public final int subceiling;

	// noise
	protected final FastNoiseLiteD noiseLobbyWalls;



	public Gen_000(final BackroomsPlugin plugin, final int level_y, final int level_h,
			final boolean buildroof, final int subfloor, final int subceiling) {
		super(plugin, level_y, level_h);
		this.buildroof  = buildroof;
		this.subfloor   = subfloor;
		this.subceiling = subceiling;
		// lobby walls
		this.noiseLobbyWalls = this.register(new FastNoiseLiteD());
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
	public void generate(final Map<Dxy, ? extends PreGenData> datamap,
			final ChunkData chunk, final int chunkX, final int chunkZ) {
		for (int z=0; z<16; z++) {
			for (int x=0; x<16; x++) {
				final int xx = (chunkX * 16) + x;
				final int zz = (chunkZ * 16) + z;
/*
				int y  = this.level_y;
				int cy = this.level_y + SUBFLOOR + this.level_h;
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
					final int h = this.level_h + 1;
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
*/
			} // end x
		} // end z
	}



}
